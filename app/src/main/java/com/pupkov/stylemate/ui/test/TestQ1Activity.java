package com.pupkov.stylemate.ui.test;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.pupkov.stylemate.R;
import com.pupkov.stylemate.repository.ActiveUserInfo;
import com.pupkov.stylemate.ui.AuthActivity;
import com.pupkov.stylemate.model.TestViewModel;

public class TestQ1Activity extends AppCompatActivity {
    private TestViewModel viewModel;

    int ans = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActiveUserInfo.setDefaults("isTest1", "yes", TestQ1Activity.this);

        viewModel = new ViewModelProvider(this).get(TestViewModel.class);
        super.onCreate(savedInstanceState);

        // 2. ПОДПИСКА НА ДАННЫЕ (СНИЗУ ВВЕРХ)
        // Следим за статусом сессии. Если ViewModel скажет "false", уходим.
        viewModel.getSessionValidState().observe(this, isValid -> {
            if (!isValid) {
                Toast.makeText(this, "Время сессии истекло. Пожалуйста, зарегистрируйтесь.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(TestQ1Activity.this, AuthActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Запускаем проверку сессии (событие сверху вниз)
        viewModel.checkSession();

        EdgeToEdge.enable(this);
        setContentView(R.layout.question1);

        ImageButton test1Button = findViewById(R.id.btnTest11);
        ImageButton test2Button = findViewById(R.id.btnTest21);
        ImageButton test3Button = findViewById(R.id.btnTest31);
        ImageButton test4Button = findViewById(R.id.btnTest41);
        ImageButton test5Button = findViewById(R.id.btnTest51);
        ImageButton nextButton = findViewById(R.id.btnNextQuestTest1);
        ImageButton skipButton = findViewById(R.id.btnSkipQuestTest1);

        // Логика переключения кнопок (визуал) остается без изменений
        test1Button.setOnClickListener(v -> {
            ans = 1;
            test1Button.setBackgroundResource(R.drawable.ic_pic6);
            test2Button.setBackgroundResource(R.drawable.ic_pic5);
            test3Button.setBackgroundResource(R.drawable.ic_pic5);
            test4Button.setBackgroundResource(R.drawable.ic_pic5);
            test5Button.setBackgroundResource(R.drawable.ic_pic5);
        });

        test2Button.setOnClickListener(v -> {
            ans = 2;
            test1Button.setBackgroundResource(R.drawable.ic_pic5);
            test2Button.setBackgroundResource(R.drawable.ic_pic6);
            test3Button.setBackgroundResource(R.drawable.ic_pic5);
            test4Button.setBackgroundResource(R.drawable.ic_pic5);
            test5Button.setBackgroundResource(R.drawable.ic_pic5);
        });

        test3Button.setOnClickListener(v -> {
            ans = 3;
            test1Button.setBackgroundResource(R.drawable.ic_pic5);
            test2Button.setBackgroundResource(R.drawable.ic_pic5);
            test3Button.setBackgroundResource(R.drawable.ic_pic6);
            test4Button.setBackgroundResource(R.drawable.ic_pic5);
            test5Button.setBackgroundResource(R.drawable.ic_pic5);
        });

        test4Button.setOnClickListener(v -> {
            ans = 4;
            test1Button.setBackgroundResource(R.drawable.ic_pic5);
            test2Button.setBackgroundResource(R.drawable.ic_pic5);
            test3Button.setBackgroundResource(R.drawable.ic_pic5);
            test4Button.setBackgroundResource(R.drawable.ic_pic6);
            test5Button.setBackgroundResource(R.drawable.ic_pic5);
        });

        test5Button.setOnClickListener(v -> {
            ans = 5;
            test1Button.setBackgroundResource(R.drawable.ic_pic5);
            test2Button.setBackgroundResource(R.drawable.ic_pic5);
            test3Button.setBackgroundResource(R.drawable.ic_pic5);
            test4Button.setBackgroundResource(R.drawable.ic_pic5);
            test5Button.setBackgroundResource(R.drawable.ic_pic6);
        });

        // 2. ЛОГИКА КНОПКИ "ДАЛЕЕ"
        nextButton.setOnClickListener(v -> {
            if (ans != -1) {
                // А. Сообщаем ViewModel о выборе (1 - номер вопроса)
                // Внутри ViewModel сама вызовет репозиторий и сохранит прогресс
                viewModel.processAnswer(1, ans);

                // В. Переходим дальше
                Intent intent = new Intent(TestQ1Activity.this, TestQ2Activity.class);
                startActivity(intent);
            } else {
                Toast.makeText(TestQ1Activity.this, "Вы не выбрали ни один из вариантов", Toast.LENGTH_LONG).show();
            }
        });

        // 3. ЛОГИКА КНОПКИ "ПРОПУСТИТЬ"
        skipButton.setOnClickListener(v -> {
            // Баллы НЕ начисляем (ans нас не интересует)

            // Сохраняем только время активности
            viewModel.saveProgressOnly();

            Intent intent = new Intent(TestQ1Activity.this, TestQ2Activity.class);
            startActivity(intent);
        });
    }
}