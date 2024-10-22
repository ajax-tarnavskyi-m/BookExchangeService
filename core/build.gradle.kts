import io.github.surpsg.deltacoverage.gradle.DeltaCoverageConfiguration

plugins {
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.spring") version "1.9.23"
    id("org.springframework.boot") version "3.3.2"
    id("io.spring.dependency-management") version "1.1.6"
    id("io.github.surpsg.delta-coverage") version "2.4.0"
}

group = "pet.project"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
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

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        exclude(group = "org.mockito", module = "mockito-core")
    }
    testImplementation("com.ninja-squad:springmockk:3.0.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.testcontainers:mongodb:1.20.1")
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("org.awaitility:awaitility:3.0.0")
    testImplementation("org.awaitility:awaitility-proxy:3.0.0")
    testImplementation("io.projectreactor:reactor-test:3.6.10")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

configure<DeltaCoverageConfiguration> {
    val targetBranch = project.properties["diffBase"]?.toString() ?: "refs/heads/main"
    diffSource.byGit {
        compareWith(targetBranch)
    }
    violationRules.failIfCoverageLessThan(0.6)
}

tasks.check {
    dependsOn(tasks.deltaCoverage)
}

detekt {
    buildUponDefaultConfig = true
}
