plugins {
    id("kotlin-conventions")
    id("io.spring.dependency-management")
    `java-test-fixtures`
}

dependencies {
    implementation("org.mongodb:bson:5.2.0")
    implementation("org.springframework.boot:spring-boot-starter-validation:3.3.2")
    implementation("org.springframework.boot:spring-boot-starter-web:3.3.2")

    testFixturesImplementation("org.mongodb:bson:5.2.0")
    testFixturesImplementation("io.github.serpro69:kotlin-faker:1.16.0")
}
