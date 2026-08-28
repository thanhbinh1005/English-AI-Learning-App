# Implementation Plan: Localize UI to Vietnamese

This plan covers the translation of all user-facing English strings in the "New Collection" and "Translate" screens into Vietnamese, including layouts and programmatic strings.

## User Review Required

> [!NOTE]
> I will be updating language names (English, French, etc.) to their Vietnamese equivalents (Tiếng Anh, Tiếng Pháp, etc.) in the translation logic. This ensures a fully localized experience.

## Proposed Changes

### UI Layouts

#### [MODIFY] [activity_new_collection.xml](file:///D:/HaUI/Phat_trien_UDDD/English-AI-Learning-App/android-client/app/src/main/res/layout/activity_new_collection.xml)
- Translate header: `New Collection` -> `Bộ sưu tập mới`
- Translate design section: `Design Your Folder` -> `Thiết kế bộ từ vựng`, `First, give your new...` -> `Trước tiên, hãy đặt tên và chọn màu sắc cho bộ từ vựng của bạn.`
- Translate form labels: `FOLDER NAME` -> `TÊN BỘ SƯU TẬP`, `DESCRIPTION (OPTIONAL)` -> `MÔ TẢ (KHÔNG BẮT BUỘC)`, `ACCENT COLOR` -> `MÀU SẮC ĐIỂM NHẤN`.
- Translate hints: `e.g., Business English 2024` -> `vd: Tiếng Anh công sở 2024`, `What will you learn...` -> `Bạn sẽ học gì trong bộ sưu tập này?`.
- Translate button: `Create Collection` -> `Tạo bộ sưu tập`.

#### [MODIFY] [fragment_translate.xml](file:///D:/HaUI/Phat_trien_UDDD/English-AI-Learning-App/android-client/app/src/main/res/layout/fragment_translate.xml)
- Translate language labels: `Detect Language` -> `Nhận diện ngôn ngữ`, `Vietnamese` -> `Tiếng Việt`.
- Translate input hint: `Type or paste text here...` -> `Nhập hoặc dán văn bản tại đây...`.
- Translate auto-detect badge: `Detected: English` -> `Đã nhận diện: Tiếng Anh`.
- Translate translate button: `Translate` -> `Dịch thuật`.
- Translate result placeholder: `Translation will appear here...` -> `Bản dịch sẽ xuất hiện tại đây...`.
- Translate history title: `Recent History` -> `Lịch sử dịch thuật`.

#### [MODIFY] [bottom_sheet_languages.xml](file:///D:/HaUI/Phat_trien_UDDD/English-AI-Learning-App/android-client/app/src/main/res/layout/bottom_sheet_languages.xml)
- Translate title: `Select Language` -> `Chọn ngôn ngữ`.

### Business Logic (Java)

#### [MODIFY] [TranslateFragment.java](file:///D:/HaUI/Phat_trien_UDDD/English-AI-Learning-App/android-client/app/src/main/java/com/thanhbinh/englishaiapp/ui/fragment/TranslateFragment.java)
- Update `getLanguageName` method to return Vietnamese names (e.g., `Vietnamese` -> `Tiếng Việt`).
- Update `showLanguageBottomSheet` to use Vietnamese language names in the list.
- Update `translateWithCode` and `translateLongTextWithTranslator` to use Vietnamese strings for status messages (`Processing...`, `Error:`, `Long Text`).
- Update `detectLanguage` and `setupActions` (Copy Toast, Speech prompt).

## Verification Plan

### Manual Verification
- **New Collection**: Open the screen and verify all labels, hints, and the button are in Vietnamese.
- **Translate**:
    - Verify language names in the switcher are "Nhận diện ngôn ngữ" and "Tiếng Việt".
    - Open language selector and verify the list contains "Tiếng Anh", "Tiếng Pháp", etc.
    - Input text and verify the "Dịch thuật" button and status messages.
    - Check the history section title.
