plugins {
    id("java")
    id("jacoco")
    id("checkstyle")
//    alias(libs.plugins.jmh)
    alias(libs.plugins.jengelman)
    id("maven-publish")
}

apply(plugin = "checkstyle")
apply(plugin = "jacoco")
apply(plugin = "com.github.johnrengelman.shadow")
apply(plugin = "maven-publish")

group = "vad0"

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation(libs.agrona)
    implementation(libs.artio.codecs)
    implementation(libs.commons.collections)
    implementation(libs.jackson.databind)
    implementation(libs.lombok)
    annotationProcessor(libs.lombok)
    testImplementation(libs.jupiter.api)
    testRuntimeOnly(libs.jupiter.engine)
    testImplementation(libs.lombok)
    testAnnotationProcessor(libs.lombok)
}

java {
    val javaVersion = JavaVersion.VERSION_21
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
                minimum = "0.95".toBigDecimal()
            }
        }
    }
}

tasks.getByName("clean") {
    doLast {
        project.delete(projectDir.toPath().resolve("output"))
    }
}

sourceSets {
    test {
        java {
            srcDir("build/generated/sources/gfjson")
        }
    }
}

tasks.register("checkstyleAll") {
    group = "verification"
    dependsOn(tasks.withType<Checkstyle>())
}

tasks.register<JavaExec>("runJsonTool") {
    systemProperty("schemaPath", "src/test/resources/generator/schema.json");
    systemProperty("outputDir", "build/generated/sources/gfjson");
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("generator.JsonTool")
    enableAssertions = true
}
