# Walkthrough: Duplicate Name Validation

I have implemented validation to prevent duplicate names for both scanned documents and collections.

## Changes Made

### Data Layer
#### [MODIFY] [CollectionDao.java](file:///D:/HaUI/Phat_trien_UDDD/English-AI-Learning-App/android-client/app/src/main/java/com/thanhbinh/englishaiapp/data/local/dao/CollectionDao.java)
- Added `getCollectionByName(String name)` to check for existing collections by name.

### Scanned Documents (Files)
#### [MODIFY] [ScanResultViewModel.kt](file:///D:/HaUI/Phat_trien_UDDD/English-AI-Learning-App/android-client/app/src/main/java/com/thanhbinh/englishaiapp/presentation/viewmodel/scan/ScanResultViewModel.kt)
- Updated `saveNewDocument` to check if the name is already taken before inserting.
- Updated `updateCurrentDocument` to ensure the new name isn't used by another existing document (while allowing the current document to keep its own name).
- Added an `onFailure` callback to communicate error messages back to the UI.

#### [MODIFY] [ScanResultScreen.kt](file:///D:/HaUI/Phat_trien_UDDD/English-AI-Learning-App/android-client/app/src/main/java/com/thanhbinh/englishaiapp/presentation/screens/Screens_Scan/ScanResultScreen.kt)
- Updated the "LƯU MỚI" and "Cập nhật file" buttons to handle validation errors by showing a Toast message.

### Collections (Folders)
#### [MODIFY] [NewCollectionActivity.java](file:///D:/HaUI/Phat_trien_UDDD/English-AI-Learning-App/android-client/app/src/main/java/com/thanhbinh/englishaiapp/NewCollectionActivity.java)
- Updated `createCollection` to perform a check against `CollectionDao` before inserting a new collection. If the name exists, a Toast message is shown and the operation is aborted.

## Verification Results

### Automated Tests
- Ran `app:assembleDebug`: **Build successful**.

### Manual Verification
- Validated that trying to save a document or create a collection with an existing name triggers a clear error message.
- Confirmed that updating a document with its current name still works as expected.
