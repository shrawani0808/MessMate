package com.example.messmate.owner.ui;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.messmate.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class OwnerMainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.owneractivity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ownermain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        bottomNavigationView = findViewById(R.id.ownerBottomNavigation);

        // Load Default Fragment (Dashboard)
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.owner_fragment_container, new OwnerDashboardFragment())
                    .commit();
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_owner_dashboard) {
                selectedFragment = new OwnerDashboardFragment();
            } else if (itemId == R.id.navigation_owner_orders) {
                selectedFragment = new OwnerOrdersFragment();
            } else if (itemId == R.id.navigation_owner_menu) {
                selectedFragment = new OwnerMenuFragment();
            } else if (itemId == R.id.navigation_owner_members) {
                selectedFragment = new OwnerMembersFragment();
            } else if (itemId == R.id.navigation_owner_more) {
                selectedFragment = new OwnerMoreFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.owner_fragment_container, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });
    }
}