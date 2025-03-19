package com.example.sorapc;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Header {
    private Context context;
    private FirebaseAuth mAuth;

    public Header(View headerView, Context context) {
        this.context = context;
        this.mAuth = FirebaseAuth.getInstance();

        ImageView logo = headerView.findViewById(R.id.logo2);
        ImageView profile = headerView.findViewById(R.id.profile);

        logo.setOnClickListener(v -> {
            Intent intent = new Intent(context, MainActivity.class);
            context.startActivity(intent);
        });

        profile.setOnClickListener(v -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(context,
                        "Авторизируйтесь или зарегистрируйтесь",
                        Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(context, ProfileActivity.class);
                context.startActivity(intent);
            }
        });
    }
}