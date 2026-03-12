# maven-central-publisher

Minimalist Gradle plugin for publishing to Sonatype Maven Central, paired with a reusable GitHub Actions workflow.

## Gradle Plugin Setup

### 1. Apply the plugin

In your module's `build.gradle.kts`:

```kotlin
plugins {
    id("com.isycat.publishing") version "<version>"
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
jobs:
  publish:
    uses: isycat/maven-central-publisher/.github/workflows/publish.yml@main
    with:
      version: "1.2.3"
      ref: "main"
    secrets:
      CENTRAL_USERNAME: ${{ secrets.CENTRAL_USERNAME }}
      CENTRAL_PASSWORD: ${{ secrets.CENTRAL_PASSWORD }}
      GPG_PRIVATE_KEY: ${{ secrets.GPG_PRIVATE_KEY }}
      GPG_PASSPHRASE: ${{ secrets.GPG_PASSPHRASE }}
```

### Multi-module project (publish a specific submodule)

Pass the `module` input with the Gradle subproject name:

```yaml
jobs:
  publish:
    uses: isycat/maven-central-publisher/.github/workflows/publish.yml@main
    with:
      version: "1.2.3"
      ref: "main"
      module: "my-library-core"   # runs :my-library-core:publishAllPublicationsToSonatypeCentralRepository
    secrets:
      CENTRAL_USERNAME: ${{ secrets.CENTRAL_USERNAME }}
      CENTRAL_PASSWORD: ${{ secrets.CENTRAL_PASSWORD }}
      GPG_PRIVATE_KEY: ${{ secrets.GPG_PRIVATE_KEY }}
      GPG_PASSPHRASE: ${{ secrets.GPG_PASSPHRASE }}
```

### Workflow inputs

| Input | Required | Description |
|---|---|---|
| `version` | ✅ | Version string (e.g. `1.2.3` or `release-1.2.3`; the `release-` prefix is stripped automatically) |
| `ref` | ✅ | Git ref (branch, tag, or SHA) to check out |
| `module` | ❌ | Gradle subproject name. When omitted, tasks run on the root project |

### Triggering automatically on release tags

```yaml
on:
  push:
    tags:
      - 'release-[0-9]+\.[0-9]+\.[0-9]+'

jobs:
  publish:
    uses: isycat/maven-central-publisher/.github/workflows/publish.yml@main
    with:
      version: ${{ github.ref_name }}
      ref: ${{ github.ref_name }}
    secrets: inherit
```
