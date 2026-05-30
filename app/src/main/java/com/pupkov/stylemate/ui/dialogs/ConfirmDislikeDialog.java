package com.pupkov.stylemate.ui.dialogs;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.DialogFragment;

import com.pupkov.stylemate.R;

/**
 * Окно подтверждения скрытия (дизлайка) образа.
 */
public class ConfirmDislikeDialog extends DialogFragment {

    private OnConfirmDislikeListener listener;

    // Слушатель для обработки обоих исходов (скрыть или отменить)
    public interface OnConfirmDislikeListener {
        void onConfirmHide();
        void onCancelHide();
    }

    public void setListener(OnConfirmDislikeListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_confirm_dislike, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            Window window = getDialog().getWindow();
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // Делаем ширину окна 85% от экрана для красоты
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);
            window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppCompatButton btnConfirmHide = view.findViewById(R.id.btnConfirmHide);
        AppCompatButton btnCancelHide = view.findViewById(R.id.btnCancelHide);

        // Кнопка «Отменить» — уведомляем экран и закрываем
        btnCancelHide.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancelHide();
            }
            dismiss();
        });

        // Кнопка «Скрыть» — уведомляем экран и закрываем
        btnConfirmHide.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConfirmHide();
            }
            dismiss();
        });
    }
}