package com.example.stylemate;

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
import androidx.fragment.app.DialogFragment; // ВАЖНО: теперь DialogFragment

// Меняем наследование на DialogFragment
public class EditCollectionBottomSheet extends DialogFragment {

    private String currentTitle;
    private OnTitleSavedListener listener;

    public interface OnTitleSavedListener {
        void onTitleSaved(String newTitle);
    }

    public void setListener(OnTitleSavedListener listener) {
        this.listener = listener;
    }

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
        if (getArguments() != null) {
            currentTitle = getArguments().getString("TITLE");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_collection, container, false);
    }

    // ЭТОТ МЕТОД НУЖЕН, ЧТОБЫ ОКНО БЫЛО КРАСИВЫМ (ПО ЦЕНТРУ ИЛИ СВЕРХУ)
    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() != null && getDialog().getWindow() != null) {
            Window window = getDialog().getWindow();

            // 1. Делаем фон системного окна прозрачным
            // (чтобы видеть только наш LinearLayout с закругленными углами снизу)
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // 2. Растягиваем на ВСЮ ширину экрана
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            // 3. Получаем параметры, чтобы задать Гравитацию
            WindowManager.LayoutParams params = window.getAttributes();

            // 4. Прижимаем к ВЕРХУ экрана
            params.gravity = android.view.Gravity.TOP;

            // 5. Убираем любые отступы (Y = 0)
            params.y = 0;

            // Опционально: можно добавить анимацию появления сверху
            // window.setWindowAnimations(R.style.DialogAnimationTop);

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

        if (currentTitle != null) {
            tvCurrentTitle.setText(currentTitle);
        }

        btnCancel.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> {
            String newText = etNewTitle.getText().toString().trim();
            if (newText.isEmpty()) {
                CustomToast.show(getContext(), "Введите название");
                return;
            }
            if (listener != null) {
                listener.onTitleSaved(newText);
            }
            dismiss();
        });
    }
}