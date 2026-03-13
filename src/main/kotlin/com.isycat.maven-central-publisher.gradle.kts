import com.isycat.publishing.MavenLicense

plugins {
    `java-library`
    `maven-publish`
    signing
}

class MavenLicenseImpl : MavenLicense {
    override var name: String = ""
    override var url: String = ""
}

abstract class MavenCentralPublishingExtensionImpl : com.isycat.publishing.MavenCentralPublishingExtension {
    override var groupId: String = ""
    override var artifactId: String = ""
    override var name: String = ""
    override var description: String = ""
    override var url: String = ""
    override var developerId: String = ""

    val licenseMetadata = MavenLicenseImpl()

    override fun license(action: Action<in MavenLicense>) {
        action.execute(licenseMetadata)
    }
}

val extension = extensions.create("mavenCentralPublishing", MavenCentralPublishingExtensionImpl::class.java)

// Set conventions for automatic derivation
extension.scmUrl.convention(
    project.provider {
        "scm:git:git://${extension.url.removePrefix("https://")}.git"
    },
)
extension.scmDevUrl.convention(
    project.provider {
        "scm:git:ssh://${extension.url.removePrefix("https://")}.git"
    },
)
extension.developerName.convention(project.provider { extension.developerId })

java {
    withSourcesJar()
    withJavadocJar()
}

configure<PublishingExtension> {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }

    afterEvaluate {
        // Validate required properties
        val missing = mutableListOf<String>()
        if (extension.groupId.isEmpty()) missing.add("groupId")
        if (extension.artifactId.isEmpty()) missing.add("artifactId")
        if (extension.name.isEmpty()) missing.add("name")
        if (extension.description.isEmpty()) missing.add("description")
        if (extension.url.isEmpty()) missing.add("url")
        if (extension.developerId.isEmpty()) missing.add("developerId")

        val metadata = extension.licenseMetadata
        if (metadata.name.isEmpty()) missing.add("license.name")
        if (metadata.url.isEmpty()) missing.add("license.url")

        if (missing.isNotEmpty()) {
            throw GradleException(
                "mavenCentralPublishing { ... } is missing required properties in ${project.path}: ${
                    missing.joinToString(", ")
                }",
            )
        }

        // Set project identity for correctly mapped project dependencies in other modules' POMs
        project.group = extension.groupId
        configure<org.gradle.api.plugins.BasePluginExtension> {
            archivesName.set(extension.artifactId)
        }

        publications.withType<MavenPublication>().configureEach {
            // Set standard GAV for the main "maven" publication
            if (name == "maven") {
                artifactId = extension.artifactId
                groupId = extension.groupId
            }
            version = project.version.toString()

            pom {
                name.set(project.provider { extension.name })
                description.set(project.provider { extension.description })
                url.set(project.provider { extension.url })
                licenses {
                    license {
                        val licenseMetadata = extension.licenseMetadata
                        name.set(project.provider { licenseMetadata.name })
                        url.set(project.provider { licenseMetadata.url })
                    }
                }
                developers {
                    developer {
                        id.set(project.provider { extension.developerId })
                        name.set(extension.developerName)
                    }
                }
                scm {
                    connection.set(extension.scmUrl)
                    developerConnection.set(extension.scmDevUrl)
                    url.set(project.provider { extension.url })
                }
            }
        }

        val mavenPublication = publications.named<MavenPublication>("maven").get()
        configure<SigningExtension> {
            val gpgKey = project.findProperty("gpgKey")?.toString()
            val gpgPassphrase = project.findProperty("gpgPassphrase")?.toString()
            if (!gpgKey.isNullOrEmpty()) {
                useInMemoryPgpKeys(gpgKey, gpgPassphrase)
                sign(mavenPublication)
                // Also sign plugin markers if they exist
                publications.withType<MavenPublication>().configureEach {
                    if (name != "maven") {
                        sign(this)
                    }
                }
            }
        }

        // Ensure each publish task depends on its corresponding sign task (required when
        // java-gradle-plugin adds extra plugin marker publications whose publish tasks may not
        // auto-wire to the signing tasks created for them)
        tasks.withType<org.gradle.api.publish.maven.tasks.PublishToMavenRepository>().configureEach {
            val capitalizedPublicationName = publication.name.replaceFirstChar { it.uppercase() }
            dependsOn(tasks.matching { it.name == "sign${capitalizedPublicationName}Publication" })
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
