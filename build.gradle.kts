plugins {
	java
	id("org.springframework.boot") version "4.0.4"
	id("io.spring.dependency-management") version "1.1.7"

	id("com.diffplug.spotless") version "8.4.0"
	id("com.github.spotbugs") version "6.4.4"
	id("org.owasp.dependencycheck") version "12.2.0"
}

group = "com.mailnest"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(24)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:testcontainers-junit-jupiter")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

spotless {
	java {
		target("src/*/java/**/*.java")
		googleJavaFormat()
	}
}

spotbugs {
	ignoreFailures = false
}

tasks.withType<com.github.spotbugs.snom.SpotBugsTask> {
	reports {
		create("html") {
			required.set(true)
		}
	}
}

dependencyCheck {
	failBuildOnCVSS = 7.0f
}