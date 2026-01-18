package com.example.stylemate;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
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

        // 2. Логика кнопки НАЗАД
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 3. Данные пользователя
        // В конце передаем R.drawable.avatar (твоя картинка)
        UserProfile currentUser = new UserProfile(
                "Марат",
                "+7 (999) 123 45 67",
                "marat@edu.hse.ru",
                "12.01.2001",
                R.drawable.avatar // Ссылка на твой файл avatar.png в папке drawable
        );

        // Для теста "без данных" раскомментируй строку ниже:
        // UserProfile currentUser = null;

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

    private void updateUI(UserProfile user) {
        if (user == null) {
            // Если юзера нет — чистим поля, чтобы вылезли Hint'ы, и ставим заглушку
            etName.setText("");
            etPhone.setText("");
            etEmail.setText("");
            etDate.setText("");
            imgAvatar.setImageResource(R.drawable.ic_edit_avatar);
            return;
        }

        // Заполняем текст (Тернарный оператор: если есть данные -> ставим, если нет -> "")
        etName.setText(user.name != null ? user.name : "");
        etPhone.setText(user.phone != null ? user.phone : "");
        etEmail.setText(user.email != null ? user.email : "");
        etDate.setText(user.birthDate != null ? user.birthDate : "");

        // Заполняем аватарку
        if (user.avatarResId != 0) {
            // Если ID картинки передан (не 0), ставим её
            imgAvatar.setImageResource(user.avatarResId);
        } else {
            // Иначе ставим заглушку
            imgAvatar.setImageResource(R.drawable.ic_placeholder_avatar);
        }
    }
}