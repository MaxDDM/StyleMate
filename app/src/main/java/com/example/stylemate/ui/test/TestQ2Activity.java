package com.example.stylemate.ui.test;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.stylemate.R;
import com.example.stylemate.model.TestViewModel;
import com.example.stylemate.ui.AuthActivity;

public class TestQ2Activity extends AppCompatActivity {
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
                Intent intent = new Intent(TestQ2Activity.this, AuthActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Запускаем проверку сессии (событие сверху вниз)
        viewModel.checkSession();

        EdgeToEdge.enable(this);
        setContentView(R.layout.question2);

        ImageButton test1Button = findViewById(R.id.btnTest12);
        ImageButton test2Button = findViewById(R.id.btnTest22);
        ImageButton test3Button = findViewById(R.id.btnTest32);
        ImageButton test4Button = findViewById(R.id.btnTest42);
        ImageButton nextButton = findViewById(R.id.btnNextQuestTest2);
        ImageButton skipButton = findViewById(R.id.btnSkipQuestTest2);


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

        // ЛОГИКА КНОПКИ "ДАЛЕЕ"
        nextButton.setOnClickListener(v -> {
            if (ans != -1) {
                // А. Сообщаем ViewModel о выборе (2 - номер вопроса)
                // Внутри ViewModel сама вызовет репозиторий и сохранит прогресс
                viewModel.processAnswer(2, ans);

                // В. Переходим к 3-му вопросу
                Intent intent = new Intent(TestQ2Activity.this, TestQ3Activity.class);
                startActivity(intent);
            } else {
                Toast.makeText(TestQ2Activity.this, "Вы не выбрали ни один из вариантов", Toast.LENGTH_LONG).show();
            }
        });

        // ЛОГИКА КНОПКИ "ПРОПУСТИТЬ"
        skipButton.setOnClickListener(v -> {
            // Сохраняем только время активности
            viewModel.saveProgressOnly();

            Intent intent = new Intent(TestQ2Activity.this, TestQ3Activity.class);
            startActivity(intent);
        });
    }
}