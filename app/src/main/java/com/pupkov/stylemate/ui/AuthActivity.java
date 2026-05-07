package com.pupkov.stylemate.ui;

import static androidx.core.content.ContextCompat.startActivity;

import static com.pupkov.stylemate.model.Resource.Status.ERROR;
import static com.pupkov.stylemate.model.Resource.Status.LOADING;
import static com.pupkov.stylemate.model.Resource.Status.SUCCESS;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;

import com.pupkov.stylemate.R;
import com.pupkov.stylemate.model.Resource;
import com.pupkov.stylemate.repository.ActiveUserInfo;
import com.pupkov.stylemate.repository.UserRepository;
import com.pupkov.stylemate.ui.dialogs.PrivacyConsentDialog;
import com.pupkov.stylemate.ui.dialogs.SkipRegDialog;
import com.pupkov.stylemate.ui.test.TestQ1Activity;

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
        if (isAuthorized != null) {
            Intent intent = new Intent(AuthActivity.this, MainActivity.class);
            startActivity(intent);
            return;
        }

        EditText email = findViewById(R.id.emailAuth);
        EditText password = findViewById(R.id.passwordAuth);
        ImageButton authButton = findViewById(R.id.enterAuthButton);
        ImageButton switchToRegButton = findViewById(R.id.switchToRegButton);
        ImageButton skipButton = findViewById(R.id.skipAuthButton);

        authButton.setOnClickListener(v -> {
            String emailStr = email.getText().toString();
            String passwordStr = password.getText().toString();

            if (isPrivacyAccepted()) {
                // Если политика принята, сразу запускаем процесс авторизации
                performLogin(emailStr, passwordStr);
            } else {
                // Иначе показываем диалог
                PrivacyConsentDialog dialog = new PrivacyConsentDialog();
                dialog.setOnConsentListener(isGranted -> {
                    if (isGranted) {
                        savePrivacyAccepted(); // Сохраняем согласие
                        // Автоматически продолжаем авторизацию
                        performLogin(emailStr, passwordStr);
                    } else {
                        Toast.makeText(AuthActivity.this, "Для авторизации необходимо принять соглашение", Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.show(getSupportFragmentManager(), "PrivacyDialog");
            }
        });

        switchToRegButton.setOnClickListener(v -> {
            Intent intent = new Intent(AuthActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        skipButton.setOnClickListener(v -> {
            if (isPrivacyAccepted()) {
                SkipRegDialog dialog = new SkipRegDialog();
                dialog.setListener(this::finish);
                dialog.show(getSupportFragmentManager(), "SkipDialog");
            } else {
                PrivacyConsentDialog dialog = new PrivacyConsentDialog();
                dialog.setOnConsentListener(isGranted -> {
                    if (isGranted) {
                        savePrivacyAccepted();
                        SkipRegDialog skipDialog = new SkipRegDialog();
                        skipDialog.setListener(this::finish);
                        skipDialog.show(getSupportFragmentManager(), "SkipDialog");
                    } else {
                        Toast.makeText(AuthActivity.this, "Для продолжения необходимо принять соглашение", Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.show(getSupportFragmentManager(), "PrivacyDialog");
            }
        });

    }

    private void performLogin(String emailStr, String passwordStr) {
        EmailValidator validator = EmailValidator.getInstance();

        if (!validator.isValid(emailStr)) {
            Toast.makeText(AuthActivity.this, "Указан некорретный адрес", Toast.LENGTH_LONG).show();
        } else {
            repo.loginUser(emailStr, passwordStr).observe(this, resource -> {
                switch(resource.status) {
                    case LOADING:
                        Toast.makeText(AuthActivity.this, "Идёт процесс авторизации", Toast.LENGTH_LONG).show();
                        break;
                    case SUCCESS:
                        if (resource.data) {
                            ActiveUserInfo.setDefaults("isRegistered", repo.getUID(), AuthActivity.this);

                            Intent intent = new Intent(AuthActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                        break;
                    case ERROR:
                        Toast.makeText(AuthActivity.this, resource.message, Toast.LENGTH_LONG).show();
                        break;
                }
            });
        }
    }

    private boolean isPrivacyAccepted() {
        return getSharedPreferences("app_prefs", MODE_PRIVATE).getBoolean("privacy_accepted", false);
    }

    private void savePrivacyAccepted() {
        getSharedPreferences("app_prefs", MODE_PRIVATE).edit().putBoolean("privacy_accepted", true).apply();
    }
}