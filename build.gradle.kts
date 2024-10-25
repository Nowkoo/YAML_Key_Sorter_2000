// ./gradlew clean shadowJar
// java -jar build/libs/OrdenadorYAML-1.0-SNAPSHOT-all.jar

plugins {
    kotlin("jvm") version "1.9.23"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "org.nowko"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation("org.yaml:snakeyaml:2.0")
}

application {
    mainClass.set("OrdenadorKt")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "OrdenadorKt" // Ajusta esto a tu clase principal
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(8)
}