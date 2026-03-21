package com.example.stylemate.ui.new_select_test;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.stylemate.R;
import com.example.stylemate.model.TestViewModel;
import com.example.stylemate.repository.ActiveUserInfo;
import com.example.stylemate.repository.SituationsRepository;
import com.example.stylemate.ui.MainActivity;
import com.example.stylemate.ui.SetSelectionNameActivity;

public class NewSelectQ14Activity extends AppCompatActivity {
    int ans = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.new_select14);

        ImageButton test1Button = findViewById(R.id.btnSelTest114);
        ImageButton test2Button = findViewById(R.id.btnSelTest214);
        ImageButton test3Button = findViewById(R.id.btnSelTest314);
        ImageButton test4Button = findViewById(R.id.btnSelTest414);
        ImageButton nextButton = findViewById(R.id.btnNextQuestSelTest14);
        ImageButton skipButton = findViewById(R.id.btnSkipQuestSelTest14);


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
                int situation_id = -1;
                switch(ans) {
                    case 1:
                        situation_id = 10;
                        break;
                    case 2:
                        situation_id = 11;
                        break;
                    case 3:
                        situation_id = 11;
                    case 4:
                        situation_id = 12;
                }

                Intent intent = new Intent(NewSelectQ14Activity.this, SetSelectionNameActivity.class);
                intent.putExtra("testNumber", 2);
                intent.putExtra("situation_id", situation_id);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(NewSelectQ14Activity.this, "Вы не выбрали ни один из вариантов", Toast.LENGTH_LONG).show();
            }
        });

        skipButton.setOnClickListener(v -> {
            Intent intent = new Intent(NewSelectQ14Activity.this, MainActivity.class);
            startActivity(intent);
        });
    }
}