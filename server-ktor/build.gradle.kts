plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.serialization") version "2.2.21"
    application
}

group = "com.idlestables"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    val ktorVersion = "2.3.12"

    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:1.5.6")

    // HTTP clients
    implementation("io.ktor:ktor-client-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-cio-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-logging-jvm:$ktorVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

    // Solana tx/signing
    implementation("com.mmorrell:solanaj:1.28.0")

    testImplementation(kotlin("test"))
}

application {
    mainClass.set("com.idlestables.server.MainKt")
}

kotlin {
    jvmToolchain(21)
}
