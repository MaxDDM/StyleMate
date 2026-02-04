package com.example.stylemate.ui.new_select_test;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.stylemate.R;
import com.example.stylemate.ui.ActiveUserInfo;

public class NewSelectQ10Activity extends AppCompatActivity {
    int ans = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.new_select10);

        ImageButton test1Button = findViewById(R.id.btnSelTest110);
        ImageButton test2Button = findViewById(R.id.btnSelTest210);
        ImageButton test3Button = findViewById(R.id.btnSelTest310);
        ImageButton nextButton = findViewById(R.id.btnNextQuestSelTest10);
        ImageButton skipButton = findViewById(R.id.btnSkipQuestSelTest10);


        test1Button.setOnClickListener(v -> {
            ans = 1;

            test1Button.setBackgroundResource(R.drawable.ic_pic6);
            test2Button.setBackgroundResource(R.drawable.ic_pic5);
            test3Button.setBackgroundResource(R.drawable.ic_pic5);
        });

        test2Button.setOnClickListener(v -> {
            ans = 2;

            test1Button.setBackgroundResource(R.drawable.ic_pic5);
            test2Button.setBackgroundResource(R.drawable.ic_pic6);
            test3Button.setBackgroundResource(R.drawable.ic_pic5);
        });

        test3Button.setOnClickListener(v -> {
            ans = 3;

            test1Button.setBackgroundResource(R.drawable.ic_pic5);
            test2Button.setBackgroundResource(R.drawable.ic_pic5);
            test3Button.setBackgroundResource(R.drawable.ic_pic6);
        });

        nextButton.setOnClickListener(v -> {
            if (ans != -1) {
                ActiveUserInfo.setDefaults("testSel" + ans, String.valueOf(ans), NewSelectQ10Activity.this);
            } else {
                Toast.makeText(NewSelectQ10Activity.this, "Вы не выбрали ни один из вариантов", Toast.LENGTH_LONG).show();
                return;
            }

            // тут будет переход на главную
        });

        skipButton.setOnClickListener(v -> {
            // тут будет переход на главную
        });
    }
}