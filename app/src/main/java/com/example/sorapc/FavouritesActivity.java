package com.example.sorapc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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
    private ImageView backIcon;
    private TextView emptyFavouritesText, empty_favourites_hint_text;

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

        backIcon = findViewById(R.id.back_icon);
        favouritesRecyclerView = findViewById(R.id.favourites_recycler_view);
        emptyFavouritesText = findViewById(R.id.empty_favourites_text);
        empty_favourites_hint_text = findViewById(R.id.empty_favourites_hint_text);

        favouritesList = new ArrayList<>();
        favouritesAdapter = new FavouritesAdapter(this, favouritesList);
        favouritesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        favouritesRecyclerView.setAdapter(favouritesAdapter);

        backIcon.setOnClickListener(v -> onBackPressed());

        loadFavourites();

        View headerView = findViewById(R.id.header);
        new Header(headerView, this);
    }

    private void loadFavourites() {
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .collection("favorites")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Ошибка загрузки избранного: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<Product> newFavouritesList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : value) {
                        Product product = document.toObject(Product.class);
                        product.setFavorite(true);
                        newFavouritesList.add(product);
                    }

                    favouritesList.clear();
                    favouritesList.addAll(newFavouritesList);

                    emptyFavouritesText.setVisibility(favouritesList.isEmpty() ? View.VISIBLE : View.GONE);
                    empty_favourites_hint_text.setVisibility(favouritesList.isEmpty() ? View.VISIBLE : View.GONE);
                    favouritesRecyclerView.setVisibility(favouritesList.isEmpty() ? View.GONE : View.VISIBLE);

                    favouritesAdapter.notifyDataSetChanged();
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}