plugins {
    id("java")
    id("org.springframework.boot") version("3.5.4")
    id("io.spring.dependency-management") version ("1.1.7")
}

group = "company-service"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    // junit
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // spring
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // postgres
    runtimeOnly("org.postgresql:postgresql")

    // lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")

}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2025.0.0")
    }
}

tasks.test {
    useJUnitPlatform()
}