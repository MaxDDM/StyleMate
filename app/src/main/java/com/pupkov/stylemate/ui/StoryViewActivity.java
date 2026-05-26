package com.pupkov.stylemate.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.bumptech.glide.Glide;
import com.pupkov.stylemate.R;

public class StoryViewActivity extends AppCompatActivity {

    private float initialY;
    private float initialTranslationY;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_view);

        getOnBackPressedDispatcher().addCallback(this,
                new androidx.activity.OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        closeWithAnimation();
                    }
                });

        ImageView ivFullStory = findViewById(R.id.ivFullStory);
        android.view.View btnStoryLink = findViewById(R.id.btnStoryLink);
        View storyContainer = findViewById(R.id.storyContainer);

        // Получаем данные из Intent
        String imageUrl = getIntent().getStringExtra("image_url");
        String link = getIntent().getStringExtra("link");

        // Загружаем полноэкранную картинку через Glide
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .into(ivFullStory);
        }

        // Проверяем наличие ссылки и настраиваем кнопку
        if (link != null && !link.trim().isEmpty()) {
            btnStoryLink.setVisibility(View.VISIBLE);
            btnStoryLink.setOnClickListener(v -> {
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                    startActivity(browserIntent);
                } catch (Exception e) {
                    String fixedLink = "https://" + link.replace("http://", "").replace("https://", "");
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fixedLink));
                    startActivity(browserIntent);
                }
            });
        }


        // Логика Swipe-to-Dismiss (свайп вниз для закрытия)
        storyContainer.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialY = event.getRawY();
                    initialTranslationY = v.getTranslationY();
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float deltaY = event.getRawY() - initialY;
                    // Разрешаем движение только вниз
                    if (deltaY > 0) {
                        v.setTranslationY(initialTranslationY + deltaY);
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                    float currentDeltaY = event.getRawY() - initialY;
                    // Если смахнули вниз больше чем на 1/4 экрана — закрываем
                    if (currentDeltaY > v.getHeight() / 4f) {
                        closeWithAnimation();
                    } else {
                        // Иначе возвращаем экран на место с легкой анимацией
                        v.animate()
                                .translationY(0)
                                .setDuration(200)
                                .start();
                    }
                    return true;
            }
            return false;
        });
    }

    private void closeWithAnimation() {
        finish();
        // Плавное затухание при закрытии
        overridePendingTransition(0, android.R.anim.fade_out);
    }
}