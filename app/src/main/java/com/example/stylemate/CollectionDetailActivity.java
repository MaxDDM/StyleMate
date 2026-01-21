package com.example.stylemate;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class CollectionDetailActivity extends AppCompatActivity {

    // Храним текущий заголовок в переменной, чтобы его менять
    private String currentCollectionTitle = "";
    private TextView tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_detail);

        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnEdit = findViewById(R.id.btnEdit);
        ImageButton btnDelete = findViewById(R.id.btnDelete);
        tvTitle = findViewById(R.id.tvCollectionTitle);

        // Получаем название из Intent
        String titleFromIntent = getIntent().getStringExtra("COLLECTION_TITLE");
        if (titleFromIntent != null) {
            currentCollectionTitle = titleFromIntent;
            tvTitle.setText(currentCollectionTitle);
        }

        btnBack.setOnClickListener(v -> finish());

        // === ЛОГИКА РЕДАКТИРОВАНИЯ ===
        btnEdit.setOnClickListener(v -> {
            // Создаем диалог
            EditCollectionBottomSheet bottomSheet = EditCollectionBottomSheet.newInstance(currentCollectionTitle);

            // Слушаем результат (когда нажали "Сохранить")
            bottomSheet.setListener(newTitle -> {
                // 1. Обновляем переменную
                currentCollectionTitle = newTitle;
                // 2. Обновляем текст на экране
                tvTitle.setText(newTitle);
                // 3. Показываем тост
                CustomToast.show(this, "Название изменено");
            });

            bottomSheet.show(getSupportFragmentManager(), "EditCollectionTag");
        });

        btnDelete.setOnClickListener(v -> {
            CustomToast.show(this, "Удаление подборки");
        });
    }
}