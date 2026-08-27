# Plan: Validate Duplicate File and Collection Names

The user wants to prevent duplicate names for scanned documents (files) and collections (folders). This will improve data integrity and avoid confusion.

## Proposed Changes

### Data Layer (Room)

#### [MODIFY] [ScanDao.kt](file:///D:/HaUI/Phat_trien_UDDD/English-AI-Learning-App/android-client/app/src/main/java/com/thanhbinh/englishaiapp/data/local/dao/ScanDao.kt)
- (Already has `getDocByTitle`) Ensure it's sufficient or add a more specific existence check if needed.

#### [MODIFY] [CollectionDao.java](file:///D:/HaUI/Phat_trien_UDDD/English-AI-Learning-App/android-client/app/src/main/java/com/thanhbinh/englishaiapp/data/local/dao/CollectionDao.java)
- Add `@Query("SELECT * FROM collections WHERE name = :name LIMIT 1") CollectionEntity getCollectionByName(String name);`

### Business Logic (ViewModel / Activity)

#### [MODIFY] [ScanResultViewModel.kt](file:///D:/HaUI/Phat_trien_UDDD/English-AI-Learning-App/android-client/app/src/main/java/com/thanhbinh/englishaiapp/presentation/viewmodel/scan/ScanResultViewModel.kt)
- Update `saveNewDocument` to check if a document with the same `fileName` already exists.
- Update `updateCurrentDocument` to check if the new `fileName` is already taken by *another* document.
- Add an error callback or use a `StateFlow` to communicate validation errors to the UI.

#### [MODIFY] [NewCollectionActivity.java](file:///D:/HaUI/Phat_trien_UDDD/English-AI-Learning-App/android-client/app/src/main/java/com/thanhbinh/englishaiapp/NewCollectionActivity.java)
- In `createCollection()`, check if a collection with the same name already exists using `CollectionDao.getCollectionByName()`.
- Show a Toast and prevent creation if it exists.

### UI Layer (Compose / Views)

#### [MODIFY] [ScanResultScreen.kt](file:///D:/HaUI/Phat_trien_UDDD/English-AI-Learning-App/android-client/app/src/main/java/com/thanhbinh/englishaiapp/presentation/screens/Screens_Scan/ScanResultScreen.kt)
- Handle the validation feedback from the ViewModel (e.g., show a Toast or specific error UI).

## Verification Plan

### Manual Verification
- **Scanned Documents**:
    - Try to save a new document with a name that already exists. Verify an error message appears and the document is not saved.
    - Try to update an existing document to a name that belongs to another document. Verify an error message appears.
    - Update a document but keep its current name. Verify it updates successfully.
- **Collections**:
    - Try to create a new collection with a name that already exists. Verify a Toast appears saying the name is already taken.
