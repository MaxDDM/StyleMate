package com.pupkov.stylemate.ui.new_select_test;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.pupkov.stylemate.R;
import com.pupkov.stylemate.ui.test.TestQ1Activity;

public class NewSelectQ1Activity extends AppCompatActivity {
    // Индекс выбора: 1 — по ситуации, 2 — по стилю
    int ans = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.new_select1);

        ImageButton test1Button = findViewById(R.id.btnSelTest11);
        ImageButton test2Button = findViewById(R.id.btnSelTest21);
        ImageButton nextButton = findViewById(R.id.btnNextQuestSelTest1);
        ImageButton skipButton = findViewById(R.id.btnSkipQuestSelTest1);

        test1Button.setOnClickListener(v -> {
            ans = 1;
            test1Button.setBackgroundResource(R.drawable.ic_pic6);
            test2Button.setBackgroundResource(R.drawable.ic_pic5);
        });

        test2Button.setOnClickListener(v -> {
            ans = 2;
            test1Button.setBackgroundResource(R.drawable.ic_pic5);
            test2Button.setBackgroundResource(R.drawable.ic_pic6);
        });

        // Главная развилка: направляем пользователя по нужному сценарию тестирования
        nextButton.setOnClickListener(v -> {
            if (ans != -1) {
                if (ans == 1) {
                    // Переход к выбору конкретных ситуаций
                    Intent intent = new Intent(NewSelectQ1Activity.this, NewSelectQ9Activity.class);
                    startActivity(intent);
                } else {
                    // Возврат к стандартному тесту определения общего стиля
                    Intent intent = new Intent(NewSelectQ1Activity.this, TestQ1Activity.class);
                    startActivity(intent);
                }
            } else {
                Toast.makeText(NewSelectQ1Activity.this, "Вы не выбрали ни один из вариантов", Toast.LENGTH_LONG).show();
            }
        });

        // При пропуске по умолчанию отправляем на сценарий выбора ситуаций
        skipButton.setOnClickListener(v -> {
            Intent intent = new Intent(NewSelectQ1Activity.this, NewSelectQ9Activity.class);
            startActivity(intent);
        });
    }
}