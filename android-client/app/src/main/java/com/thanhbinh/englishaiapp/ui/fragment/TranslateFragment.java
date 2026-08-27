package com.thanhbinh.englishaiapp.ui.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.thanhbinh.englishaiapp.R;
import com.thanhbinh.englishaiapp.data.local.AppDatabase;
import com.thanhbinh.englishaiapp.data.local.entity.HistoryItem;
import com.thanhbinh.englishaiapp.databinding.FragmentTranslateBinding;
import com.thanhbinh.englishaiapp.ui.adapter.HistoryAdapter;
import com.thanhbinh.englishaiapp.ui.adapter.LanguageSelectionAdapter;
import com.thanhbinh.englishaiapp.utils.TranslationHelper;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentifier;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TranslateFragment extends Fragment {

    private FragmentTranslateBinding binding;
    private AppDatabase db;
    private HistoryAdapter adapter;
    private List<HistoryItem> historyList = new ArrayList<>();

    private static final int SPEECH_REQUEST_CODE = 100;
    private static final int MAX_CHUNK_SIZE = 2000;

    private Translator translator;
    private String lastSourceCode = "";
    private String lastTargetCode = "";

    private TextToSpeech tts;
    private LanguageIdentifier languageIdentifier;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTranslateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = AppDatabase.getInstance(requireContext());
        languageIdentifier = LanguageIdentification.getClient();
        initTTS();

        setupRecyclerView();
        setupLanguagePickers();
        setupActions();
        loadHistory();

        prepareTranslator();
    }

    private void initTTS() {
        tts = new TextToSpeech(requireContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new HistoryAdapter(historyList, new HistoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(HistoryItem item) {
                if (item == null) return;
                if (item.sourceText != null) {
                    binding.etInput.setText(item.sourceText);
                    binding.etInput.setSelection(item.sourceText.length());
                }
                if (item.sourceLang != null && !item.sourceLang.isEmpty()) {
                    binding.tvSourceLang.setText(item.sourceLang);
                }
                if (item.targetLang != null && !item.targetLang.isEmpty()) {
                    binding.tvTargetLang.setText(item.targetLang);
                }
                if (item.translatedText != null && !item.translatedText.isEmpty()) {
                    binding.tvResult.setText(item.translatedText);
                }
                prepareTranslator();
            }

            @Override
            public void onFavoriteClick(HistoryItem item) {
                item.isFavorite = !item.isFavorite;
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    db.translationHistoryDao().update(item);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> loadHistory());
                    }
                });
            }
        });
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvHistory.setAdapter(adapter);
    }

    private void loadHistory() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<HistoryItem> items = db.translationHistoryDao().getAll();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    historyList = items;
                    adapter.updateData(historyList);
                });
            }
        });
    }

    private void setupLanguagePickers() {
        binding.tvSourceLang.setOnClickListener(v -> showLanguageBottomSheet(true));
        binding.tvTargetLang.setOnClickListener(v -> showLanguageBottomSheet(false));

        binding.btnSwapLang.setOnClickListener(v -> {
            String source = binding.tvSourceLang.getText().toString();
            String target = binding.tvTargetLang.getText().toString();
            if (source.equals("Detect Language")) return;

            binding.tvSourceLang.setText(target);
            binding.tvTargetLang.setText(source);
            prepareTranslator();
        });
    }

    private void showLanguageBottomSheet(boolean isSource) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_languages, null);
        RecyclerView rv = view.findViewById(R.id.rvLanguages);

        List<String> langs = new ArrayList<>();
        if (isSource) langs.add("Detect Language");
        langs.add("English");
        langs.add("Vietnamese");
        langs.add("French");
        langs.add("Japanese");
        langs.add("German");
        langs.add("Spanish");

        LanguageSelectionAdapter langAdapter = new LanguageSelectionAdapter(langs, lang -> {
            if (isSource) {
                binding.tvSourceLang.setText(lang);
            } else {
                binding.tvTargetLang.setText(lang);
            }
            prepareTranslator();
            dialog.dismiss();
        });

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(langAdapter);
        dialog.setContentView(view);
        dialog.show();
    }

    private void setupActions() {
        binding.btnTranslate.setOnClickListener(v -> translateText());

        binding.btnCopy.setOnClickListener(v -> {
            String result = binding.tvResult.getText().toString();
            if (!result.isEmpty() && !result.equals("Translation will appear here...")) {
                ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Translated Text", result);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(requireContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.btnMic.setOnClickListener(v -> startSpeechToText());

        binding.btnClear.setOnClickListener(v -> binding.etInput.setText(""));

        binding.btnSpeaker.setOnClickListener(v -> {
            String text = binding.tvResult.getText().toString();
            if (!text.isEmpty()) {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });

        binding.etInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.btnClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                if (s.length() > 5 && binding.tvSourceLang.getText().toString().equals("Detect Language")) {
                    detectLanguage(s.toString());
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void detectLanguage(String text) {
        languageIdentifier.identifyLanguage(text)
                .addOnSuccessListener(languageCode -> {
                    if (!languageCode.equals("und")) {
                        binding.tvAutoDetectBadge.setVisibility(View.VISIBLE);
                        binding.tvAutoDetectBadge.setText("Detected: " + getLanguageName(languageCode));
                    }
                });
    }

    private void startSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Listening...");
        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Speech-to-Text not supported", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SPEECH_REQUEST_CODE && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                binding.etInput.setText(result.get(0));
            }
        }
    }

    private synchronized void prepareTranslator() {
        String sourceLangName = binding.tvSourceLang.getText().toString();
        String targetLangName = binding.tvTargetLang.getText().toString();

        String sourceCode = getLanguageCode(sourceLangName);
        String targetCode = getLanguageCode(targetLangName);

        if (sourceCode == null || targetCode == null) return;

        if (translator != null && sourceCode.equals(lastSourceCode) && targetCode.equals(lastTargetCode)) {
            return;
        }

        if (translator != null) {
            translator.close();
        }

        lastSourceCode = sourceCode;
        lastTargetCode = targetCode;

        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(sourceCode)
                .setTargetLanguage(targetCode)
                .build();

        translator = Translation.getClient(options);
        DownloadConditions conditions = new DownloadConditions.Builder().build();
        translator.downloadModelIfNeeded(conditions);
    }

    private void translateText() {
        String input = binding.etInput.getText().toString().trim();
        if (input.isEmpty()) return;

        String sourceName = binding.tvSourceLang.getText().toString();
        if (sourceName.equals("Detect Language")) {
            languageIdentifier.identifyLanguage(input)
                    .addOnSuccessListener(languageCode -> {
                        if (!languageCode.equals("und")) {
                            translateWithCode(input, languageCode);
                        } else {
                            translateWithCode(input, TranslateLanguage.ENGLISH);
                        }
                    });
        } else {
            translateWithCode(input, getLanguageCode(sourceName));
        }
    }

    private void translateWithCode(String input, String sourceCode) {
        String targetCode = getLanguageCode(binding.tvTargetLang.getText().toString());
        if (sourceCode == null || targetCode == null) return;

        TranslationHelper.PreprocessResult prepResult = TranslationHelper.preprocessInput(input, sourceCode, targetCode);
        String textToTranslate = prepResult.processedText;

        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(sourceCode)
                .setTargetLanguage(targetCode)
                .build();

        final Translator t = Translation.getClient(options);
        binding.btnTranslate.setEnabled(false);
        binding.tvResult.setText("Processing...");

        t.downloadModelIfNeeded(new DownloadConditions.Builder().build())
                .addOnSuccessListener(unused -> {
                    if (textToTranslate.length() > MAX_CHUNK_SIZE) {
                        translateLongTextWithTranslator(t, input, prepResult);
                    } else {
                        t.translate(textToTranslate).addOnSuccessListener(res -> {
                            String finalResult = TranslationHelper.postprocessOutput(res, prepResult, sourceCode, targetCode);
                            binding.tvResult.setText(finalResult);
                            binding.btnTranslate.setEnabled(true);
                            saveToHistory(input, finalResult, getLanguageName(sourceCode), binding.tvTargetLang.getText().toString());
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    binding.tvResult.setText("Error: " + e.getMessage());
                    binding.btnTranslate.setEnabled(true);
                });
    }

    private void translateLongTextWithTranslator(Translator t, String fullText, TranslationHelper.PreprocessResult prepResult) {
        String textToTranslate = prepResult.processedText;
        List<String> chunks = splitIntoSentences(textToTranslate);
        List<Task<String>> tasks = new ArrayList<>();
        for (String chunk : chunks) tasks.add(t.translate(chunk));

        Tasks.whenAllSuccess(tasks).addOnSuccessListener(results -> {
            StringBuilder sb = new StringBuilder();
            for (Object r : results) sb.append((String) r).append(" ");
            String rawResult = sb.toString().trim();
            String sourceName = binding.tvSourceLang.getText().toString();
            String targetName = binding.tvTargetLang.getText().toString();
            String finalResult = TranslationHelper.postprocessOutput(rawResult, prepResult, getLanguageCode(sourceName), getLanguageCode(targetName));
            binding.tvResult.setText(finalResult);
            binding.btnTranslate.setEnabled(true);
            saveToHistory(fullText, finalResult, "Long Text", targetName);
        });
    }

    private List<String> splitIntoSentences(String text) {
        List<String> sentences = new ArrayList<>();
        String[] split = text.split("(?<=[.!?])\\s+");
        StringBuilder current = new StringBuilder();
        for (String s : split) {
            if (current.length() + s.length() > MAX_CHUNK_SIZE) {
                sentences.add(current.toString());
                current = new StringBuilder(s);
            } else {
                current.append(s).append(" ");
            }
        }
        if (current.length() > 0) sentences.add(current.toString());
        return sentences;
    }

    private String getLanguageCode(String name) {
        switch (name) {
            case "Vietnamese": return TranslateLanguage.VIETNAMESE;
            case "French": return TranslateLanguage.FRENCH;
            case "Japanese": return TranslateLanguage.JAPANESE;
            case "German": return TranslateLanguage.GERMAN;
            case "Spanish": return TranslateLanguage.SPANISH;
            case "English": return TranslateLanguage.ENGLISH;
            default: return null;
        }
    }

    private String getLanguageName(String code) {
        switch (code) {
            case TranslateLanguage.VIETNAMESE: return "Vietnamese";
            case TranslateLanguage.FRENCH: return "French";
            case TranslateLanguage.JAPANESE: return "Japanese";
            case TranslateLanguage.GERMAN: return "German";
            case TranslateLanguage.SPANISH: return "Spanish";
            case TranslateLanguage.ENGLISH: return "English";
            default: return code;
        }
    }

    private void saveToHistory(String source, String result, String sLang, String tLang) {
        HistoryItem item = new HistoryItem(source, result, sLang, tLang);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.translationHistoryDao().insert(item);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> loadHistory());
            }
        });
    }

    @Override
    public void onDestroyView() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (translator != null) translator.close();
        super.onDestroyView();
        binding = null;
    }
}
