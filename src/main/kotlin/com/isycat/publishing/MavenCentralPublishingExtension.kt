package com.isycat.publishing

import org.gradle.api.Action
import org.gradle.api.provider.Property

interface MavenCentralPublishingExtension {
    /** REQUIRED: The Maven group ID (e.g., "com.isycat.ktox"). */
    var groupId: String

    /** REQUIRED: The Maven artifact ID (e.g., "ktox-core"). */
    var artifactId: String

    /** REQUIRED: The display name of the project (e.g., "ktox Core"). */
    var name: String

    /** REQUIRED: A brief description of the project. */
    var description: String

    /** REQUIRED: The project homepage URL. */
    var url: String

    /** REQUIRED: The ID of the developer (e.g., "isycat"). */
    var developerId: String

    /** REQUIRED: Configure the Maven license. */
    fun license(action: Action<in MavenLicense>) {}

    /** OPTIONAL: The SCM connection URL (derived from [url] if not set). */
    val scmUrl: Property<String>

    /** OPTIONAL: The SCM developer connection URL (derived from [url] if not set). */
    val scmDevUrl: Property<String>

    /** OPTIONAL: The name of the developer (defaults to [developerId] if not set). */
    val developerName: Property<String>
}

interface MavenLicense {
    /** REQUIRED: The name of the license (e.g., "Apache-2.0"). */
    var name: String

    /** REQUIRED: The URL of the license. */
    var url: String
}
