package com.example.sorapc;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNavigation implements BottomNavigationView.OnNavigationItemSelectedListener {

    private final Activity activity;
    private final BottomNavigationView bottomNavigationView;

    public BottomNavigation(Activity activity, int selectedItemId) {
        this.activity = activity;
        this.bottomNavigationView = activity.findViewById(R.id.bottomNavigation);
        this.bottomNavigationView.setOnNavigationItemSelectedListener(this);
        this.bottomNavigationView.setSelectedItemId(selectedItemId);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.bottom_main) {
            if (!(activity instanceof MainActivity)) {
                Intent intent = new Intent(activity, MainActivity.class);
                activity.startActivity(intent);
                activity.finish();
            }
            return true;
        } else if (itemId == R.id.bottom_catalog) {
            if (!(activity instanceof CatalogActivity)) {
                Intent intent = new Intent(activity, CatalogActivity.class);
                activity.startActivity(intent);
                activity.finish();
            }
            return true;
        } else if (itemId == R.id.bottom_cart) {
            if (!(activity instanceof CartActivity)) {
                Intent intent = new Intent(activity, CartActivity.class);
                activity.startActivity(intent);
                activity.finish();
            }
            return true;
        }

        return false;
    }
}