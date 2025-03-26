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
import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;
    private List<Product> filteredList;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
        this.filteredList = new ArrayList<>(productList);
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = filteredList.get(position);

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

        holder.favoriteIcon.setImageResource(product.isFavorite() ? R.drawable.heart_pressed : R.drawable.heart_unpressed);

        holder.favoriteIcon.setOnClickListener(v -> {
            if (auth.getCurrentUser() == null) {
                Toast.makeText(context, "Пожалуйста, авторизуйтесь", Toast.LENGTH_SHORT).show();
                return;
            }

            product.setFavorite(!product.isFavorite());
            holder.favoriteIcon.setImageResource(product.isFavorite() ? R.drawable.heart_pressed : R.drawable.heart_unpressed);

            String userId = auth.getCurrentUser().getUid();
            if (product.isFavorite()) {
                db.collection("users").document(userId)
                        .collection("favorites").document(product.getArticle())
                        .set(product)
                        .addOnSuccessListener(aVoid -> {
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                db.collection("users").document(userId)
                        .collection("favorites").document(product.getArticle())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });

        holder.addToCartButton.setOnClickListener(v -> {
            if (auth.getCurrentUser() == null) {
                Toast.makeText(context, "Пожалуйста, авторизуйтесь", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = auth.getCurrentUser().getUid();
            product.setQuantity(1);
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
        return filteredList.size();
    }

    public void filter(String query, String priceSort, String category) {
        filteredList.clear();
        for (Product product : productList) {
            if (query.isEmpty() || product.getArticle().toLowerCase().contains(query.toLowerCase())) {
                if (category.equals("Все") ||
                        (category.equals("Игровые ПК") && product.getCategory() == 0) ||
                        (category.equals("Рабочие станции") && product.getCategory() == 1)) {
                    filteredList.add(product);
                }
            }
        }

        if (priceSort.equals("По убыванию")) {
            filteredList.sort((p1, p2) -> Long.compare(p2.getPrice(), p1.getPrice()));
        } else if (priceSort.equals("По возрастанию")) {
            filteredList.sort((p1, p2) -> Long.compare(p1.getPrice(), p2.getPrice()));
        }

        notifyDataSetChanged();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage, favoriteIcon;
        TextView titleTextView, articleTextView, priceTextView;
        Button addToCartButton;

        public ProductViewHolder(@NonNull View itemView) {
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