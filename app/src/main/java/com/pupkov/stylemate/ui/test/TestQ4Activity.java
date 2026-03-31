package com.pupkov.stylemate.ui.test;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.pupkov.stylemate.R;
import com.pupkov.stylemate.model.TestViewModel;
import com.pupkov.stylemate.ui.AuthActivity;

public class TestQ4Activity extends AppCompatActivity {
    private TestViewModel viewModel;
    int ans = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(TestViewModel.class);
        super.onCreate(savedInstanceState);

        // 2. ПОДПИСКА НА ДАННЫЕ (СНИЗУ ВВЕРХ)
        // Следим за статусом сессии. Если ViewModel скажет "false", уходим.
        viewModel.getSessionValidState().observe(this, isValid -> {
            if (!isValid) {
                Toast.makeText(this, "Время сессии истекло. Пожалуйста, зарегистрируйтесь.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(TestQ4Activity.this, AuthActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Запускаем проверку сессии (событие сверху вниз)
        viewModel.checkSession();

        EdgeToEdge.enable(this);
        setContentView(R.layout.question4);

        ImageButton test1Button = findViewById(R.id.btnTest14);
        ImageButton test2Button = findViewById(R.id.btnTest24);
        ImageButton test3Button = findViewById(R.id.btnTest34);
        ImageButton test4Button = findViewById(R.id.btnTest44);
        ImageButton nextButton = findViewById(R.id.btnNextQuestTest4);
        ImageButton skipButton = findViewById(R.id.btnSkipQuestTest4);


        test1Button.setOnClickListener(v -> {
            ans = 1;
            test1Button.setBackgroundResource(R.drawable.ic_pic6);
            test2Button.setBackgroundResource(R.drawable.ic_pic5);
            test3Button.setBackgroundResource(R.drawable.ic_pic5);
            test4Button.setBackgroundResource(R.drawable.ic_pic5);
        });

        test2Button.setOnClickListener(v -> {
            ans = 2;
            test1Button.setBackgroundResource(R.drawable.ic_pic5);
            test2Button.setBackgroundResource(R.drawable.ic_pic6);
            test3Button.setBackgroundResource(R.drawable.ic_pic5);
            test4Button.setBackgroundResource(R.drawable.ic_pic5);
        });

        test3Button.setOnClickListener(v -> {
            ans = 3;
            test1Button.setBackgroundResource(R.drawable.ic_pic5);
            test2Button.setBackgroundResource(R.drawable.ic_pic5);
            test3Button.setBackgroundResource(R.drawable.ic_pic6);
            test4Button.setBackgroundResource(R.drawable.ic_pic5);
        });

        test4Button.setOnClickListener(v -> {
            ans = 4;
            test1Button.setBackgroundResource(R.drawable.ic_pic5);
            test2Button.setBackgroundResource(R.drawable.ic_pic5);
            test3Button.setBackgroundResource(R.drawable.ic_pic5);
            test4Button.setBackgroundResource(R.drawable.ic_pic6);
        });

        // 2. КНОПКА ДАЛЕЕ
        nextButton.setOnClickListener(v -> {
            if (ans != -1) {
                // А. Сообщаем ViewModel о выборе (2 - номер вопроса)
                // Внутри ViewModel сама вызовет репозиторий и сохранит прогресс
                viewModel.processAnswer(4, ans);

                // Переход к 5-му вопросу
                Intent intent = new Intent(TestQ4Activity.this, TestQ5Activity.class);
                startActivity(intent);
            } else {
                Toast.makeText(TestQ4Activity.this, "Вы не выбрали ни один из вариантов", Toast.LENGTH_LONG).show();
            }
        });

        // 3. КНОПКА ПРОПУСТИТЬ
        skipButton.setOnClickListener(v -> {
            // Сохраняем только время активности
            viewModel.saveProgressOnly();

            Intent intent = new Intent(TestQ4Activity.this, TestQ5Activity.class);
            startActivity(intent);
        });
    }
}