plugins {
    id("spring-conventions")
}

dependencies {
    implementation(project(":internal-api"))
    implementation(project(":core"))

    implementation("org.apache.logging.log4j:log4j-api-kotlin:1.5.0")
    implementation("org.springframework.boot:spring-boot-starter-validation:3.3.2")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.mongock:mongock-springboot-v3:5.4.4")
    implementation("io.mongock:mongodb-springdata-v4-driver:5.4.4")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.2.3")
    implementation ("org.springframework.boot:spring-boot-starter-webflux")
    implementation("io.nats:jnats:2.16.14")

    testImplementation("com.ninja-squad:springmockk:3.0.1")
    testImplementation("org.testcontainers:mongodb:1.20.1")
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("io.projectreactor:reactor-test:3.6.10")
}
