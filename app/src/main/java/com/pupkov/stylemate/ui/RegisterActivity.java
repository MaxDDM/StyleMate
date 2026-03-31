package com.pupkov.stylemate.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.pupkov.stylemate.R;
import com.pupkov.stylemate.repository.ActiveUserInfo;
import com.pupkov.stylemate.repository.UserRepository;
import com.pupkov.stylemate.ui.dialogs.SkipRegDialog;
import com.pupkov.stylemate.ui.test.TestQ1Activity;

import org.apache.commons.validator.routines.EmailValidator;

import java.util.regex.Pattern;

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
        ImageButton verifyButton = findViewById(R.id.verifyEmail);
        ImageButton continueButton = findViewById(R.id.continueRegButton);
        ImageButton skipRegButton = findViewById(R.id.skipRegButton);

        verifyButton.setOnClickListener(v -> {
            EmailValidator validator = EmailValidator.getInstance();

            if (!validator.isValid(email.getText().toString())) {
                Toast.makeText(RegisterActivity.this, "Указан некорретный адрес", Toast.LENGTH_LONG).show();
            } else if (password.getText().toString().length() < 10 || password.getText().toString().length() > 20) {
                Toast.makeText(RegisterActivity.this, "Пароль должен содержать от 10 до 20 символов", Toast.LENGTH_LONG).show();
            } else if (!isValidDateRegex(birth.getText().toString())) {
                Toast.makeText(RegisterActivity.this, "Дата должна быть в формате ДД/ММ/ГГ", Toast.LENGTH_LONG).show();
            } else {
                repo.sendEmail(email.getText().toString(), password.getText().toString()).observe(this, resource -> {
                    switch(resource.status) {
                        case LOADING:
                            break;
                        case SUCCESS:
                            if (resource.data) {
                                Toast.makeText(RegisterActivity.this, "На указанный адрес отправлено письмо для его верификации", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(RegisterActivity.this, "Пользователь с указанным email уже зарегистрирован", Toast.LENGTH_LONG).show();
                            }
                            break;
                        case ERROR:
                            Toast.makeText(RegisterActivity.this, resource.message, Toast.LENGTH_LONG).show();
                            break;
                    }
                });
            }
        });

        continueButton.setOnClickListener(v -> {
            repo.checkEmailVerifiedAndRegister(name.getText().toString(), "", email.getText().toString(), birth.getText().toString(), "", password.getText().toString()).observe(this, resource -> {
                switch(resource.status) {
                    case LOADING:
                        Toast.makeText(RegisterActivity.this, "Идёт процесс регистрации", Toast.LENGTH_LONG).show();
                        break;
                    case SUCCESS:
                        if (resource.data) {
                            ActiveUserInfo.setDefaults("isRegistered", repo.getUID(), RegisterActivity.this);

                            Intent intent = new Intent(RegisterActivity.this, TestQ1Activity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(RegisterActivity.this, "Вы не подтвердили email. Для повторной отправки письма можно нажать ещё раз.", Toast.LENGTH_LONG).show();
                        }
                        break;
                    case ERROR:
                        Toast.makeText(RegisterActivity.this, resource.message, Toast.LENGTH_LONG).show();
                        break;
                }
            });
        });

        skipRegButton.setOnClickListener(v -> {
            SkipRegDialog dialog = new SkipRegDialog();

            dialog.setListener(this::finish);

            dialog.show(getSupportFragmentManager(), "DeleteDialog");
        });
    }

    public boolean isValidDateRegex(String dateStr) {
        String regex = "^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])/([0-9]{2}|[0-9]{4})$";
        return Pattern.matches(regex, dateStr);
    }
}