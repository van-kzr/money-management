# Fix Gradle Sync Error: "Cannot add extension with name 'kotlin'"

The error occurs because the experimental `android.builtInKotlin` feature is enabled in `gradle.properties`. This feature registers a `kotlin` extension that conflicts with the standard Kotlin Gradle Plugin. Additionally, `builtInKotlin` is often incompatible with other Kotlin-based plugins like KSP and the Compose compiler plugin.

## Proposed Changes

### Build Configuration

#### [MODIFY] [gradle.properties](file:///home/vankzr/project/mobile/money%20management/gradle.properties)
- Disable `android.builtInKotlin` by setting it to `false` or removing it.

#### [MODIFY] [build.gradle.kts (root)](file:///home/vankzr/project/mobile/money%20management/build.gradle.kts)
- Add `alias(libs.plugins.jetbrains.kotlin) apply false` to the `plugins` block to manage the Kotlin plugin version centrally.

#### [MODIFY] [build.gradle.kts (app)](file:///home/vankzr/project/mobile/money%20management/app/build.gradle.kts)
- Add `alias(libs.plugins.jetbrains.kotlin)` to the `plugins` block to apply the Kotlin Android plugin.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:help` to verify that the project configuration phase completes successfully.
- Trigger a Gradle sync in the IDE.

### Manual Verification
- Verify that the "Cannot add extension with name 'kotlin'" error no longer appears during sync.
