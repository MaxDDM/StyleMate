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

public class NewSelectQ13Activity extends AppCompatActivity {
    int ans = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.new_select13);

        ImageButton test1Button = findViewById(R.id.btnSelTest113);
        ImageButton test2Button = findViewById(R.id.btnSelTest213);
        ImageButton test3Button = findViewById(R.id.btnSelTest313);
        ImageButton test4Button = findViewById(R.id.btnSelTest413);
        ImageButton nextButton = findViewById(R.id.btnNextQuestSelTest13);
        ImageButton skipButton = findViewById(R.id.btnSkipQuestSelTest13);


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
                ActiveUserInfo.setDefaults("testSel" + ans, String.valueOf(ans), NewSelectQ13Activity.this);
            } else {
                Toast.makeText(NewSelectQ13Activity.this, "Вы не выбрали ни один из вариантов", Toast.LENGTH_LONG).show();
                return;
            }

            Intent intent = new Intent(NewSelectQ13Activity.this, NewSelectQ14Activity.class);
            startActivity(intent);
        });

        skipButton.setOnClickListener(v -> {
            Intent intent = new Intent(NewSelectQ13Activity.this, NewSelectQ14Activity.class);
            startActivity(intent);
        });
    }
}