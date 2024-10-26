plugins {
    id("spring-conventions")
}

dependencies {
    implementation(project(":internal-api"))
    implementation(project(":core"))

    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.2.3")
    implementation("org.springframework.boot:spring-boot-starter-validation:3.3.2")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation ("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.nats:jnats:2.16.14")
    implementation("org.mongodb:bson:5.2.0")

    testImplementation(testFixtures(project(":core")))
    testImplementation("com.ninja-squad:springmockk:3.0.1")
}
