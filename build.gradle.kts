plugins {
    id("java")
    id("jacoco")
    id("checkstyle")
//    alias(libs.plugins.jmh)
//    alias(libs.plugins.release)
    alias(libs.plugins.jengelman)
    id("maven-publish")
}

apply(plugin = "checkstyle")
apply(plugin = "jacoco")
apply(plugin = "com.github.johnrengelman.shadow")
apply(plugin = "maven-publish")

group = "vad0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.agrona)
    implementation(libs.artio.codecs)
    implementation(libs.apache.commons.collections4)
    testImplementation(libs.jupiter.api)
    testRuntimeOnly(libs.jupiter.engine)
    testImplementation(libs.jackson.databind)
    implementation(libs.lombok)
    annotationProcessor(libs.lombok)
    testImplementation(libs.lombok)
    testAnnotationProcessor(libs.lombok)
}

java {
    val javaVersion = JavaVersion.VERSION_17
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("PASSED", "SKIPPED", "FAILED")
    }
    finalizedBy("jacocoTestReport")
}

//jmh {
//    jvmArgs = ["-Djmh.ignoreLock=true"]
////    includes = ["Write*"]
//    fork = 1
//    warmupIterations = 3
//    iterations = 5
//    benchmarkMode = ["avgt"]
////    benchmarkMode = ["all"]
//    timeUnit = "ns"
//    failOnError = true
//    duplicateClassesStrategy = DuplicatesStrategy.INCLUDE
//}
//
//jmhJar { duplicatesStrategy(DuplicatesStrategy.INCLUDE) }

tasks.jacocoTestReport {
    reports {
        xml.required.set(false)
        csv.required.set(false)
//        html.required.set(true)
//        html.destination file("output/jacoco/jacocoHtml")
    }
    finalizedBy("jacocoTestCoverageVerification")
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
//            includes = [""]
//            excludes = [""]
            element = "CLASS"
            limit {
                counter = "LINE"
                minimum = "1.0".toBigDecimal()
            }
        }
    }
}

tasks.getByName("clean") {
    doLast {
        project.delete(projectDir.toPath().resolve("output"))
    }
}

//release {
//    failOnCommitNeeded = false
//    failOnUpdateNeeded = false
//}

publishing {
    publications {
        // This mavenJava can be filled in randomly, it's just a task name
        // MavenPublication must have, this is the task class to call
        create<MavenPublication>("maven") {
            // The header here is the artifacts configuration information, do not fill in the default
            groupId = "gfjson"
            artifactId = "library"
            version = "1.1"

            from(components["java"])
        }
    }
}