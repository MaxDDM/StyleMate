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
import com.example.stylemate.ui.test.TestQ1Activity;
import com.example.stylemate.ui.test.TestQ2Activity;

public class NewSelectQ1Activity extends AppCompatActivity {
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

        nextButton.setOnClickListener(v -> {
            if (ans != -1) {
                ActiveUserInfo.setDefaults("testSel" + ans, String.valueOf(ans), NewSelectQ1Activity.this);
            } else {
                Toast.makeText(NewSelectQ1Activity.this, "Вы не выбрали ни один из вариантов", Toast.LENGTH_LONG).show();
                return;
            }

            Intent intent = new Intent(NewSelectQ1Activity.this, NewSelectQ2Activity.class);
            startActivity(intent);
        });

        skipButton.setOnClickListener(v -> {
            Intent intent = new Intent(NewSelectQ1Activity.this, NewSelectQ2Activity.class);
            startActivity(intent);
        });
    }
}