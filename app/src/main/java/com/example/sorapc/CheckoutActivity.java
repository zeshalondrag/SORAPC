package com.example.sorapc;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class CheckoutActivity extends AppCompatActivity {
    private ImageView backIcon;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        View headerView = findViewById(R.id.header);
        new Header(headerView, this);

        backIcon = findViewById(R.id.back_icon);
        backIcon.setOnClickListener(v -> onBackPressed());
    }
}
