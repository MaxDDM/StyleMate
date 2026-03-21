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
import com.example.stylemate.ui.AuthActivity;
import com.example.stylemate.ui.MainActivity;
import com.example.stylemate.ui.RegisterActivity;
import com.example.stylemate.ui.SetSelectionNameActivity;

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
                int situation_id = ans;

                Intent intent = new Intent(NewSelectQ10Activity.this, SetSelectionNameActivity.class);
                intent.putExtra("testNumber", 2);
                intent.putExtra("situation_id", situation_id);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(NewSelectQ10Activity.this, "Вы не выбрали ни один из вариантов", Toast.LENGTH_LONG).show();
            }
        });

        skipButton.setOnClickListener(v -> {
            Intent intent = new Intent(NewSelectQ10Activity.this, MainActivity.class);
            startActivity(intent);
        });
    }
}