package com.pupkov.stylemate.ui.new_select_test;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.pupkov.stylemate.R;
import com.pupkov.stylemate.ui.MainActivity;
import com.pupkov.stylemate.ui.SetSelectionNameActivity;

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
            setRadioSelection(test1Button, test2Button, test3Button);
        });

        test2Button.setOnClickListener(v -> {
            ans = 2;
            setRadioSelection(test2Button, test1Button, test3Button);
        });

        test3Button.setOnClickListener(v -> {
            ans = 3;
            setRadioSelection(test3Button, test1Button, test2Button);
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

    private void setRadioSelection(ImageButton selected, ImageButton... others) {
        selected.setBackgroundResource(R.drawable.ic_pic6);
        for (ImageButton other : others) {
            other.setBackgroundResource(R.drawable.ic_pic5);
        }
    }
}