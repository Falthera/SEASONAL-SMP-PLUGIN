# Release Process

This document describes how to create a new release of SeasonalSMP using the automated GitHub Actions pipeline.

## Prerequisites

- Maintainer access to the repository
- All tests passing on the `main`/`master` branch
- Updated `CHANGELOG.md` with release notes under the new version heading

## Creating a Release

### Option 1: Tag Push (Recommended)

1. Update the version in `pom.xml` if not already updated by CI:
   ```bash
   mvn versions:set -DnewVersion=1.0.1 -DgenerateBackupPoms=false
   mvn versions:commit
   ```

2. Commit the version bump:
   ```bash
   git add pom.xml
   git commit -m "chore: bump version to 1.0.1"
   ```

3. Create and push a version tag:
   ```bash
   git tag v1.0.1
   git push origin v1.0.1
   ```

4. GitHub Actions will automatically:
   - Build the plugin with Maven
   - Run verification checks
   - Generate SHA256 checksums
   - Extract release notes from `CHANGELOG.md`
   - Create a GitHub Release with the compiled JAR attached

### Option 2: Manual Workflow Dispatch

1. Go to **Actions** → **Build and Release** → **Run workflow**
2. Enter the version number (e.g. `1.0.1`)
3. Check **Mark this release as a pre-release** if applicable
4. Click **Run workflow**

## Pre-releases

Pre-releases are automatically detected when the tag contains a hyphen:
- `v1.0.1-rc1` → pre-release
- `v1.0.1` → stable release

You can also manually trigger a pre-release via workflow dispatch.

## Artifacts

Each release includes:
- `SeasonalSMP-<version>.jar` - The compiled plugin
- `SeasonalSMP-<version>.jar.sha256` - SHA256 checksum for verification

## Verification

After the release is published:
1. Download the JAR and checksum
2. Verify integrity:
   ```bash
   sha256sum -c SeasonalSMP-1.0.1.jar.sha256
   ```

## CI Pipeline

On every push to `main`/`master` and on all pull requests:
- Code style is checked with Checkstyle
- Tests are executed
- Build artifacts are verified
