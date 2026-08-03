package com.example.messmate.presentation.member;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.messmate.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MemberMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_member_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.membermain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigationView bottomNavigationView =
                findViewById(R.id.memberBottomNavigation);

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.memberNavHostFragment);

        if (navHostFragment != null) {

            NavController navController =
                    navHostFragment.getNavController();

            NavigationUI.setupWithNavController(
                    bottomNavigationView,
                    navController
            );
        }
    }
}