plugins {
    kotlin("jvm")
    application

    id("com.github.johnrengelman.shadow")
}

dependencies {
    api(rootProject)

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains:annotations:24.0.1")
    implementation("org.slf4j:slf4j-api:2.0.5")
    implementation("org.java-websocket:Java-WebSocket:1.5.4")
    annotationProcessor("org.java-websocket:Java-WebSocket:1.5.4")
}

application {
    mainClass.set("top.mrxiaom.kritor.adapter.onebot.standalone.MainKt")
}
