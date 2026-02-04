package com.example.stylemate.ui.test;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stylemate.R;
import com.example.stylemate.ui.ActiveUserInfo;

public class TestQ4Activity extends AppCompatActivity {
    int ans = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        nextButton.setOnClickListener(v -> {
            if (ans != -1) {
                ActiveUserInfo.setDefaults("test" + ans, String.valueOf(ans), TestQ4Activity.this);
            } else {
                Toast.makeText(TestQ4Activity.this, "Вы не выбрали ни один из вариантов", Toast.LENGTH_LONG).show();
                return;
            }

            Intent intent = new Intent(TestQ4Activity.this, TestQ5Activity.class);
            startActivity(intent);
        });

        skipButton.setOnClickListener(v -> {
            Intent intent = new Intent(TestQ4Activity.this, TestQ5Activity.class);
            startActivity(intent);
        });
    }
}