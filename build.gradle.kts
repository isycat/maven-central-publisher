plugins {
    `kotlin-dsl`
    `maven-publish`
    signing
}

group = "com.isycat.publishing"

repositories {
    mavenCentral()
}

java {
    withSourcesJar()
    withJavadocJar()
}

afterEvaluate {
    publishing {
        publications.withType<MavenPublication>().configureEach {
            version = project.version.toString()
            // The pluginMaven publication is auto-created by java-gradle-plugin (via kotlin-dsl)
            // with the correct artifactId derived from the project name.
            pom {
                name.set("Maven Central Publisher")
                description.set("Minimalist Gradle plugin for publishing to Sonatype Maven Central")
                url.set("https://github.com/isycat/maven-central-publisher")
                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                developers {
                    developer {
                        id.set("isycat")
                        name.set("isycat")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/isycat/maven-central-publisher.git")
                    developerConnection.set("scm:git:ssh://github.com/isycat/maven-central-publisher.git")
                    url.set("https://github.com/isycat/maven-central-publisher")
                }
            }
        }

        configure<SigningExtension> {
            val gpgKey = project.findProperty("gpgKey")?.toString()
            val gpgPassphrase = project.findProperty("gpgPassphrase")?.toString()
            if (!gpgKey.isNullOrEmpty()) {
                useInMemoryPgpKeys(gpgKey, gpgPassphrase)
                sign(*publications.toTypedArray())
            }
        }

        repositories {
            maven {
                name = "SonatypeCentral"
                url = uri(
                    "https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/",
                )
                credentials {
                    username = project.findProperty("centralUsername")?.toString()
                    password = project.findProperty("centralPassword")?.toString()
                }
            }
        }
    }
}
