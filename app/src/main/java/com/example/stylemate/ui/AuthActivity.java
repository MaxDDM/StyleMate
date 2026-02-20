package com.example.stylemate.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stylemate.R;
import com.example.stylemate.repository.ActiveUserInfo;
import com.example.stylemate.repository.UserRepository;

import org.apache.commons.validator.routines.EmailValidator;

public class AuthActivity extends AppCompatActivity {
    UserRepository repo = new UserRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.auth);

        String isAuthorized = ActiveUserInfo.getDefaults("isRegistered", this);
        if (isAuthorized != null && !isAuthorized.isEmpty()) {
            Intent intent = new Intent(AuthActivity.this, MainActivity.class);
            startActivity(intent);
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

            if (repo.exists(email.getText().toString().replace(".", "|"), AuthActivity.this)) {
                if (repo.checkCurrentPassword(password.getText().toString(), AuthActivity.this)) {
                    ActiveUserInfo.setDefaults("isRegistered", email.getText().toString().replace(".", "|"), AuthActivity.this);

                    Intent intent = new Intent(AuthActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(AuthActivity.this, "Введён неверный пароль", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(AuthActivity.this, "Пользователь с такой почтой не зарегистрирован", Toast.LENGTH_LONG).show();
            }
        });

        switchToRegButton.setOnClickListener(v -> {
            Intent intent = new Intent(AuthActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

    }
}