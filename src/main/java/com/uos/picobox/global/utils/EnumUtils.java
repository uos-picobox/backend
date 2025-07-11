package com.uos.picobox.global.utils;


import com.uos.picobox.global.enumClass.BaseEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnumUtils {
    /** enum 클래스에서 특정 값을 가진 원소를 찾습니다. */
    public static <T extends Enum<T> & BaseEnum> T fromValue(Class<T> enumClass, String value) {
        for (T enumConstant : enumClass.getEnumConstants()) {
            if (enumConstant.getValue().equals(value)) {
                return enumConstant;
            }
        }
        throw new IllegalArgumentException("해당 "+enumClass.getName() +" 값은 존재하지 않습니다: " + value);
    }
}
