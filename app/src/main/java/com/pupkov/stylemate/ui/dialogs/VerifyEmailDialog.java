package com.pupkov.stylemate.ui.dialogs;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.pupkov.stylemate.R;
import com.pupkov.stylemate.repository.ActiveUserInfo;
import com.pupkov.stylemate.repository.UserRepository;
import com.pupkov.stylemate.ui.RegisterActivity;
import com.pupkov.stylemate.ui.test.TestQ1Activity;

public class VerifyEmailDialog extends DialogFragment {
    // 1. Создаем свой интерфейс специально для этого окна
    public interface OnVerifyListener {
        void onCheckStatus();
    }

    private OnVerifyListener listener;

    // 2. Метод для установки слушателя
    public void setListener(OnVerifyListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_verify_email, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            Window window = getDialog().getWindow();
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);
            window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnContinue = view.findViewById(R.id.btnContinueVerify);
        Button btnRepeat = view.findViewById(R.id.btnRepeatVerify);


        btnContinue.setOnClickListener(v -> {
            // 3. САМОЕ ВАЖНОЕ: вызываем слушатель перед закрытием
            if (listener != null) {
                listener.onCheckStatus();
            }
            dismiss();
        });

        btnRepeat.setOnClickListener(v -> {
            RegisterActivity obj = new RegisterActivity();

            assert getArguments() != null;
            String email = getArguments().getString("email");
            String password = getArguments().getString("password");

            UserRepository repo = new UserRepository();

            repo.sendEmail(email, password).observe(getViewLifecycleOwner(), resource -> {
                switch(resource.status) {
                    case LOADING:
                        Toast.makeText(requireContext(), "Отправка письма с подтверждением...", Toast.LENGTH_SHORT).show();
                        break;
                    case SUCCESS:
                        if (resource.data) {
                            Toast.makeText(requireContext(), "На указанный адрес отправлено письмо для его верификации", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(requireContext(), "Пользователь с указанным email уже зарегистрирован", Toast.LENGTH_LONG).show();
                        }
                        break;
                    case ERROR:
                        Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show();
                        break;
                }
            });
        });
    }
}
