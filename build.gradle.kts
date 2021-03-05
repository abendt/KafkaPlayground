plugins {
    val kotlinVersion = "1.4.31"

    kotlin("jvm") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion

    id("org.springframework.boot") version "2.4.3"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"

    id("com.adarshr.test-logger") version "2.1.1"
}

repositories {
    jcenter()
    mavenCentral()
}

testlogger {
    val isIdea = System.getProperty("idea.version") != null
    setTheme(if (isIdea) "plain" else "mocha")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testImplementation(kotlin("reflect"))

    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("org.springframework.kafka:spring-kafka:2.6.6")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    runtimeOnly("ch.qos.logback:logback-classic:1.2.3")
    runtimeOnly("ch.qos.logback:logback-core:1.2.3")
    implementation("org.codehaus.janino:janino:3.1.3")
    implementation("io.github.microutils:kotlin-logging:2.0.4")
    implementation("org.slf4j:log4j-over-slf4j:1.7.30")

    // Achtung: nach Änderungen der JUnit5 Versionen unbedingt prüfen,
    // ob JUnit5 _und_ JUnit4 Tests noch ausgeführt werden!
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    testImplementation("org.awaitility:awaitility:4.0.3")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "commons-logging")
    }

    testImplementation( "org.testcontainers:testcontainers:1.15.2") {
        exclude(group = "junit")
    }

    testImplementation( "org.testcontainers:kafka:1.15.2")
    testImplementation( "org.testcontainers:junit-jupiter:1.15.2")

    testImplementation("io.mockk:mockk:1.10.6")
    testImplementation("io.strikt:strikt-core:0.29.0")
    testImplementation("com.ninja-squad:springmockk:3.0.1")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = listOf("-Xjsr305=strict")
        apiVersion = "1.4"
        languageVersion = "1.4"
        useIR = true
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
