package com.example.stylemate;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DeleteCollectionDialog extends DialogFragment {

    private OnDeleteListener listener;

    // Интерфейс для связи с Activity
    public interface OnDeleteListener {
        void onConfirmDelete();
    }

    public void setListener(OnDeleteListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_delete_collection, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Настраиваем внешний вид окна (Прозрачный фон + ширина)
        if (getDialog() != null && getDialog().getWindow() != null) {
            Window window = getDialog().getWindow();
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // Ширина 85% от экрана, чтобы были отступы по бокам
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);
            window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnConfirm = view.findViewById(R.id.btnConfirmDelete);
        Button btnCancel = view.findViewById(R.id.btnCancelDelete);

        // Кнопка ОТМЕНА - просто закрываем
        btnCancel.setOnClickListener(v -> dismiss());

        // Кнопка УДАЛИТЬ - сообщаем Activity и закрываем
        btnConfirm.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConfirmDelete();
            }
            dismiss();
        });
    }
}