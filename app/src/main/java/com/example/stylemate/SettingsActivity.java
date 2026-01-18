package com.example.stylemate;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast; // Добавил для проверки кнопок
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private EditText etName;
    private EditText etPhone;
    private EditText etEmail;
    private EditText etDate;
    private ImageView imgAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 1. Находим View
        initViews();

        // 2. Логика кнопок (вынес в отдельный метод для чистоты)
        setupListeners();

        // 3. Данные пользователя
        UserProfile currentUser = new UserProfile(
                "Марат",
                "+7 (999) 123 45 67",
                "marat@edu.hse.ru",
                "12.01.2001",
                R.drawable.avatar
        );

        // 4. Заполняем
        updateUI(currentUser);
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etDate = findViewById(R.id.etDate);
        imgAvatar = findViewById(R.id.imgAvatarEdit);
    }

    private void setupListeners() {
        // Кнопка НАЗАД
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Клик по АВАТАРКЕ -> Открываем диалог
        if (imgAvatar != null) {
            imgAvatar.setOnClickListener(v -> showChangePhotoDialog());
        }
    }

    // === ЛОГИКА ДИАЛОГА (Всплывающего окна) ===
    private void showChangePhotoDialog() {
        // 1. Подготавливаем диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Берем наш XML файл диалога
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_change_photo, null);
        builder.setView(dialogView);

        // 2. Создаем диалог
        AlertDialog dialog = builder.create();

        // ВАЖНО: Делаем фон прозрачным, чтобы углы были скругленными
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            android.view.WindowManager.LayoutParams params = dialog.getWindow().getAttributes();

            // Оставляем гравитацию по центру
            params.gravity = android.view.Gravity.CENTER;

            params.y = -400;

            // Применяем настройки обратно
            dialog.getWindow().setAttributes(params);
        }

        // 3. Находим кнопки ВНУТРИ диалога (используем dialogView.findViewById)
        Button btnGallery = dialogView.findViewById(R.id.btnGallery);
        Button btnCamera = dialogView.findViewById(R.id.btnCamera);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        // 4. Вешаем действия на кнопки диалога
        btnGallery.setOnClickListener(v -> {
            Toast.makeText(this, "Открываем галерею...", Toast.LENGTH_SHORT).show();

            dialog.dismiss(); // Закрыть окно
        });

        btnCamera.setOnClickListener(v -> {
            Toast.makeText(this, "Открываем камеру...", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss(); // Просто закрыть окно
        });

        // 5. Показываем диалог
        dialog.show();
    }

    private void updateUI(UserProfile user) {
        if (user == null) {
            etName.setText("");
            etPhone.setText("");
            etEmail.setText("");
            etDate.setText("");
            imgAvatar.setImageResource(R.drawable.ic_placeholder_avatar); // Используй consistent placeholder
            return;
        }

        etName.setText(user.name != null ? user.name : "");
        etPhone.setText(user.phone != null ? user.phone : "");
        etEmail.setText(user.email != null ? user.email : "");
        etDate.setText(user.birthDate != null ? user.birthDate : "");

        if (user.avatarResId != 0) {
            imgAvatar.setImageResource(user.avatarResId);
        } else {
            imgAvatar.setImageResource(R.drawable.ic_placeholder_avatar);
        }
    }
}