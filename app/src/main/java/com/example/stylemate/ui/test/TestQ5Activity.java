package com.example.stylemate.ui.test;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stylemate.R;
import com.example.stylemate.ui.ActiveUserInfo;

public class TestQ5Activity extends AppCompatActivity {
    int ans = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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


        test1Button.setOnClickListener(v -> {
            ans = 1;

            test1Button.setBackgroundResource(R.drawable.ic_pic6);
            test2Button.setBackgroundResource(R.drawable.ic_pic5);
            test3Button.setBackgroundResource(R.drawable.ic_pic5);
            test4Button.setBackgroundResource(R.drawable.ic_pic5);
            test5Button.setBackgroundResource(R.drawable.ic_pic5);
            test6Button.setBackgroundResource(R.drawable.ic_pic5);
        });

        test2Button.setOnClickListener(v -> {
            ans = 2;

            test1Button.setBackgroundResource(R.drawable.ic_pic5);
            test2Button.setBackgroundResource(R.drawable.ic_pic6);
            test3Button.setBackgroundResource(R.drawable.ic_pic5);
            test4Button.setBackgroundResource(R.drawable.ic_pic5);
            test5Button.setBackgroundResource(R.drawable.ic_pic5);
            test6Button.setBackgroundResource(R.drawable.ic_pic5);
        });

        test3Button.setOnClickListener(v -> {
            ans = 3;

            test1Button.setBackgroundResource(R.drawable.ic_pic5);
            test2Button.setBackgroundResource(R.drawable.ic_pic5);
            test3Button.setBackgroundResource(R.drawable.ic_pic6);
            test4Button.setBackgroundResource(R.drawable.ic_pic5);
            test5Button.setBackgroundResource(R.drawable.ic_pic5);
            test6Button.setBackgroundResource(R.drawable.ic_pic5);
        });

        test4Button.setOnClickListener(v -> {
            ans = 4;

            test1Button.setBackgroundResource(R.drawable.ic_pic5);
            test2Button.setBackgroundResource(R.drawable.ic_pic5);
            test3Button.setBackgroundResource(R.drawable.ic_pic5);
            test4Button.setBackgroundResource(R.drawable.ic_pic6);
            test5Button.setBackgroundResource(R.drawable.ic_pic5);
            test6Button.setBackgroundResource(R.drawable.ic_pic5);
        });

        test5Button.setOnClickListener(v -> {
            ans = 5;

            test1Button.setBackgroundResource(R.drawable.ic_pic5);
            test2Button.setBackgroundResource(R.drawable.ic_pic5);
            test3Button.setBackgroundResource(R.drawable.ic_pic5);
            test4Button.setBackgroundResource(R.drawable.ic_pic5);
            test5Button.setBackgroundResource(R.drawable.ic_pic6);
            test6Button.setBackgroundResource(R.drawable.ic_pic5);
        });

        test6Button.setOnClickListener(v -> {
            ans = 6;

            test1Button.setBackgroundResource(R.drawable.ic_pic5);
            test2Button.setBackgroundResource(R.drawable.ic_pic5);
            test3Button.setBackgroundResource(R.drawable.ic_pic5);
            test4Button.setBackgroundResource(R.drawable.ic_pic5);
            test5Button.setBackgroundResource(R.drawable.ic_pic5);
            test6Button.setBackgroundResource(R.drawable.ic_pic6);
        });

        nextButton.setOnClickListener(v -> {
            if (ans != -1) {
                ActiveUserInfo.setDefaults("test" + ans, String.valueOf(ans), TestQ5Activity.this);
            } else {
                Toast.makeText(TestQ5Activity.this, "Вы не выбрали ни один из вариантов", Toast.LENGTH_LONG).show();
                return;
            }

            // тут будет переход на главную
        });

        skipButton.setOnClickListener(v -> {
            // тут будет переход на главную
        });
    }
}