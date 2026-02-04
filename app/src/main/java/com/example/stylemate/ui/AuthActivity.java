package com.example.stylemate.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stylemate.R;

import org.apache.commons.validator.routines.EmailValidator;

public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.auth);

        String isAuthorized = ActiveUserInfo.getDefaults("isRegistered", this);
        if (isAuthorized != null && !isAuthorized.isEmpty()) {
            // тут будет переход на главную активити
        }

        EditText email = findViewById(R.id.emailAuth);
        EditText password = findViewById(R.id.passwordAuth);
        ImageButton authButton = findViewById(R.id.enterAuthButton);
        ImageButton switchToRegButton = findViewById(R.id.switchToRegButton);

        authButton.setOnClickListener(v -> {
            EmailValidator validator = EmailValidator.getInstance();

            if (!validator.isValid(email.getText().toString())) {
                Toast.makeText(AuthActivity.this, "Указан некорретный адрес", Toast.LENGTH_LONG).show();
            }

            if (password.getText().toString().length() < 10 || password.getText().toString().length() > 20) {
                Toast.makeText(AuthActivity.this, "Пароль должен содержать от 10 до 20 символов", Toast.LENGTH_LONG).show();
            }

            // тут будет проверка наличия пользователя в бд и переход на главную
        });

        switchToRegButton.setOnClickListener(v -> {
            Intent intent = new Intent(AuthActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

    }
}