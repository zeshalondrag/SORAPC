package com.example.sorapc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

/**
 * <summary>
 * Класс CartAdapter используется для отображения списка товаров в корзине в RecyclerView.
 * Предоставляет функционал управления товарами, включая изменение количества, удаление из корзины,
 * а также добавление или удаление товаров в/из избранного с синхронизацией данных в Firebase Firestore.
 * </summary>
 */

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private List<Product> cartList;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public CartAdapter(Context context, List<Product> cartList) {
        this.context = context;
        this.cartList = cartList;
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_item_product, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Product product = cartList.get(position);

        holder.titleTextView.setText(product.getTitle());
        holder.articleTextView.setText("Артикул: " + product.getArticle());

        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        DecimalFormat decimalFormat = new DecimalFormat("#,### ₽", symbols);
        String formattedPrice = decimalFormat.format(product.getPrice() * product.getQuantity());
        holder.priceTextView.setText("Цена: " + formattedPrice);

        Glide.with(context)
                .load(product.getImg())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.productImage);

        holder.favoriteIcon.setImageResource(product.isFavorite() ? R.drawable.heart_pressed : R.drawable.heart_unpressed);

        holder.quantityText.setText(String.valueOf(product.getQuantity()));

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
                        .addOnFailureListener(e -> Toast.makeText(context, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                db.collection("users").document(userId)
                        .collection("favorites").document(product.getArticle())
                        .delete()
                        .addOnFailureListener(e -> Toast.makeText(context, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });

        holder.binIcon.setOnClickListener(v -> {
            if (auth.getCurrentUser() == null) {
                Toast.makeText(context, "Пожалуйста, авторизуйтесь", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = auth.getCurrentUser().getUid();
            db.collection("users").document(userId)
                    .collection("cart").document(product.getArticle())
                    .delete()
                    .addOnFailureListener(e -> Toast.makeText(context, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        holder.decreaseQuantityButton.setOnClickListener(v -> {
            if (auth.getCurrentUser() == null) {
                Toast.makeText(context, "Пожалуйста, авторизуйтесь", Toast.LENGTH_SHORT).show();
                return;
            }

            int quantity = product.getQuantity();
            if (quantity > 1) {
                quantity--;
                product.setQuantity(quantity);
                holder.quantityText.setText(String.valueOf(quantity));
                String userId = auth.getCurrentUser().getUid();
                db.collection("users").document(userId)
                        .collection("cart").document(product.getArticle())
                        .set(product)
                        .addOnSuccessListener(aVoid -> {
                            String updatedPrice = decimalFormat.format(product.getPrice() * product.getQuantity());
                            holder.priceTextView.setText("Цена: " + updatedPrice);
                        })
                        .addOnFailureListener(e -> Toast.makeText(context, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });

        holder.increaseQuantityButton.setOnClickListener(v -> {
            if (auth.getCurrentUser() == null) {
                Toast.makeText(context, "Пожалуйста, авторизуйтесь", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = auth.getCurrentUser().getUid();
            // Проверяем доступное количество товара в Firestore
            db.collection("products")
                    .whereEqualTo("article", product.getArticle())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            Long availableQuantity = queryDocumentSnapshots.getDocuments().get(0).getLong("quantity");
                            if (availableQuantity != null) {
                                int currentQuantity = product.getQuantity();
                                int newQuantity = currentQuantity + 1;

                                if (newQuantity <= availableQuantity) {
                                    // Если новое количество не превышает доступное, обновляем
                                    product.setQuantity(newQuantity);
                                    holder.quantityText.setText(String.valueOf(newQuantity));
                                    db.collection("users").document(userId)
                                            .collection("cart").document(product.getArticle())
                                            .set(product)
                                            .addOnSuccessListener(aVoid -> {
                                                String updatedPrice = decimalFormat.format(product.getPrice() * product.getQuantity());
                                                holder.priceTextView.setText("Цена: " + updatedPrice);
                                            })
                                            .addOnFailureListener(e -> Toast.makeText(context, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                } else {
                                    // Если превышает, показываем сообщение
                                    Toast.makeText(context, "Недостаточно товара на складе", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(context, "Ошибка: не удалось получить доступное количество", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(context, "Товар не найден в базе данных", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Ошибка проверки количества: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage, favoriteIcon, binIcon, decreaseQuantityButton, increaseQuantityButton;
        TextView titleTextView, articleTextView, priceTextView, quantityText;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image2);
            favoriteIcon = itemView.findViewById(R.id.favorite_icon2);
            binIcon = itemView.findViewById(R.id.bin_icon);
            titleTextView = itemView.findViewById(R.id.product_title);
            articleTextView = itemView.findViewById(R.id.product_article);
            priceTextView = itemView.findViewById(R.id.product_price);
            quantityText = itemView.findViewById(R.id.quantity_text);
            decreaseQuantityButton = itemView.findViewById(R.id.decrease_quantity_button);
            increaseQuantityButton = itemView.findViewById(R.id.increase_quantity_button);
        }
    }
}