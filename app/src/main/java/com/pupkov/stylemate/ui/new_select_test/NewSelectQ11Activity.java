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

public class NewSelectQ11Activity extends AppCompatActivity {
    int ans = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.new_select11);

        ImageButton test1Button = findViewById(R.id.btnSelTest111);
        ImageButton test2Button = findViewById(R.id.btnSelTest211);
        ImageButton test3Button = findViewById(R.id.btnSelTest311);
        ImageButton test4Button = findViewById(R.id.btnSelTest411);
        ImageButton nextButton = findViewById(R.id.btnNextQuestSelTest11);
        ImageButton skipButton = findViewById(R.id.btnSkipQuestSelTest11);


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
                int situation_id = ans + 3;

                Intent intent = new Intent(NewSelectQ11Activity.this, SetSelectionNameActivity.class);
                intent.putExtra("testNumber", 2);
                intent.putExtra("situation_id", situation_id);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(NewSelectQ11Activity.this, "Вы не выбрали ни один из вариантов", Toast.LENGTH_LONG).show();
            }
        });

        skipButton.setOnClickListener(v -> {
            Intent intent = new Intent(NewSelectQ11Activity.this, MainActivity.class);
            startActivity(intent);
        });
    }
}