package com.uos.picobox.global.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    /**
     * 파일을 S3에 업로드하고 해당 파일의 전체 URL을 반환합니다.
     * @param file 업로드할 파일
     * @param directoryPath S3 버킷 내 저장할 디렉토리 경로 (예: "movie-posters")
     * @return 업로드된 파일의 전체 S3 URL
     * @throws IOException 파일 처리 중 오류 발생 시
     * @throws S3Exception S3 업로드 실패 시 SDK에서 발생하는 예외
     * @throws SdkException S3 업로드 실패 시 SDK에서 발생하는 일반 예외
     */
    public String upload(MultipartFile file, String directoryPath) throws IOException {
        if (file == null || file.isEmpty()) {
            log.warn("업로드할 파일이 비어있습니다.");
            return null;
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFileName = UUID.randomUUID().toString() + extension;
        String s3Key = directoryPath + "/" + uniqueFileName;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType(file.getContentType())
                .build();

        try {
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // 업로드된 객체의 URL 가져오기
            GetUrlRequest getUrlRequest = GetUrlRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();
            String fileUrl = s3Client.utilities().getUrl(getUrlRequest).toString();

            log.info("S3 파일 업로드 성공: Key = {}, URL = {}", s3Key, fileUrl);
            return fileUrl;
        } catch (S3Exception e) { // S3 서비스 관련 예외
            log.error("S3 업로드 중 S3Exception 발생: {}", e.getMessage(), e);
            throw e;
        } catch (SdkException e) { // AWS SDK 일반 예외 (네트워크 오류 등)
            log.error("S3 업로드 중 SdkException 발생: {}", e.getMessage(), e);
            throw e;
        } catch (IOException e) { // 파일 스트림 오류
            log.error("S3 업로드 중 IOException 발생 (파일 스트림 오류): {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * S3에서 파일을 삭제합니다.
     * @param fileUrl 삭제할 파일의 전체 S3 URL
     */
    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            log.warn("삭제할 S3 파일 URL이 비어있습니다.");
            return;
        }

        try {
            String objectKey = extractObjectKeyFromUrl(fileUrl);
            if (objectKey == null) {
                log.warn("유효하지 않은 S3 URL이거나 객체 키를 추출할 수 없습니다: {}", fileUrl);
                return;
            }

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
            log.info("S3 파일 삭제 성공: Key = {}", objectKey);
        } catch (S3Exception e) {
            log.error("S3 파일 삭제 중 S3Exception 발생 (URL: {}): {}", fileUrl, e.getMessage(), e);
        } catch (SdkException e) {
            log.error("S3 파일 삭제 중 SdkException 발생 (URL: {}): {}", fileUrl, e.getMessage(), e);
        } catch (Exception e) {
            log.error("S3 파일 URL 파싱 또는 삭제 중 예외 발생 (URL: {}): {}", fileUrl, e.getMessage(), e);
        }
    }

    private String extractObjectKeyFromUrl(String fileUrl) {
        try {
            java.net.URL url = new java.net.URL(fileUrl);
            String path = url.getPath();
            String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8);
            String keyPrefix = "/" + bucketName + "/";
            if (decodedPath.startsWith(keyPrefix)) {
                return decodedPath.substring(keyPrefix.length());
            } else if (decodedPath.startsWith("/")) {
                return decodedPath.substring(1);
            }
            return decodedPath;
        } catch (Exception e) {
            log.error("S3 URL에서 객체 키 추출 실패: {}", fileUrl, e);
            return null;
        }
    }
}