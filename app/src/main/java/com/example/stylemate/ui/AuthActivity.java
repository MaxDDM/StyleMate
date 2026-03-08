package com.example.stylemate.ui;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;

import com.example.stylemate.R;
import com.example.stylemate.model.Resource;
import com.example.stylemate.repository.ActiveUserInfo;
import com.example.stylemate.repository.UserRepository;
import com.example.stylemate.ui.test.TestQ1Activity;

import org.apache.commons.validator.routines.EmailValidator;

public class AuthActivity extends AppCompatActivity {
    UserRepository repo = new UserRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.auth);

        String isTest1 = ActiveUserInfo.getDefaults("isTest1", this);
        if (isTest1 != null && !isTest1.isEmpty()) {
            Intent intent = new Intent(AuthActivity.this, TestQ1Activity.class);
            startActivity(intent);
            return;
        }

        String isAuthorized = ActiveUserInfo.getDefaults("isRegistered", this);
        if (isAuthorized != null && !isAuthorized.isEmpty() && !isAuthorized.equals("0")) {
            Intent intent = new Intent(AuthActivity.this, MainActivity.class);
            startActivity(intent);
            return;
        }

        EditText email = findViewById(R.id.emailAuth);
        EditText password = findViewById(R.id.passwordAuth);
        ImageButton authButton = findViewById(R.id.enterAuthButton);
        ImageButton switchToRegButton = findViewById(R.id.switchToRegButton);

        authButton.setOnClickListener(v -> {
            EmailValidator validator = EmailValidator.getInstance();

            if (!validator.isValid(email.getText().toString())) {
                Toast.makeText(AuthActivity.this, "Указан некорретный адрес", Toast.LENGTH_LONG).show();
            } else if (password.getText().toString().length() < 10 || password.getText().toString().length() > 20) {
                Toast.makeText(AuthActivity.this, "Пароль должен содержать от 10 до 20 символов", Toast.LENGTH_LONG).show();
            } else {
                repo.exists(email.getText().toString().replace(".", "|"), AuthActivity.this).observe(this, resource -> {
                    if (resource != null) {
                        switch (resource.status) {
                            case LOADING:
                                Toast.makeText(AuthActivity.this, "Идёт процесс авторизации", Toast.LENGTH_LONG).show();
                                break;
                            case SUCCESS:
                                ActiveUserInfo.setDefaults("isRegistered", email.getText().toString().replace(".", "|"), AuthActivity.this);

                                if (resource.data) {
                                    checkPassword(password.getText().toString());
                                } else {
                                    ActiveUserInfo.setDefaults("isRegistered", "", AuthActivity.this);
                                    Toast.makeText(AuthActivity.this, "Пользователь с такой почтой не зарегистрирован", Toast.LENGTH_LONG).show();
                                }
                                break;
                            case ERROR:
                                Toast.makeText(AuthActivity.this, resource.message, Toast.LENGTH_LONG).show();
                                break;
                        }

                    }
                });
            }
        });

        switchToRegButton.setOnClickListener(v -> {
            Intent intent = new Intent(AuthActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

    }

    private void checkPassword(String password) {
        repo.checkCurrentPassword(password, this).observe(this, resource1 -> {
            switch (resource1.status) {
                case LOADING:
                    break;
                case SUCCESS:
                    if (resource1.data) {
                        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(AuthActivity.this, "Введён неверный пароль", Toast.LENGTH_LONG).show();
                    }
                    break;
                case ERROR:
                    Toast.makeText(AuthActivity.this, resource1.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }
}