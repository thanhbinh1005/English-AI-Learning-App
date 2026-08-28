# Plan: Fix MainActivity and Theme Red Markers

The user reported errors in `MainActivity.kt`. Although the project builds successfully, the IDE/Linter is showing "red markers" (unresolved references) because the analyzer is failing to resolve symbols across different files in the same package (`com.thanhbinh.englishaiapp.ui.theme`).

## Proposed Changes

### UI / Theme

#### [MODIFY] [Theme.kt](file:///D:/HaUI/Phat_trien_UDDD/English-AI-Learning-App/android-client/app/src/main/java/com/thanhbinh/englishaiapp/ui/theme/Theme.kt)
- Move color definitions from `Color.kt` directly into `Theme.kt` to help the analyzer resolve them.
- Use `AppTypography` (already renamed from `Typography` to avoid class name shadowing).

#### [DELETE] [Color.kt](file:///D:/HaUI/Phat_trien_UDDD/English-AI-Learning-App/android-client/app/src/main/java/com/thanhbinh/englishaiapp/ui/theme/Color.kt)
- Redundant once colors are in `Theme.kt`.

#### [MODIFY] [MainActivity.kt](file:///D:/HaUI/Phat_trien_UDDD/English-AI-Learning-App/android-client/app/src/main/java/com/thanhbinh/englishaiapp/MainActivity.kt)
- Clean up redundant `MaterialTheme` nesting.
- Ensure standard Composable structure.

## Verification Plan

### Automated Tests
- Run `./gradlew app:assembleDebug` to ensure it still builds.
- Use `analyze_file` on `MainActivity.kt` and `Theme.kt` to verify that the "unresolved reference" errors are gone.

### Manual Verification
- Check the IDE for any remaining red markers.
