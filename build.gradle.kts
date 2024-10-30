plugins {
    id("delta-coverage-conventions")
    id("kotlin-conventions")
}

allprojects {
    group = "pet.project"
    version = "0.0.1-SNAPSHOT"
    repositories {
        mavenCentral()
    }
}

tasks.check {
    dependsOn(tasks.deltaCoverage)
}

