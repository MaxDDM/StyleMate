package com.pupkov.stylemate.ui.new_select_test;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.pupkov.stylemate.R;
import com.pupkov.stylemate.model.TestViewModel;
import com.pupkov.stylemate.repository.ActiveUserInfo;
import com.pupkov.stylemate.repository.SituationsRepository;
import com.pupkov.stylemate.ui.MainActivity;
import com.pupkov.stylemate.ui.SetSelectionNameActivity;

public class NewSelectQ12Activity extends AppCompatActivity {
    int ans = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.new_select12);

        ImageButton test1Button = findViewById(R.id.btnSelTest112);
        ImageButton test2Button = findViewById(R.id.btnSelTest212);
        ImageButton nextButton = findViewById(R.id.btnNextQuestSelTest12);
        ImageButton skipButton = findViewById(R.id.btnSkipQuestSelTest12);


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
                int situation_id = ans + 7;

                Intent intent = new Intent(NewSelectQ12Activity.this, SetSelectionNameActivity.class);
                intent.putExtra("testNumber", 2);
                intent.putExtra("situation_id", situation_id);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(NewSelectQ12Activity.this, "Вы не выбрали ни один из вариантов", Toast.LENGTH_LONG).show();
            }
        });

        skipButton.setOnClickListener(v -> {
            Intent intent = new Intent(NewSelectQ12Activity.this, MainActivity.class);
            startActivity(intent);
        });
    }
}