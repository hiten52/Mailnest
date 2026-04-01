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
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")

	implementation("org.springframework.boot:spring-boot-starter-flyway")
	runtimeOnly("org.flywaydb:flyway-database-postgresql")
	runtimeOnly("org.postgresql:postgresql")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-opentelemetry")

	implementation("commons-validator:commons-validator:1.9.0")
	testImplementation("net.jqwik:jqwik:1.8.4")

	implementation("org.springframework.boot:spring-boot-starter-webflux")
	testImplementation("org.wiremock:wiremock-standalone:3.9.1")

	implementation("org.springframework.security:spring-security-crypto")
}

tasks.withType<Test> {
	useJUnitPlatform()
	jvmArgs("-Duser.timezone=Asia/Kolkata")
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

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    jvmArgs = listOf("-Duser.timezone=Asia/Kolkata")
}