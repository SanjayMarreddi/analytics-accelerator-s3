/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("buildlogic.java-library-conventions")
    id("io.freefair.lombok") version "8.10.2"
    `maven-publish`
}

dependencies {
    implementation(libs.log4j.api)
    implementation(libs.log4j.core)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.mockito.junit.jupiter)

    testRuntimeOnly(libs.junit.jupiter.launcher)
}

tasks.withType<JavaCompile>().configureEach {
}

publishing {
    publications {
        create<MavenPublication>("common") {
            groupId = "com.amazon.connector.s3"
            version = "0.0.1"

            from(components["java"])
        }
    }
}
