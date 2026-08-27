package com.thanhbinh.englishaiapp.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TranslationHelper {

    private static final String PLACEHOLDER_PREFIX = "__PROP_NAME_";
    private static final String PLACEHOLDER_SUFFIX = "__";

    // Regex pattern for Vietnamese introducing name sentences
    // Matches phrases like: "Tôi tên là Giang", "Tên tôi là Công", "Mình tên là Đỗ Hữu Công", "Anh tên là Sơn", "Em tên là Ngọc"
    private static final Pattern VI_NAME_INTRO_PATTERN = Pattern.compile(
        "(?i)^\\s*(tôi tên là|tên tôi là|mình tên là|anh tên là|chị tên là|em tên là|tên của tôi là|tôi tên|tên tôi)\\s+([A-ZÀÁẢÃẠĂẮẰẲẴẶÂẤẦẨẪẬĐÈÉẺẼẸÊẾỀỂỄỆÌÍỈĨỊÒÓỎÕỌÔỐỒỔỖỘƠỚỜởỠỢÙÚỦŨỤƯỨỪỬỮỰỲÝỶỸỴ][a-zàáảãạăắằẳẵặâấầuẩẫậnđèéẻẽẹêếềểễệìíỉĩịòóỏõọôốồổỗộơớờởỡợùúủũụưứừửữựỳýỷỹỵ\\s]*)$"
    );

    public static class PreprocessResult {
        public String processedText;
        public String extractedName;
        public boolean isNameIntro;

        public PreprocessResult(String processedText, String extractedName, boolean isNameIntro) {
            this.processedText = processedText;
            this.extractedName = extractedName;
            this.isNameIntro = isNameIntro;
        }
    }

    /**
     * Preprocesses input string before feeding into ML Kit translator.
     * If translating from Vietnamese to English and matches a name introduction pattern,
     * protects the name with a placeholder to avoid mistranslations (e.g. "Công" -> "public", "Giang" -> "Jiang").
     */
    public static PreprocessResult preprocessInput(String text, String sourceLangCode, String targetLangCode) {
        if (text == null || text.trim().isEmpty()) {
            return new PreprocessResult(text, null, false);
        }

        // Apply Vietnamese -> English specific name protection
        if ("vi".equalsIgnoreCase(sourceLangCode) && "en".equalsIgnoreCase(targetLangCode)) {
            Matcher matcher = VI_NAME_INTRO_PATTERN.matcher(text.trim());
            if (matcher.find()) {
                String introPhrase = matcher.group(1);
                String nameGroup = matcher.group(2);
                if (nameGroup != null) {
                    String name = nameGroup.trim();
                    if (!name.isEmpty()) {
                        String processed = introPhrase + " " + PLACEHOLDER_PREFIX + "0" + PLACEHOLDER_SUFFIX;
                        return new PreprocessResult(processed, name, true);
                    }
                }
            }
        }

        return new PreprocessResult(text, null, false);
    }

    /**
     * Postprocesses translated result from ML Kit.
     * Fixes ML Kit grammar errors (e.g., "I named" -> "My name is") and restores protected name.
     */
    public static String postprocessOutput(String translatedText, PreprocessResult preprocessResult, String sourceLangCode, String targetLangCode) {
        if (translatedText == null) return "";

        String result = translatedText.trim();

        if (preprocessResult != null && preprocessResult.isNameIntro && preprocessResult.extractedName != null) {
            // Fix grammar issues common in ML Kit (e.g. "I named __PROP_NAME_0__" -> "My name is __PROP_NAME_0__")
            if (result.matches("(?i).*I named.*") || result.matches("(?i).*I am named.*") || result.matches("(?i).*My name.*")) {
                result = result.replaceAll("(?i)^I named\\b", "My name is");
                result = result.replaceAll("(?i)^I am named\\b", "My name is");
            }
            
            // In case ML Kit output didn't retain "My name is" template properly
            if (!result.toLowerCase().startsWith("my name is") && !result.toLowerCase().startsWith("i am")) {
                result = "My name is " + PLACEHOLDER_PREFIX + "0" + PLACEHOLDER_SUFFIX;
            }

            // Restore extracted name
            String placeholder = PLACEHOLDER_PREFIX + "0" + PLACEHOLDER_SUFFIX;
            if (result.contains(placeholder)) {
                result = result.replace(placeholder, preprocessResult.extractedName);
            } else {
                // Fallback if placeholder was dropped by ML Kit
                result = "My name is " + preprocessResult.extractedName;
            }
        }

        return result;
    }
}
