# maven-central-publisher

Minimalist Gradle plugin for publishing to Sonatype Maven Central, paired with a reusable GitHub Actions workflow.

## Gradle Plugin Setup

### 1. Apply the plugin

In your module's `build.gradle.kts`:

```kotlin
plugins {
    id("com.isycat.maven-central-publisher") version "<version>"
}
```

### 2. Configure publishing metadata

```kotlin
mavenCentralPublishing {
    groupId = "com.example"
    artifactId = "my-library"
    name = "My Library"
    description = "A brief description of the library."
    url = "https://github.com/example/my-library"
    developerId = "yourGithubHandle"
    // developerName is optional, defaults to developerId
    // scmUrl and scmDevUrl are optional, derived from url

    license {
        name = "Apache-2.0"
        url = "https://www.apache.org/licenses/LICENSE-2.0"
    }
}
```

### 3. Provide credentials

Pass credentials at publish time via Gradle properties (environment variables or `~/.gradle/gradle.properties`):

| Property | Description |
|---|---|
| `centralUsername` | Sonatype Central username |
| `centralPassword` | Sonatype Central password |
| `gpgKey` | ASCII-armored GPG private key |
| `gpgPassphrase` | GPG key passphrase |
| `version` | Artifact version to publish |

---

## GitHub Actions Workflow

The reusable workflow `.github/workflows/publish.yml` handles building, signing, uploading, and finalizing the deployment on Sonatype Central.

### Single-project (no submodules)

```yaml
name: Publish my thing

on:
  push:
    tags:
      - 'release-[0-9]+.[0-9]+.[0-9]+'
      - 'release-[0-9]+.[0-9]+.[0-9]+-SNAPSHOT'
  workflow_dispatch:
    inputs:
      version:
        description: 'Version to publish (e.g. 1.0.0)'
        required: true
        type: string
      commit:
        description: 'Commit SHA to publish from (optional, defaults to main)'
        required: false
        type: string

jobs:
  publish:
    uses: isycat/maven-central-publisher/.github/workflows/publish.yml@v1
    with:
      version: ${{ github.event_name == 'workflow_dispatch' && inputs.version || github.ref_name }}
      ref: ${{ github.event_name == 'workflow_dispatch' && (inputs.commit || 'main') || github.ref_name }}
    secrets: inherit
```

### Multi-module project (publish a specific submodule)

Pass the `module` input with the Gradle subproject name:

```yaml
name: Publish yamllint-maven-plugin

on:
  push:
    tags:
      - 'release-[0-9]+.[0-9]+.[0-9]+'
      - 'release-[0-9]+.[0-9]+.[0-9]+-SNAPSHOT'
  workflow_dispatch:
    inputs:
      version:
        description: 'Version to publish (e.g. 1.0.0)'
        required: true
        type: string
      commit:
        description: 'Commit SHA to publish from (optional, defaults to main)'
        required: false
        type: string

jobs:
  publish:
    uses: isycat/maven-central-publisher/.github/workflows/publish.yml@v1
    with:
      module: yamllint-maven-plugin
      version: ${{ github.event_name == 'workflow_dispatch' && inputs.version || github.ref_name }}
      ref: ${{ github.event_name == 'workflow_dispatch' && (inputs.commit || 'main') || github.ref_name }}
    secrets: inherit

```

### Workflow inputs

| Input | Required | Description |
|---|---|---|
| `version` | ✅ | Version string (e.g. `1.2.3` or `release-1.2.3`; the `release-` prefix is stripped automatically) |
| `ref` | ✅ | Git ref (branch, tag, or SHA) to check out |
| `module` | ❌ | Gradle subproject name. When omitted, tasks run on the root project |

