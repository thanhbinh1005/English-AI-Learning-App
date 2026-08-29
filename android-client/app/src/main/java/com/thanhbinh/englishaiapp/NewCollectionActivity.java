package com.thanhbinh.englishaiapp;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.thanhbinh.englishaiapp.data.local.AppDatabase;
import com.thanhbinh.englishaiapp.data.local.entity.CollectionEntity;
import com.thanhbinh.englishaiapp.databinding.ActivityNewCollectionBinding;

public class NewCollectionActivity extends AppCompatActivity {

    private ActivityNewCollectionBinding binding;

    private final String[] colors = new String[]{
            "#8EC5FC", // Blue
            "#81C784", // Green
            "#FF8A80", // Pink
            "#FFD54F", // Yellow
            "#B388FF"  // Purple
    };

    private View[] colorViews;
    private View[] wrapperViews;
    private String selectedColorHex = colors[0];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityNewCollectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
    }

    private void initViews() {
        binding.btnBack.setOnClickListener(v -> finish());

        colorViews = new View[]{
                binding.colorCircle1,
                binding.colorCircle2,
                binding.colorCircle3,
                binding.colorCircle4,
                binding.colorCircle5
        };

        wrapperViews = new View[]{
                binding.wrapperColor1,
                binding.wrapperColor2,
                binding.wrapperColor3,
                binding.wrapperColor4,
                binding.wrapperColor5
        };

        for (int i = 0; i < colors.length; i++) {
            final int index = i;
            GradientDrawable circleDrawable = new GradientDrawable();
            circleDrawable.setShape(GradientDrawable.OVAL);
            circleDrawable.setColor(Color.parseColor(colors[i]));
            colorViews[i].setBackground(circleDrawable);

            wrapperViews[i].setOnClickListener(v -> selectColor(index));
        }

        selectColor(0);

        binding.edtFolderName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSubmitButtonState(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.btnCreateCollection.setOnClickListener(v -> createCollection());
        updateSubmitButtonState(binding.edtFolderName.getText().toString().trim());
    }

    private void selectColor(int selectedIndex) {
        selectedColorHex = colors[selectedIndex];
        int accentColor = ContextCompat.getColor(this, R.color.app_accent);
        for (int i = 0; i < colors.length; i++) {
            if (i == selectedIndex) {
                GradientDrawable ringDrawable = new GradientDrawable();
                ringDrawable.setShape(GradientDrawable.OVAL);
                ringDrawable.setColor(Color.TRANSPARENT);
                ringDrawable.setStroke(8, accentColor);
                wrapperViews[i].setBackground(ringDrawable);
            } else {
                wrapperViews[i].setBackground(null);
            }
        }
    }

    private void updateSubmitButtonState(String folderName) {
        boolean isValid = !folderName.isEmpty();
        binding.btnCreateCollection.setEnabled(isValid);
        if (isValid) {
            int activeColor = ContextCompat.getColor(this, R.color.app_accent);
            binding.btnCreateCollection.setBackgroundTintList(ColorStateList.valueOf(activeColor));
            binding.btnCreateCollection.setTextColor(Color.WHITE);
        } else {
            int disabledColor = ContextCompat.getColor(this, R.color.app_input_hint);
            binding.btnCreateCollection.setBackgroundTintList(ColorStateList.valueOf(disabledColor));
            binding.btnCreateCollection.setTextColor(Color.parseColor("#E0E0E0"));
        }
    }

    private void createCollection() {
        String name = binding.edtFolderName.getText().toString().trim();
        String description = binding.edtFolderDescription.getText().toString().trim();

        if (name.isEmpty()) return;

        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Kiểm tra trùng tên bộ sưu tập
            CollectionEntity existing = AppDatabase.getInstance(getApplicationContext()).collectionDao().getCollectionByName(name);
            if (existing != null) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Tên bộ sưu tập đã tồn tại. Vui lòng chọn tên khác.", Toast.LENGTH_LONG).show();
                });
                return;
            }

            CollectionEntity collection = new CollectionEntity(name, description, selectedColorHex, System.currentTimeMillis());
            AppDatabase.getInstance(getApplicationContext()).collectionDao().insert(collection);
            runOnUiThread(() -> {
                Toast.makeText(this, "Đã tạo bộ sưu tập mới!", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
