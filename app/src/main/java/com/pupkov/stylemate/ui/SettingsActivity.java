package com.pupkov.stylemate.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import android.util.Patterns;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.pupkov.stylemate.R;
import com.pupkov.stylemate.model.Resource;
import com.pupkov.stylemate.model.SettingsViewModel;
import com.pupkov.stylemate.repository.UserRepository;

import java.util.regex.Pattern;
import java.io.File;

public class SettingsActivity extends AppCompatActivity {

    private SettingsViewModel viewModel;
    private EditText etName;
    private EditText etPhone;
    private EditText etEmail;
    private EditText etDate;
    private ImageView imgAvatar;

    private final UserRepository repo = new UserRepository();
    private Uri cameraImageUri;

    private String currentAvatarUrl;

    private boolean isLoadingData = false;

    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        registerLaunchers();
        initViews();
        setupListeners();
        observeViewModel();
    }

    private void registerLaunchers() {

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        handleImageUri(uri);
                    }
                }
        );

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && cameraImageUri != null) {
                        handleImageUri(cameraImageUri);
                    }
                }
        );

        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        launchCamera();
                    } else {
                        CustomToast.show(this, "Для съёмки фото нужно разрешение на камеру");
                    }
                }
        );
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etDate = findViewById(R.id.etDate);
        imgAvatar = findViewById(R.id.imgAvatarEdit);
    }

    private void setupListeners() {
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (imgAvatar != null) {
            imgAvatar.setOnClickListener(v -> {
                if (!repo.isLogged(this)) {
                    CustomToast.show(this, "Смена аватарки доступна только после регистрации");
                    return;
                }
                showChangePhotoDialog();
            });
        }

        Button btnChangePassword = findViewById(R.id.btnChangePassword);
        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v -> {
                ChangePasswordBottomSheet bottomSheet = new ChangePasswordBottomSheet();
                bottomSheet.show(getSupportFragmentManager(), "ChangePasswordTag");
            });
        }

        Button btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> showLogoutDialog());
        }

        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isLoadingData) return;
                if(repo.isLogged(SettingsActivity.this) && !s.toString().isEmpty()) {
                    repo.changeParameter("name", s.toString());
                }
            }
        });

        etPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isLoadingData) return;
                if(repo.isLogged(SettingsActivity.this) && !s.toString().isEmpty()) {
                    repo.changeParameter("phone", s.toString());
                }
            }
        });

        etDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isLoadingData) return;
                if(repo.isLogged(SettingsActivity.this)) {
                    String regex = "^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])/([0-9]{2}|[0-9]{4})$";
                    if (Pattern.matches(regex, s.toString()) && !s.toString().isEmpty()) {
                        repo.changeParameter("birthDate", s.toString());
                    } else {
                        CustomToast.show(SettingsActivity.this, "Дата должна быть в формате ДД/ММ/ГГ");
                    }
                }
            }
        });
    }

    private void observeViewModel() {
        viewModel.userProfile.observe(this, user -> {
            if (user != null) {
                switch (user.status) {
                    case LOADING:
                        break;
                    case SUCCESS:
                        isLoadingData = true;
                        etName.setText(user.data.name);
                        etPhone.setText(user.data.phone);

                        if (user.data.email == null) {
                            etEmail.setText(user.data.email);
                        } else {
                            etEmail.setText(user.data.email.replace('|', '.'));
                        }

                        etDate.setText(user.data.birthDate);
                        currentAvatarUrl = user.data.avatarUrl;
                        if (imgAvatar != null && currentAvatarUrl != null && !currentAvatarUrl.isEmpty()) {
                            Glide.with(SettingsActivity.this)
                                    .load(currentAvatarUrl)
                                    .apply(RequestOptions.circleCropTransform())
                                    .placeholder(R.drawable.ic_edit_avatar)
                                    .error(R.drawable.ic_edit_avatar)
                                    .into(imgAvatar);
                        }
                        isLoadingData = false;

                        break;
                    case ERROR:
                        break;
                }

            }
        });

        viewModel.logoutEvent.observe(this, isLoggedOut -> {
            if (isLoggedOut) {
                CustomToast.show(this, "Выход из аккаунта");
                Intent intent = new Intent(SettingsActivity.this, AuthActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void showChangePhotoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_change_photo, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setGravity(android.view.Gravity.CENTER);
        }

        ImageView imgDialogAvatar = dialogView.findViewById(R.id.imgDialogAvatar);
        if (currentAvatarUrl != null && !currentAvatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(currentAvatarUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.ic_edit_avatar)
                    .error(R.drawable.ic_edit_avatar)
                    .into(imgDialogAvatar);
        }

        Button btnGallery = dialogView.findViewById(R.id.btnGallery);
        Button btnCamera = dialogView.findViewById(R.id.btnCamera);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        btnGallery.setOnClickListener(v -> {
            dialog.dismiss();
            galleryLauncher.launch("image/*");
        });

        btnCamera.setOnClickListener(v -> {
            dialog.dismiss();
            if (checkSelfPermission(Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void launchCamera() {
        File tempFile = new File(getCacheDir(),
                "camera_avatar_" + System.currentTimeMillis() + ".jpg");

        cameraImageUri = FileProvider.getUriForFile(
                this,
                getApplicationContext().getPackageName() + ".fileprovider",
                tempFile
        );

        cameraLauncher.launch(cameraImageUri);
    }

    private void handleImageUri(Uri uri) {
        CustomToast.show(this, "Загрузка фото...");

        repo.uploadAvatar(this, uri, new UserRepository.AvatarCallback() {
            @Override
            public void onSuccess(String downloadUrl) {
                currentAvatarUrl = downloadUrl;

                if (imgAvatar != null) {
                    Glide.with(SettingsActivity.this)
                            .load(downloadUrl)
                            .apply(RequestOptions.circleCropTransform())
                            .placeholder(R.drawable.ic_edit_avatar)
                            .error(R.drawable.ic_edit_avatar)
                            .into(imgAvatar);
                }

                CustomToast.show(SettingsActivity.this,  "Фото обновлено");
            }

            @Override
            public void onError(String error) {
                CustomToast.show(SettingsActivity.this, error);
            }
        });
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_logout, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        Button btnCancel = dialogView.findViewById(R.id.btnCancelLogout);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmLogout);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            viewModel.onLogoutConfirmed(SettingsActivity.this);
        });

        dialog.show();
    }

    boolean isPhoneLoose(String s) {
        if (s == null) return false;
        s = s.trim();

        if (!s.matches("^\\+?[0-9()\\s-]{5,}$")) return false;

        String digits = s.replaceAll("\\D", "");
        return digits.length() >= 10 && digits.length() <= 15;
    }
}