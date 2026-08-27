package com.thanhbinh.englishaiapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.thanhbinh.englishaiapp.data.local.AppDatabase;
import com.thanhbinh.englishaiapp.data.local.entity.VocabularyEntity;
import com.thanhbinh.englishaiapp.databinding.ActivityCollectionDetailBinding;
import com.thanhbinh.englishaiapp.ui.adapter.VocabularyAdapter;

import java.util.List;
import java.util.Locale;

public class CollectionDetailActivity extends AppCompatActivity implements VocabularyAdapter.OnVocabularyClickListener {

    private ActivityCollectionDetailBinding binding;
    private VocabularyAdapter adapter;
    private TextToSpeech tts;

    private long collectionId;
    private String collectionName;
    private String collectionColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityCollectionDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        collectionId = getIntent().getLongExtra("collection_id", -1);
        collectionName = getIntent().getStringExtra("collection_name");
        collectionColor = getIntent().getStringExtra("collection_color");

        if (collectionId == -1) {
            finish();
            return;
        }

        initTTS();
        initViews();
        observeData("");
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
        binding.txtCollectionTitle.setText(collectionName);

        if (collectionColor != null) {
            try {
                binding.accentLine.setBackgroundColor(Color.parseColor(collectionColor));
            } catch (Exception ignored) {}
        }

        adapter = new VocabularyAdapter(this);
        binding.rvVocabularies.setLayoutManager(new LinearLayoutManager(this));
        binding.rvVocabularies.setAdapter(adapter);

        binding.btnStudyFlashcard.setOnClickListener(v -> {
            Intent intent = new Intent(this, FlashcardActivity.class);
            intent.putExtra("collection_id", collectionId);
            intent.putExtra("collection_name", collectionName);
            startActivity(intent);
        });

        binding.edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                observeData(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.fabAddWord.setOnClickListener(v -> showAddWordDialog());
    }

    private void observeData(String query) {
        AppDatabase db = AppDatabase.getInstance(this);
        if (query.isEmpty()) {
            db.vocabularyDao().getVocabulariesByCollectionId(collectionId)
                    .observe(this, this::updateVocabList);
        } else {
            db.vocabularyDao().searchVocabularies(collectionId, query)
                    .observe(this, this::updateVocabList);
        }
    }

    private void updateVocabList(List<VocabularyEntity> vocabularies) {
        adapter.setVocabularies(vocabularies);
        if (vocabularies == null || vocabularies.isEmpty()) {
            binding.layoutEmpty.setVisibility(View.VISIBLE);
            binding.rvVocabularies.setVisibility(View.GONE);
        } else {
            binding.layoutEmpty.setVisibility(View.GONE);
            binding.rvVocabularies.setVisibility(View.VISIBLE);
        }
    }

    private void showAddWordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm từ vựng mới");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_word, null);
        EditText edtTerm = dialogView.findViewById(R.id.edtTerm);
        EditText edtMeaning = dialogView.findViewById(R.id.edtMeaning);
        EditText edtExample = dialogView.findViewById(R.id.edtExample);

        builder.setView(dialogView);
        builder.setPositiveButton("Thêm", (dialog, which) -> {
            String term = edtTerm.getText().toString().trim();
            String meaning = edtMeaning.getText().toString().trim();
            String example = edtExample.getText().toString().trim();

            if (term.isEmpty() || meaning.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập Từ và Nghĩa!", Toast.LENGTH_SHORT).show();
                return;
            }

            VocabularyEntity vocabulary = new VocabularyEntity(collectionId, term, meaning, example, false, System.currentTimeMillis());
            AppDatabase.databaseWriteExecutor.execute(() -> {
                AppDatabase.getInstance(getApplicationContext()).vocabularyDao().insert(vocabulary);
                runOnUiThread(() -> Toast.makeText(this, "Đã thêm từ vựng mới", Toast.LENGTH_SHORT).show());
            });
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    @Override
    public void onDeleteClick(VocabularyEntity vocabulary) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa từ vựng")
                .setMessage("Bạn có chắc muốn xóa từ \"" + vocabulary.getTerm() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        AppDatabase.getInstance(getApplicationContext()).vocabularyDao().delete(vocabulary);
                        runOnUiThread(() -> Toast.makeText(this, "Đã xóa từ vựng", Toast.LENGTH_SHORT).show());
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onSpeechClick(VocabularyEntity vocabulary) {
        if (tts != null && vocabulary.getTerm() != null) {
            tts.speak(vocabulary.getTerm(), TextToSpeech.QUEUE_FLUSH, null, "tts_id");
        }
    }

    @Override
    public void onLearnedToggle(VocabularyEntity vocabulary, boolean isLearned) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase.getInstance(getApplicationContext())
                    .vocabularyDao().updateLearnedStatus(vocabulary.getId(), isLearned);
        });
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
