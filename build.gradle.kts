plugins {
    kotlin("jvm") version "2.2.21"
    id("com.gradleup.shadow") version "9.4.1"
    id("xyz.jpenilla.run-paper") version "3.0.2"
    jacoco
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    testImplementation("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.110.0")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        configurations = listOf(project.configurations.runtimeClasspath.get())
        mergeServiceFiles()
    }

    jar {
        enabled = false
    }

    assemble {
        dependsOn(shadowJar)
    }

    build {
        dependsOn(shadowJar)
    }

    runServer {
        minecraftVersion("1.21.11")
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    processResources {
        val props = mapOf("version" to version)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    test {
        useJUnitPlatform()
        finalizedBy(jacocoTestReport)
    }

    jacocoTestReport {
        dependsOn(test)
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }

    jacocoTestCoverageVerification {
        dependsOn(test)
        violationRules {
            rule {
                limit {
                    minimum = "0.35".toBigDecimal()
                }
            }
        }
    }

    check {
        dependsOn(jacocoTestCoverageVerification)
    }
}
