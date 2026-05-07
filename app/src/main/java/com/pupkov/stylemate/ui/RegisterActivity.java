package com.pupkov.stylemate.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.pupkov.stylemate.R;
import com.pupkov.stylemate.repository.ActiveUserInfo;
import com.pupkov.stylemate.repository.UserRepository;
import com.pupkov.stylemate.ui.dialogs.DialogCheckEmail;
import com.pupkov.stylemate.ui.dialogs.DialogSuccessReg;
import com.pupkov.stylemate.ui.dialogs.SkipRegDialog;
import com.pupkov.stylemate.ui.dialogs.PrivacyConsentDialog;
import com.pupkov.stylemate.ui.dialogs.VerifyEmailDialog;
import com.pupkov.stylemate.ui.test.TestQ1Activity;
import com.google.android.material.datepicker.MaterialDatePicker;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

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

        ImageButton backToAuth = findViewById(R.id.backToAuth);

        backToAuth.setOnClickListener(v -> finish());

        birth.setOnClickListener(v -> {
            // 1. Создаем календарь
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Выберите дату рождения")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            // 2. Слушатель нажатия "OK"
            datePicker.addOnPositiveButtonClickListener(selection -> {
                // Форматируем выбранную дату в твой формат ДД/ММ/ГГ
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendar.setTimeInMillis(selection);
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
                String formattedDate = format.format(calendar.getTime());

                birth.setText(formattedDate);
            });

            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        });

        ImageButton verifyButton = findViewById(R.id.verifyEmail);
        //ImageButton continueButton = findViewById(R.id.continueRegButton);
        ImageButton skipRegButton = findViewById(R.id.skipRegButton);

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean allFilled = !email.getText().toString().trim().isEmpty() &&
                        !password.getText().toString().trim().isEmpty() &&
                        !name.getText().toString().trim().isEmpty() &&
                        !birth.getText().toString().trim().isEmpty();

                backToAuth.setVisibility(allFilled ? View.GONE : View.VISIBLE);
                verifyButton.setVisibility(allFilled ? View.VISIBLE : View.GONE);
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        email.addTextChangedListener(watcher);
        password.addTextChangedListener(watcher);
        name.addTextChangedListener(watcher);
        birth.addTextChangedListener(watcher);

        /*verifyButton.setOnClickListener(v -> {
            EmailValidator validator = EmailValidator.getInstance();

            // 1. Сначала стандартные проверки полей
            if (!validator.isValid(email.getText().toString())) {
                Toast.makeText(RegisterActivity.this, "Указан некорретный адрес", Toast.LENGTH_LONG).show();
                return;
            }

            if (password.getText().toString().length() < 6 || password.getText().toString().length() > 12) {
                Toast.makeText(RegisterActivity.this, "Пароль должен содержать от 6 до 12 символов", Toast.LENGTH_LONG).show();
                return;
            }

            if (!isValidDateRegex(birth.getText().toString())) {
                Toast.makeText(RegisterActivity.this, "Дата должна быть в формате ДД/ММ/ГГ", Toast.LENGTH_LONG).show();
                return;
            }

            // 2. Проверка согласия (из SharedPreferences)
            if (isPrivacyAccepted()) {
                // Если уже соглашался ранее — сразу отправляем email
                sendVerificationEmail(email.getText().toString(), password.getText().toString(), RegisterActivity.this);
            } else {
                // Если еще не соглашался — показываем диалог
                PrivacyConsentDialog dialog = new PrivacyConsentDialog();
                dialog.setOnConsentListener(isGranted -> {
                    if (isGranted) {
                        savePrivacyAccepted(); // Сохраняем, чтобы больше не спрашивать
                        sendVerificationEmail(email.getText().toString(), password.getText().toString(), RegisterActivity.this);
                    } else {
                        Toast.makeText(RegisterActivity.this, "Для регистрации необходимо принять соглашение", Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.show(getSupportFragmentManager(), "PrivacyDialog");
            }

            VerifyEmailDialog dialog = new VerifyEmailDialog();

            Bundle args = new Bundle();
            args.putString("email", email.getText().toString());
            args.putString("password", password.getText().toString());
            dialog.setArguments(args);

            dialog.show(getSupportFragmentManager(), "DeleteDialog");
        });*/

        /*continueButton.setOnClickListener(v -> {
            repo.checkEmailVerifiedAndRegister(name.getText().toString(), "", email.getText().toString(), birth.getText().toString(), "", password.getText().toString()).observe(this, resource -> {
                switch(resource.status) {
                    case LOADING:
                        Toast.makeText(RegisterActivity.this, "Идёт процесс регистрации", Toast.LENGTH_LONG).show();
                        break;
                    case SUCCESS:
                        if (resource.data) {
                            DialogSuccessReg dialog = new DialogSuccessReg();

                            Bundle args = new Bundle();
                            args.putString("uid", repo.getUID());
                            dialog.setArguments(args);

                            dialog.show(getSupportFragmentManager(), "DeleteDialog");
                        } else {
                            DialogCheckEmail dialog = new DialogCheckEmail();

                            Bundle args = new Bundle();
                            args.putString("email", email.getText().toString());
                            args.putString("password", password.getText().toString());
                            dialog.setArguments(args);

                            dialog.show(getSupportFragmentManager(), "DeleteDialog");
                        }
                        break;
                    case ERROR:
                        Toast.makeText(RegisterActivity.this, resource.message, Toast.LENGTH_LONG).show();
                        break;
                }
            });
        });*/

        verifyButton.setOnClickListener(v -> {
            // 1. Валидация
            if (!validateInputs()) return;

            // 2. Проверка политики
            if (isPrivacyAccepted()) {
                startVerificationChain();
            } else {
                PrivacyConsentDialog dialog = new PrivacyConsentDialog();
                dialog.setOnConsentListener(isGranted -> {
                    if (isGranted) {
                        savePrivacyAccepted();
                        startVerificationChain();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Для регистрации нужно принять соглашение", Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.show(getSupportFragmentManager(), "PrivacyDialog");
            }
        });

        /*skipRegButton.setOnClickListener(v -> {
            SkipRegDialog dialog = new SkipRegDialog();

            dialog.setListener(this::finish);

            dialog.show(getSupportFragmentManager(), "DeleteDialog");
        });*/

        backToAuth.setOnClickListener(v -> {
            // Просто закрываем текущую активность, возвращаясь на AuthActivity
            finish();
        });

        skipRegButton.setOnClickListener(v -> {
            if (isPrivacyAccepted()) {
                // Если уже принимал — сразу показываем диалог пропуска
                showSkipDialog();
            } else {
                // Если нет — просим согласиться
                PrivacyConsentDialog dialog = new PrivacyConsentDialog();
                dialog.setOnConsentListener(isGranted -> {
                    if (isGranted) {
                        savePrivacyAccepted(); // Сохраняем выбор
                        showSkipDialog();      // Открываем диалог пропуска
                    } else {
                        Toast.makeText(RegisterActivity.this, "Для продолжения необходимо принять соглашение", Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.show(getSupportFragmentManager(), "PrivacyDialog");
            }
        });
    }

    private boolean validateInputs() {
        EditText email = findViewById(R.id.emailReg);
        EditText password = findViewById(R.id.passwordReg);
        EditText birth = findViewById(R.id.birthDateReg);
        EmailValidator validator = EmailValidator.getInstance();

        if (!validator.isValid(email.getText().toString())) {
            Toast.makeText(this, "Указан некорретный адрес", Toast.LENGTH_LONG).show();
            return false;
        }
        if (password.getText().length() < 6 || password.getText().length() > 12) {
            Toast.makeText(this, "Пароль должен содержать от 6 до 12 символов", Toast.LENGTH_LONG).show();
            return false;
        }
        if (!isValidDateRegex(birth.getText().toString())) {
            Toast.makeText(this, "Дата должна быть в формате ДД/ММ/ГГ", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void startVerificationChain() {
        String emailStr = ((EditText)findViewById(R.id.emailReg)).getText().toString();
        String passwordStr = ((EditText)findViewById(R.id.passwordReg)).getText().toString();

        // 1. Отправляем письмо
        sendVerificationEmail(emailStr, passwordStr, this);

        // 2. Показываем диалог VerifyEmailDialog (Инструкция)
        VerifyEmailDialog verifyDialog = new VerifyEmailDialog();
        Bundle args = new Bundle();
        args.putString("email", emailStr);
        args.putString("password", passwordStr);
        verifyDialog.setArguments(args);

        // ВАЖНО: Вешаем слушатель на кнопку "Я подтвердил" в этом диалоге
        verifyDialog.setListener(this::checkUserStatusAndFinalize);

        verifyDialog.show(getSupportFragmentManager(), "VerifyDialog");
    }

    private void checkUserStatusAndFinalize() {
        String emailStr = ((EditText)findViewById(R.id.emailReg)).getText().toString();
        String passwordStr = ((EditText)findViewById(R.id.passwordReg)).getText().toString();
        String nameStr = ((EditText)findViewById(R.id.nameReg)).getText().toString();
        String birthStr = ((EditText)findViewById(R.id.birthDateReg)).getText().toString();

        repo.checkEmailVerifiedAndRegister(nameStr, "", emailStr, birthStr, "", passwordStr)
                .observe(this, resource -> {
                    switch(resource.status) {
                        case LOADING:
                            Toast.makeText(this, "Проверяем статус подтверждения...", Toast.LENGTH_SHORT).show();
                            break;
                        case SUCCESS:
                            if (resource.data) {
                                // УСПЕХ: Почта подтверждена
                                DialogSuccessReg dialog = new DialogSuccessReg();
                                Bundle args = new Bundle();
                                args.putString("uid", repo.getUID());
                                dialog.setArguments(args);
                                dialog.show(getSupportFragmentManager(), "SuccessDialog");
                            } else {
                                // ОШИБКА: Посылка не подтверждена
                                DialogCheckEmail dialog = new DialogCheckEmail();
                                Bundle args = new Bundle();
                                args.putString("email", emailStr);
                                args.putString("password", passwordStr);
                                dialog.setArguments(args);
                                dialog.show(getSupportFragmentManager(), "CheckEmailDialog");
                            }
                            break;
                        case ERROR:
                            Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show();
                            break;
                    }
                });
    }

    public void sendVerificationEmail(String emailStr, String passwordStr, Context context) {
        repo.sendEmail(emailStr, passwordStr).observe(this, resource -> {
            switch(resource.status) {
                case LOADING:
                    Toast.makeText(context, "Отправка письма с подтверждением...", Toast.LENGTH_SHORT).show();
                    break;
                case SUCCESS:
                    if (resource.data) {
                        Toast.makeText(context, "На указанный адрес отправлено письмо для его верификации", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, "Пользователь с указанным email уже зарегистрирован", Toast.LENGTH_LONG).show();
                    }
                    break;
                case ERROR:
                    Toast.makeText(context, resource.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    private void showSkipDialog() {
        SkipRegDialog dialog = new SkipRegDialog();
        // При нажатии "Продолжить без регистрации" в диалоге — закрываем экран (finish)
        dialog.setListener(this::finish);
        dialog.show(getSupportFragmentManager(), "SkipDialog");
    }

    private boolean isPrivacyAccepted() {
        return getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getBoolean("privacy_accepted", false);
    }

    private void savePrivacyAccepted() {
        getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("privacy_accepted", true)
                .apply();
    }

    public boolean isValidDateRegex(String dateStr) {
        String regex = "^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])/([0-9]{2}|[0-9]{4})$";
        return Pattern.matches(regex, dateStr);
    }
}