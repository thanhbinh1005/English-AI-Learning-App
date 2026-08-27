package com.thanhbinh.englishaiapp;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.thanhbinh.englishaiapp.data.local.AppDatabase;
import com.thanhbinh.englishaiapp.data.local.entity.VocabularyEntity;
import com.thanhbinh.englishaiapp.databinding.ActivityFlashcardBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FlashcardActivity extends AppCompatActivity {

    private ActivityFlashcardBinding binding;
    private TextToSpeech tts;

    private long collectionId;
    private String collectionName;
    private List<VocabularyEntity> vocabList = new ArrayList<>();
    private int currentIndex = 0;
    private boolean isBackShowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityFlashcardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        collectionId = getIntent().getLongExtra("collection_id", -1);
        collectionName = getIntent().getStringExtra("collection_name");

        if (collectionId == -1) {
            finish();
            return;
        }

        initTTS();
        initViews();
        loadData();
    }

    private void initTTS() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });
    }

    private void initViews() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.txtFlashcardTitle.setText("Học Flashcard: " + (collectionName != null ? collectionName : ""));

        binding.cardFront.setOnClickListener(v -> flipCard());
        binding.cardBack.setOnClickListener(v -> flipCard());

        binding.btnSpeechFront.setOnClickListener(v -> speakCurrentTerm());
        binding.btnSpeechBack.setOnClickListener(v -> speakCurrentTerm());

        binding.btnUnlearned.setOnClickListener(v -> markCurrentStatus(false));
        binding.btnLearned.setOnClickListener(v -> markCurrentStatus(true));
    }

    private void loadData() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<VocabularyEntity> list = AppDatabase.getInstance(getApplicationContext())
                    .vocabularyDao().getVocabulariesSync(collectionId);
            runOnUiThread(() -> {
                if (list == null || list.isEmpty()) {
                    Toast.makeText(this, "Chưa có từ vựng nào để học!", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                vocabList = list;
                currentIndex = 0;
                showCard(currentIndex);
            });
        });
    }

    private void showCard(int index) {
        if (index < 0 || index >= vocabList.size()) {
            showCompletionDialog();
            return;
        }

        VocabularyEntity vocab = vocabList.get(index);
        binding.txtFrontTerm.setText(vocab.getTerm());
        binding.txtBackMeaning.setText(vocab.getMeaning());

        if (vocab.getExample() != null && !vocab.getExample().trim().isEmpty()) {
            binding.txtBackExample.setText("Ví dụ: " + vocab.getExample());
            binding.txtBackExample.setVisibility(View.VISIBLE);
        } else {
            binding.txtBackExample.setVisibility(View.GONE);
        }

        binding.txtCardCounter.setText("Thẻ " + (index + 1) + " / " + vocabList.size());
        int progress = (int) (((float) (index + 1) / vocabList.size()) * 100);
        binding.progressFlashcard.setProgress(progress);

        if (isBackShowing) {
            binding.cardBack.setVisibility(View.GONE);
            binding.cardFront.setVisibility(View.VISIBLE);
            isBackShowing = false;
        }
    }

    private void flipCard() {
        if (!isBackShowing) {
            binding.cardFront.animate().rotationY(90).setDuration(150).withEndAction(() -> {
                binding.cardFront.setVisibility(View.GONE);
                binding.cardFront.setRotationY(0);
                binding.cardBack.setVisibility(View.VISIBLE);
                binding.cardBack.setRotationY(-90);
                binding.cardBack.animate().rotationY(0).setDuration(150).start();
                isBackShowing = true;
            }).start();
        } else {
            binding.cardBack.animate().rotationY(90).setDuration(150).withEndAction(() -> {
                binding.cardBack.setVisibility(View.GONE);
                binding.cardBack.setRotationY(0);
                binding.cardFront.setVisibility(View.VISIBLE);
                binding.cardFront.setRotationY(-90);
                binding.cardFront.animate().rotationY(0).setDuration(150).start();
                isBackShowing = false;
            }).start();
        }
    }

    private void speakCurrentTerm() {
        if (tts != null && currentIndex < vocabList.size()) {
            String term = vocabList.get(currentIndex).getTerm();
            tts.speak(term, TextToSpeech.QUEUE_FLUSH, null, "tts_id");
        }
    }

    private void markCurrentStatus(boolean isLearned) {
        if (currentIndex >= vocabList.size()) return;

        VocabularyEntity vocab = vocabList.get(currentIndex);
        vocab.setLearned(isLearned);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase.getInstance(getApplicationContext()).vocabularyDao().updateLearnedStatus(vocab.getId(), isLearned);
        });

        currentIndex++;
        showCard(currentIndex);
    }

    private void showCompletionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("🎉 Hoàn thành lượt học!")
                .setMessage("Bạn đã xem hết tất cả các thẻ từ vựng trong bộ này. Tiếp tục ôn tập lại?")
                .setPositiveButton("Học lại từ đầu", (dialog, which) -> {
                    currentIndex = 0;
                    showCard(currentIndex);
                })
                .setNegativeButton("Thoát", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
