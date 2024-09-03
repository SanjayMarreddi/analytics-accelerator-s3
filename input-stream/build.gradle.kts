import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.spotbugs.snom.SpotBugsTask

/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("buildlogic.java-library-conventions")
    id("io.freefair.lombok") version "8.6"
    id("me.champeau.jmh") version "0.7.2"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    `maven-publish`
}

// Allow to separate dependencies for reference testing
sourceSets {
    create("referenceTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

val referenceTestImplementation by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

val referenceTestRuntimeOnly by configurations.getting
configurations["referenceTestRuntimeOnly"].extendsFrom(configurations.runtimeOnly.get())

dependencies {
    api(project(":object-client"))

    implementation(project(":common"))
    implementation(libs.parquet.format)
    implementation(libs.log4j.api)
    implementation(libs.log4j.core)

    jmhImplementation(libs.s3)

    testImplementation(libs.s3)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.mockito.junit.jupiter)
    testImplementation(libs.sdk.url.connection.client)
    testImplementation(libs.netty.nio.client)
    testRuntimeOnly(libs.junit.jupiter.launcher)

    referenceTestImplementation(libs.s3mock.testcontainers)
    referenceTestImplementation(libs.testcontainers.junit.jupiter)

    referenceTestImplementation(libs.jqwik)
    referenceTestImplementation(libs.jqwik.testcontainers)
    referenceTestImplementation(libs.testcontainers)

    referenceTestRuntimeOnly(libs.junit.jupiter.launcher)
}

tasks.withType<JavaCompile>().configureEach {
}

tasks.compileJava {
    javaCompiler = javaToolchains.compilerFor {
        languageVersion = JavaLanguageVersion.of(8)
    }
}

tasks.named("compileReferenceTestJava", JavaCompile::class) {
    javaCompiler = javaToolchains.compilerFor {
        languageVersion = JavaLanguageVersion.of(17)
    }

    options.compilerArgs.add("-parameters")
}

val shadowJar = tasks.withType<ShadowJar> {
    relocate("org.apache.parquet.format", "com.amazon.shaded.apache.parquet.format")
    relocate("shaded.parquet.org.apache.thrift", "com.amazon.shaded.parquet.org.apache.thrift")
    relocate("org.apache.commons.io", "com.amazon.shaded.org.apache.commons.io")
}

val refTest = task<Test>("referenceTest") {
    description = "Runs reference tests."
    group = "verification"

    testClassesDirs = sourceSets["referenceTest"].output.classesDirs
    classpath = sourceSets["referenceTest"].runtimeClasspath
    shouldRunAfter("test")

    useJUnitPlatform()

    testLogging {
        events("passed")
        events("failed")
    }

    javaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(17)
    }

    environment("AWS_REGION", "eu-west-1")
}

tasks.spotbugsJmh {
    reports.create("html") {
        required = true
        setStylesheet("fancy-hist.xsl")
    }
}

tasks.named<SpotBugsTask>("spotbugsReferenceTest") {
    reports.create("html") {
        required = true
        setStylesheet("fancy-hist.xsl")
    }
}

tasks.build {dependsOn(shadowJar)}

tasks.check { dependsOn(refTest) }

// JMH micro-benchmarks
jmh {
    jmhVersion = "1.37"
    failOnError = true
    forceGC = true
    includeTests = false
    resultFormat = "JSON"
    zip64 = true
}

publishing {
    publications {
        create<MavenPublication>("inputStream") {
            // TODO: update this when we figure out versioning
            //  ticket: https://app.asana.com/0/1206885953994785/1207481230403504/f
            groupId = "com.amazon.connector.s3"
            version = "1.0.0"

            from(components["java"])
        }
    }
}
