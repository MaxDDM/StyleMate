package com.example.stylemate.ui;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.stylemate.R;
import com.example.stylemate.model.SettingsViewModel;

public class SettingsActivity extends AppCompatActivity {

    private SettingsViewModel viewModel;
    private EditText etName;
    private EditText etPhone;
    private EditText etEmail;
    private EditText etDate;
    private ImageView imgAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 1. Подключаем ViewModel
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        initViews();
        setupListeners();
        observeViewModel();
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etDate = findViewById(R.id.etDate);
        imgAvatar = findViewById(R.id.imgAvatarEdit);
    }

    private void setupListeners() {
        // Назад
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // Смена фото
        if (imgAvatar != null) imgAvatar.setOnClickListener(v -> showChangePhotoDialog());

        // Смена пароля
        Button btnChangePassword = findViewById(R.id.btnChangePassword);
        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v -> {
                ChangePasswordBottomSheet bottomSheet = new ChangePasswordBottomSheet();
                bottomSheet.show(getSupportFragmentManager(), "ChangePasswordTag");
            });
        }

        // === ВЫХОД ИЗ АККАУНТА ===
        Button btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> showLogoutDialog());
        }
    }

    private void observeViewModel() {
        // А. Заполняем поля данными
        viewModel.userProfile.observe(this, user -> {
            if (user != null) {
                etName.setText(user.name);
                etPhone.setText(user.phone);
                etEmail.setText(user.email);
                etDate.setText(user.birthDate);
                if (imgAvatar != null) imgAvatar.setImageResource(user.avatarResId != 0 ? user.avatarResId : R.drawable.ic_edit_avatar);
            }
        });

        // Б. Следим за выходом
        viewModel.logoutEvent.observe(this, isLoggedOut -> {
            if (isLoggedOut) {
                CustomToast.show(this, "Выход из аккаунта");
                // Тут можно открыть экран LoginActivity
                finish();
            }
        });
    }

    // Диалог смены фото (твой старый код)
    private void showChangePhotoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_change_photo, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setGravity(android.view.Gravity.CENTER);
            // dialog.getWindow().getAttributes().y = -400; // Тут можно убрать смещение, если хочешь по центру
        }

        Button btnGallery = dialogView.findViewById(R.id.btnGallery);
        Button btnCamera = dialogView.findViewById(R.id.btnCamera);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        btnGallery.setOnClickListener(v -> {
            Toast.makeText(this, "Открываем галерею...", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        btnCamera.setOnClickListener(v -> {
            Toast.makeText(this, "Открываем камеру...", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // === НОВЫЙ ДИАЛОГ ВЫХОДА ===
    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_logout, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Прозрачный фон для скругления
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        Button btnCancel = dialogView.findViewById(R.id.btnCancelLogout);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmLogout);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            // Мы не делаем логику тут, а зовем ViewModel
            viewModel.onLogoutConfirmed();
        });

        dialog.show();
    }
}