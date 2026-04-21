package com.pupkov.stylemate.ui.dialogs;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.pupkov.stylemate.R;

public class PrivacyConsentDialog extends DialogFragment {

    public interface OnConsentListener {
        void onResult(boolean isGranted);
    }

    private OnConsentListener listener;

    public void setOnConsentListener(OnConsentListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Убираем стандартный фон диалога, чтобы видеть наши закругления
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return inflater.inflate(R.layout.dialog_privacy_consent, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (view instanceof ViewGroup) {
            View contentContainer = ((ViewGroup) view).getChildAt(0);

            // Настройка программного фона
            GradientDrawable shape = new GradientDrawable();
            shape.setCornerRadius(80f); // Настройте радиус под ваш дизайн
            shape.setColor(Color.WHITE);
            contentContainer.setBackground(shape);
        }

        TextView tvPrivacyText = view.findViewById(R.id.tvPrivacyText);
        Button btnConfirm = view.findViewById(R.id.btnConfirm);
        Button btnExit = view.findViewById(R.id.btnExit);

        setupClickableText(tvPrivacyText);

        btnConfirm.setOnClickListener(v -> {
            if (listener != null) listener.onResult(true);
            dismiss();
        });

        btnExit.setOnClickListener(v -> {
            if (listener != null) listener.onResult(false);
            dismiss();
        });
    }

    private void setupClickableText(TextView textView) {
        String fullText = "Вы согласны с Политикой конфиденциальности и правилами использования?";
        String linkPart = "Политикой конфиденциальности и правилами использования";

        SpannableString ss = new SpannableString(fullText);
        int start = fullText.indexOf(linkPart);
        int end = start + linkPart.length();
        textView.setHighlightColor(Color.TRANSPARENT);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                String url = "https://stylemate.tilda.ws/privacy-policy"; // Замените на вашу ссылку
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                intent.setData(android.net.Uri.parse(url));
                startActivity(intent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.parseColor("#1A73E8")); // Синий цвет
                ds.setUnderlineText(true); // Убираем подчеркивание, если нужно как на макете
            }
        };

        ss.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(ss);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}