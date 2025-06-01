plugins {
    java
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.uos"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")
    // s3
    implementation("io.awspring.cloud:spring-cloud-aws-starter-s3:3.2.1")
    // redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    // mail sender
    implementation("org.springframework.boot:spring-boot-starter-mail")
    // spring cache
    implementation("org.springframework.boot:spring-boot-starter-cache")
    // caffeine cache
    implementation("com.github.ben-manes.caffeine:caffeine")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // oracle jdbc
    implementation("com.oracle.database.jdbc:ojdbc11:23.8.0.25.04")
    implementation("com.oracle.database.security:oraclepki:23.8.0.25.04")
    implementation("com.oracle.database.security:osdt_cert:21.17.0.0")
    implementation("com.oracle.database.security:osdt_core:21.17.0.0")

    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
