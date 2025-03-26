package com.example.sorapc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;

public class FavouritesAdapter extends RecyclerView.Adapter<FavouritesAdapter.FavouritesViewHolder> {

    private Context context;
    private List<Product> favouritesList;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public FavouritesAdapter(Context context, List<Product> favouritesList) {
        this.context = context;
        this.favouritesList = favouritesList;
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public FavouritesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new FavouritesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavouritesViewHolder holder, int position) {
        Product product = favouritesList.get(position);

        holder.titleTextView.setText(product.getTitle());
        holder.articleTextView.setText("Артикул: " + product.getArticle());

        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        DecimalFormat decimalFormat = new DecimalFormat("#,### ₽", symbols);
        String formattedPrice = decimalFormat.format(product.getPrice());
        holder.priceTextView.setText("Цена: " + formattedPrice);

        Glide.with(context)
                .load(product.getImg())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.productImage);

        holder.favoriteIcon.setImageResource(R.drawable.heart_pressed);

        holder.favoriteIcon.setOnClickListener(v -> {
            if (auth.getCurrentUser() == null) {
                Toast.makeText(context, "Пожалуйста, авторизуйтесь", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = auth.getCurrentUser().getUid();
            db.collection("users").document(userId)
                    .collection("favorites").document(product.getArticle())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        holder.addToCartButton.setOnClickListener(v -> {
            if (auth.getCurrentUser() == null) {
                Toast.makeText(context, "Пожалуйста, авторизуйтесь", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = auth.getCurrentUser().getUid();
            db.collection("users").document(userId)
                    .collection("cart").document(product.getArticle())
                    .set(product)
                    .addOnSuccessListener(aVoid -> {
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public int getItemCount() {
        return favouritesList.size();
    }

    static class FavouritesViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage, favoriteIcon;
        TextView titleTextView, articleTextView, priceTextView;
        Button addToCartButton;

        public FavouritesViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image);
            favoriteIcon = itemView.findViewById(R.id.favorite_icon);
            titleTextView = itemView.findViewById(R.id.product_title);
            articleTextView = itemView.findViewById(R.id.product_article);
            priceTextView = itemView.findViewById(R.id.product_price);
            addToCartButton = itemView.findViewById(R.id.add_to_cart_button);
        }
    }
}