package com.example.stylemate.ui.test;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.stylemate.repository.ActiveUserInfo;
import com.example.stylemate.ui.MainActivity; // <-- Убедись, что это твоя главная активность
import com.example.stylemate.R;
import com.example.stylemate.model.TestViewModel;
import com.example.stylemate.ui.AuthActivity;

public class TestQ5Activity extends AppCompatActivity {
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
                Intent intent = new Intent(TestQ5Activity.this, AuthActivity.class);
                startActivity(intent);
                finish();
            }
        });

        viewModel.getWinnerStyle().observe(this, winnerStyleIndex -> {
            // Этот код сработает, когда ViewModel закончит считать

            Toast.makeText(this, "Тест завершен! Победил стиль №: " + winnerStyleIndex, Toast.LENGTH_LONG).show();

            // Переход на главную
            Intent intent = new Intent(TestQ5Activity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        viewModel.checkSession(); // Запуск проверки

        EdgeToEdge.enable(this);
        setContentView(R.layout.question5);

        ImageButton test1Button = findViewById(R.id.btnTest15);
        ImageButton test2Button = findViewById(R.id.btnTest25);
        ImageButton test3Button = findViewById(R.id.btnTest35);
        ImageButton test4Button = findViewById(R.id.btnTest45);
        ImageButton test5Button = findViewById(R.id.btnTest55);
        ImageButton test6Button = findViewById(R.id.btnTest65);
        ImageButton nextButton = findViewById(R.id.btnNextQuestTest5);
        ImageButton skipButton = findViewById(R.id.btnSkipQuestTest5);


        // Обработчики нажатий (визуал)
        test1Button.setOnClickListener(v -> {
            ans = 1;
            setSelections(test1Button, test2Button, test3Button, test4Button, test5Button, test6Button);
        });

        test2Button.setOnClickListener(v -> {
            ans = 2;
            setSelections(test2Button, test1Button, test3Button, test4Button, test5Button, test6Button);
        });

        test3Button.setOnClickListener(v -> {
            ans = 3;
            setSelections(test3Button, test1Button, test2Button, test4Button, test5Button, test6Button);
        });

        test4Button.setOnClickListener(v -> {
            ans = 4;
            setSelections(test4Button, test1Button, test2Button, test3Button, test5Button, test6Button);
        });

        test5Button.setOnClickListener(v -> {
            ans = 5;
            setSelections(test5Button, test1Button, test2Button, test3Button, test4Button, test6Button);
        });

        test6Button.setOnClickListener(v -> {
            ans = 6; // Это вариант "Не определился"
            setSelections(test6Button, test1Button, test2Button, test3Button, test4Button, test5Button);
        });

        // 2. ФИНАЛ: КНОПКА ДАЛЕЕ
        nextButton.setOnClickListener(v -> {
            ActiveUserInfo.setDefaults("isTest1", "", TestQ5Activity.this);

            if (ans != -1) {
                // Шаг 1: Записываем ответ
                viewModel.processAnswer(5, ans);

                // Шаг 2: Просим посчитать результат
                // Мы НЕ переходим никуда вручную. Мы ждем, пока сработает observer выше (пункт 2).
                viewModel.calculateResult();
            } else {
                Toast.makeText(TestQ5Activity.this, "Вы не выбрали ни один из вариантов", Toast.LENGTH_LONG).show();
            }
        });

        // 3. ФИНАЛ: КНОПКА ПРОПУСТИТЬ
        skipButton.setOnClickListener(v -> {
            ActiveUserInfo.setDefaults("isTest1", "", TestQ5Activity.this);

            // Просто просим результат (без записи ответа)
            viewModel.calculateResult();
        });
    }

    // Вспомогательный метод, чтобы не копировать код смены картинок 6 раз
    private void setSelections(ImageButton selected, ImageButton... others) {
        selected.setBackgroundResource(R.drawable.ic_pic6); // Выбран
        for (ImageButton other : others) {
            other.setBackgroundResource(R.drawable.ic_pic5); // Не выбран
        }
    }
}