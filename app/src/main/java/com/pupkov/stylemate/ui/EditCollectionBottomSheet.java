package com.pupkov.stylemate.ui;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.pupkov.stylemate.R;

/**
 * Окно для переименования папки
 */
public class EditCollectionBottomSheet extends DialogFragment {

    private String currentTitle;
    private OnTitleSavedListener listener;

    // Интерфейс для передачи нового названия обратно в Activity
    public interface OnTitleSavedListener {
        void onTitleSaved(String newTitle);
    }

    public void setListener(OnTitleSavedListener listener) {
        this.listener = listener;
    }

    // Создаем окно и передаем в него старое название
    public static EditCollectionBottomSheet newInstance(String title) {
        EditCollectionBottomSheet fragment = new EditCollectionBottomSheet();
        Bundle args = new Bundle();
        args.putString("TITLE", title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Достаем старое название из аргументов
        if (getArguments() != null) {
            currentTitle = getArguments().getString("TITLE");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_collection, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() != null && getDialog().getWindow() != null) {
            Window window = getDialog().getWindow();

            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = android.view.Gravity.TOP;
            params.y = 0;
            window.setAttributes(params);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvCurrentTitle = view.findViewById(R.id.tvCurrentTitle);
        EditText etNewTitle = view.findViewById(R.id.etNewTitle);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        // Показываем текущее название папки
        if (currentTitle != null) {
            tvCurrentTitle.setText(currentTitle);
        }

        // Кнопка "Отмена" — просто закрываем окно
        btnCancel.setOnClickListener(v -> dismiss());

        // Кнопка "Сохранить" — проверяем текст и сохраняем
        btnSave.setOnClickListener(v -> {
            String newText = etNewTitle.getText().toString().trim();

            // Если ничего не ввели — ругаемся
            if (newText.isEmpty()) {
                CustomToast.show(getContext(), "Введите название");
                return;
            }

            // Если все ок — отдаем текст и закрываемся
            if (listener != null) {
                listener.onTitleSaved(newText);
            }
            dismiss();
        });
    }
}