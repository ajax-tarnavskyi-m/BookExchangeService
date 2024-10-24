plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "book-exchange-service"
include("gateway", "internal-api", "domain-service", "core")
