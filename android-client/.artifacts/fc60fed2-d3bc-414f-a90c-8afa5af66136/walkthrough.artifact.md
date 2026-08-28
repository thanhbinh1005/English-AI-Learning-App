# Walkthrough: UI Localization to Vietnamese

I have localized the "New Collection" and "Translate" screens to Vietnamese, including all user-facing labels, hints, language names, and status messages.

## Changes Made

### New Collection Screen
- **[activity_new_collection.xml](file:///D:/HaUI/Phat_trien_UDDD/English-AI-Learning-App/android-client/app/src/main/res/layout/activity_new_collection.xml)**:
    - Translated header: "Bộ sưu tập mới".
    - Translated design labels and hints.
    - Updated button: "Tạo bộ sưu tập".

### Translate Screen
- **[fragment_translate.xml](file:///D:/HaUI/Phat_trien_UDDD/English-AI-Learning-App/android-client/app/src/main/res/layout/fragment_translate.xml)**:
    - Localized default language selectors: "Nhận diện ngôn ngữ" and "Tiếng Việt".
    - Translated input hint and history header.
    - Updated button: "Dịch thuật".
- **[TranslateFragment.java](file:///D:/HaUI/Phat_trien_UDDD/English-AI-Learning-App/android-client/app/src/main/java/com/thanhbinh/englishaiapp/ui/fragment/TranslateFragment.java)**:
    - Updated language mapping logic to use Vietnamese names (e.g., "Tiếng Anh", "Tiếng Pháp").
    - Localized all status messages: "Đang xử lý...", "Lỗi:", "Đã sao chép vào bộ nhớ tạm".
    - Updated Speech-to-Text prompt and error messages.
- **[bottom_sheet_languages.xml](file:///D:/HaUI/Phat_trien_UDDD/English-AI-Learning-App/android-client/app/src/main/res/layout/bottom_sheet_languages.xml)**:
    - Translated title to "Chọn ngôn ngữ".

## Verification Results

### Automated Tests
- Ran `app:assembleDebug`: **Build successful**.

### Manual Verification
- All UI elements in the target screens now display Vietnamese text correctly.
- Language selection and translation logic correctly handle the new Vietnamese language names.
- Error and success Toasts are now in Vietnamese.
