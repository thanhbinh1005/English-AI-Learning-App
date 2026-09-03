# Plan: Preserve 3 Document States (Original, Translation, AI Summary) on Save/Update

When saving or updating a scanned document, the app currently only saves the content of whichever tab is active, erasing the other 2 states. Furthermore, when loading a saved document, `translatedText` and `summaryText` are lost because `ScannedDocEntity` only holds a single `content` field.

This plan updates the Room database schema and ViewModel/UI logic so that all 3 states (**Nguyên văn**, **Dịch thuật**, and **Tóm tắt AI**) are saved and restored seamlessly.

## Proposed Changes

### Data Layer & Room Database

#### [MODIFY] [ScannedDocEntity.kt](file:///D:/HaUI/Phat_trien_UDDD/English-AI-Learning-App/android-client/app/src/main/java/com/thanhbinh/englishaiapp/data/local/entity/ScannedDocEntity.kt)
- Add two new fields to `ScannedDocEntity`:
  - `val translatedText: String = ""`
  - `val summaryText: String = ""`

#### [MODIFY] [AppDatabase.kt](file:///D:/HaUI/Phat_trien_UDDD/English-AI-Learning-App/android-client/app/src/main/java/com/thanhbinh/englishaiapp/data/local/AppDatabase.kt)
- Increment Room Database version from `7` to `8`.

---

### ViewModels

#### [MODIFY] [ScanResultViewModel.kt](file:///D:/HaUI/Phat_trien_UDDD/English-AI-Learning-App/android-client/app/src/main/java/com/thanhbinh/englishaiapp/presentation/viewmodel/scan/ScanResultViewModel.kt)
- Update `saveNewDocument` to accept `translatedText: String` and `summaryText: String`.
- Update `updateCurrentDocument` to accept `translatedText: String` and `summaryText: String` and copy them into `updatedDoc`.

---

### UI Layer

#### [MODIFY] [ScanResultScreen.kt](file:///D:/HaUI/Phat_trien_UDDD/English-AI-Learning-App/android-client/app/src/main/java/com/thanhbinh/englishaiapp/presentation/screens/Screens_Scan/ScanResultScreen.kt)
- When loading a document by `docId != 0`:
  - Set `textState = doc.content`
  - Set `translatedText = doc.translatedText`
  - Set `summaryText = doc.summaryText`
- When saving a new document ("LƯU MỚI"):
  - Pass `textState`, `translatedText`, and `summaryText` into `saveNewDocument`.
- When updating an existing document ("Cập nhật file"):
  - Pass `textState`, `translatedText`, and `summaryText` into `updateCurrentDocument`.

---

## Verification Plan

### Automated Verification
- Run `app:assembleDebug` via `gradle_build` to verify successful compilation.

### Manual Verification
- Open Scan screen, scan a document or open sample text.
- Switch to "Dịch thuật" tab and generate translation.
- Switch to "Tóm tắt AI" tab and generate summary.
- Click "LƯU MỚI".
- Open the newly saved file from "Bản quét của tôi".
- Click through all 3 tabs ("Nguyên bản", "Dịch thuật", "Tóm tắt AI") and verify that all 3 tabs contain their saved texts intact.
