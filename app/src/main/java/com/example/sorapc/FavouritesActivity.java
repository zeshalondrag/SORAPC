package com.example.sorapc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FavouritesActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private RecyclerView favouritesRecyclerView;
    private FavouritesAdapter favouritesAdapter;
    private List<Product> favouritesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Пожалуйста, авторизуйтесь", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        favouritesRecyclerView = findViewById(R.id.favourites_recycler_view);
        favouritesList = new ArrayList<>();
        favouritesAdapter = new FavouritesAdapter(this, favouritesList);
        favouritesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        favouritesRecyclerView.setAdapter(favouritesAdapter);

        loadFavourites();

        View headerView = findViewById(R.id.header);
        new Header(headerView, this);
        new BottomNavigation(this, R.id.bottom_favourites);
    }

    private void loadFavourites() {
        TextView emptyText = findViewById(R.id.empty_favourites_text);
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .collection("favorites")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Ошибка загрузки избранного: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    favouritesList.clear();
                    for (QueryDocumentSnapshot document : value) {
                        Product product = document.toObject(Product.class);
                        favouritesList.add(product);
                    }
                    favouritesAdapter.notifyDataSetChanged();
                    emptyText.setVisibility(favouritesList.isEmpty() ? View.VISIBLE : View.GONE);
                    favouritesRecyclerView.setVisibility(favouritesList.isEmpty() ? View.GONE : View.VISIBLE);
                });
    }
}