plugins {
    id("java")
    id("application")
}

application {
    mainClass.set("be.kdg.integration3.Main")
}

group = "be.kdg.integration3"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.fazecast:jSerialComm:2.10.3")
}

tasks.test {
    useJUnitPlatform()
}