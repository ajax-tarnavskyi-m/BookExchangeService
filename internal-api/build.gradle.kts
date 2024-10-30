plugins {
    id("com.google.protobuf") version "0.9.4"
    id("kotlin-conventions")
}

dependencies {
    api("com.google.protobuf:protobuf-kotlin:4.28.2")
    implementation ("com.google.protobuf:protobuf-java-util:4.28.2")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.28.2"
    }
}
