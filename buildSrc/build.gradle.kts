plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.10")
    implementation("io.github.surpsg:delta-coverage-gradle:2.5.0")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.7")
    implementation("org.springframework.boot:spring-boot-gradle-plugin:3.3.2")
    implementation("org.jetbrains.kotlin:kotlin-allopen:2.0.10")
}
