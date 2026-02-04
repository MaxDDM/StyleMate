package com.example.stylemate.ui;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stylemate.R;
import com.example.stylemate.ui.dialogs.SkipRegDialog;

import org.apache.commons.validator.routines.EmailValidator;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.reg);

        EditText email = findViewById(R.id.emailReg);
        EditText password = findViewById(R.id.passwordReg);
        ImageButton continueButton = findViewById(R.id.continueRegButton);
        ImageButton skipRegButton = findViewById(R.id.skipRegButton);

        continueButton.setOnClickListener(v -> {
            EmailValidator validator = EmailValidator.getInstance();

            if (!validator.isValid(email.getText().toString())) {
                Toast.makeText(RegisterActivity.this, "Указан некорретный адрес", Toast.LENGTH_LONG).show();
            }

            if (password.getText().toString().length() < 10 || password.getText().toString().length() > 20) {
                Toast.makeText(RegisterActivity.this, "Пароль должен содержать от 10 до 20 символов", Toast.LENGTH_LONG).show();
            }

            // тут будет регистрация пользователя в бд
        });

        skipRegButton.setOnClickListener(v -> {
            SkipRegDialog dialog = new SkipRegDialog();

            dialog.setListener(this::finish);

            dialog.show(getSupportFragmentManager(), "DeleteDialog");
        });
    }
}