plugins {
    id("com.google.protobuf") version "0.9.4"

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
    api("com.google.protobuf:protobuf-kotlin:4.28.2")
    implementation ("com.google.protobuf:protobuf-java-util:4.28.2")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.28.2"
    }
}
