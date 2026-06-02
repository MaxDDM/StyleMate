package com.pupkov.stylemate.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.pupkov.stylemate.R;
import com.pupkov.stylemate.analytics.TimeAnalytics;
import com.pupkov.stylemate.repository.UserRepository;

import java.time.LocalDateTime;

public class MainActivity extends AppCompatActivity {

    private View bgHomeSelected, bgProfileSelected;
    private ImageView iconHome, iconProfile;

    private final Fragment homeFragment = new HomeFragment();
    private final Fragment profileFragment = new ProfileFragment();
    private final UserRepository repo = new UserRepository();

    private Fragment activeFragment = homeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (repo.isLogged(MainActivity.this)) {
            TimeAnalytics.saveDate(LocalDateTime.now(), repo.getUID());
        }

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        View btnHome = findViewById(R.id.btnHome);
        View btnProfile = findViewById(R.id.btnProfile);
        bgHomeSelected = findViewById(R.id.bgHomeSelected);
        bgProfileSelected = findViewById(R.id.bgProfileSelected);
        iconHome = findViewById(R.id.iconHome);
        iconProfile = findViewById(R.id.iconProfile);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, homeFragment, "HOME")
                .commit();

        updateIcons(true);

        handleNavigationIntent(getIntent());

        btnHome.setOnClickListener(v -> {
            switchFragment(homeFragment);
            updateIcons(true);
        });

        btnProfile.setOnClickListener(v -> {
            switchFragment(profileFragment);
            updateIcons(false);
        });
    }

    private void switchFragment(Fragment targetFragment) {
        if (activeFragment == targetFragment) return;

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        transaction.hide(activeFragment);

        if (!targetFragment.isAdded()) {
            transaction.add(R.id.fragment_container, targetFragment);
        } else {
            transaction.show(targetFragment);
        }

        transaction.commit();
        activeFragment = targetFragment;
    }

    private void updateIcons(boolean isHomeActive) {
        int activeColor = Color.parseColor("#3D5AFE");
        int inactiveColor = ContextCompat.getColor(this, R.color.collection_text);

        if (isHomeActive) {
            iconHome.setColorFilter(activeColor);
            bgHomeSelected.setVisibility(View.VISIBLE);
            iconProfile.setColorFilter(inactiveColor);
            bgProfileSelected.setVisibility(View.GONE);
        } else {
            iconHome.setColorFilter(inactiveColor);
            bgHomeSelected.setVisibility(View.GONE);
            iconProfile.setColorFilter(activeColor);
            bgProfileSelected.setVisibility(View.VISIBLE);
        }
    }

    private void handleNavigationIntent(Intent intent) {
        if (intent != null && "PROFILE".equals(intent.getStringExtra("OPEN_TAB"))) {
            overridePendingTransition(0, 0);

            switchFragment(profileFragment);
            updateIcons(false);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleNavigationIntent(intent);
    }
}