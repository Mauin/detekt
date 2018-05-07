import io.gitlab.arturbosch.detekt.detekt

plugins {
	id("java-library")
	id("io.gitlab.arturbosch.detekt")
}

repositories {
	jcenter()
	mavenLocal()
}

detekt {
	toolVersion = "1.0.0.RC6-MARVIN2"
}
