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
import com.example.stylemate.ui.dialogs.SkipRegDialog;
import com.example.stylemate.ui.test.TestQ1Activity;

import org.apache.commons.validator.routines.EmailValidator;

public class RegisterActivity extends AppCompatActivity {
    UserRepository repo = new UserRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.reg);

        EditText email = findViewById(R.id.emailReg);
        EditText password = findViewById(R.id.passwordReg);
        EditText name = findViewById(R.id.nameReg);
        EditText birth = findViewById(R.id.birthDateReg);
        ImageButton continueButton = findViewById(R.id.continueRegButton);
        ImageButton skipRegButton = findViewById(R.id.skipRegButton);

        continueButton.setOnClickListener(v -> {
            EmailValidator validator = EmailValidator.getInstance();

            if (!validator.isValid(email.getText().toString())) {
                Toast.makeText(RegisterActivity.this, "Указан некорретный адрес", Toast.LENGTH_LONG).show();
            } else if (password.getText().toString().length() < 10 || password.getText().toString().length() > 20) {
                Toast.makeText(RegisterActivity.this, "Пароль должен содержать от 10 до 20 символов", Toast.LENGTH_LONG).show();
            } else {
                repo.exists(email.getText().toString().replace(".", "|"), RegisterActivity.this).observe(this, resource -> {
                    if (resource != null) {
                        switch(resource.status) {
                            case LOADING:
                                break;
                            case SUCCESS:
                                if (resource.data) {
                                    Toast.makeText(RegisterActivity.this, "Пользователь с этим email уже зарегистрирован", Toast.LENGTH_LONG).show();
                                } else {
                                    login(name.getText().toString(), email.getText().toString(), birth.getText().toString(), password.getText().toString());
                                }
                                break;
                            case ERROR:
                                break;
                        }
                    }
                });
            }
        });

        skipRegButton.setOnClickListener(v -> {
            SkipRegDialog dialog = new SkipRegDialog();

            dialog.setListener(this::finish);

            dialog.show(getSupportFragmentManager(), "DeleteDialog");
        });
    }

    private void login(String name, String email, String birth, String password) {
        repo.login(name, "", email, birth, null, password, RegisterActivity.this).observe(this, resource -> {
            if (resource != null) {
                switch (resource.status) {
                    case LOADING:
                        break;
                    case SUCCESS:
                        if (resource.data) {
                            ActiveUserInfo.setDefaults("isRegistered", email.replace(".", "|"), RegisterActivity.this);

                            Intent intent = new Intent(RegisterActivity.this, TestQ1Activity.class);
                            startActivity(intent);
                        }
                        break;
                    case ERROR:
                        break;
                }
            }
        });
    }
}