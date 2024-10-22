import io.gitlab.arturbosch.detekt.Detekt

plugins {
    kotlin("jvm")
    id("io.gitlab.arturbosch.detekt")
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.6")
}

val targetJvmVersion = JavaLanguageVersion.of(17)

kotlin {
    jvmToolchain {
        languageVersion.set(targetJvmVersion)
    }
}

java {
    toolchain {
        languageVersion.set(targetJvmVersion)
    }
}

detekt {
    buildUponDefaultConfig = true
    config.from(file("${rootDir}/config/detekt/detekt.yml"))
}

tasks.withType<Detekt> {
    reports {
        html.required.set(true)
        xml.required.set(true)
        sarif.required.set(true)
        md.required.set(true)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
