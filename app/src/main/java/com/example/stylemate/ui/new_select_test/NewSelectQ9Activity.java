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

public class NewSelectQ9Activity extends AppCompatActivity {
    int ans = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.new_select9);

        ImageButton test1Button = findViewById(R.id.btnSelTest19);
        ImageButton test2Button = findViewById(R.id.btnSelTest29);
        ImageButton test3Button = findViewById(R.id.btnSelTest39);
        ImageButton test4Button = findViewById(R.id.btnSelTest49);
        ImageButton nextButton = findViewById(R.id.btnNextQuestSelTest9);
        ImageButton skipButton = findViewById(R.id.btnSkipQuestSelTest9);


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
                ActiveUserInfo.setDefaults("testSel" + ans, String.valueOf(ans), NewSelectQ9Activity.this);
            } else {
                Toast.makeText(NewSelectQ9Activity.this, "Вы не выбрали ни один из вариантов", Toast.LENGTH_LONG).show();
                return;
            }

            Intent intent;

            switch(ans) {
                case 1:
                    intent = new Intent(NewSelectQ9Activity.this, NewSelectQ10Activity.class);
                    startActivity(intent);
                    break;
                case 2:
                    intent = new Intent(NewSelectQ9Activity.this, NewSelectQ13Activity.class);
                    startActivity(intent);
                    break;
                case 3:
                    intent = new Intent(NewSelectQ9Activity.this, NewSelectQ12Activity.class);
                    startActivity(intent);
                    break;
                case 4:
                    intent = new Intent(NewSelectQ9Activity.this, NewSelectQ11Activity.class);
                    startActivity(intent);
                    break;
            }
        });

        skipButton.setOnClickListener(v -> {
            // тут будет переход на главную
        });
    }
}