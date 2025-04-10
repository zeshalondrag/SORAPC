package com.example.sorapc;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavouritesAdapter extends RecyclerView.Adapter<FavouritesAdapter.FavouritesViewHolder> {

    private Context context;
    private List<Product> favouritesList;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private Map<String, Product> cartItems; // Для хранения товаров в корзине

    public FavouritesAdapter(Context context, List<Product> favouritesList) {
        this.context = context;
        this.favouritesList = favouritesList;
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
        this.cartItems = new HashMap<>();
        loadCartItems(); // Загружаем товары из корзины при инициализации
    }

    private void loadCartItems() {
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .collection("cart")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(context, "Ошибка загрузки корзины: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    cartItems.clear();
                    for (QueryDocumentSnapshot document : value) {
                        Product product = document.toObject(Product.class);
                        cartItems.put(product.getArticle(), product);
                    }
                    notifyDataSetChanged(); // Обновляем отображение
                });
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

        // Проверяем, есть ли товар в корзине
        Product cartProduct = cartItems.get(product.getArticle());
        if (cartProduct != null) {
            // Товар в корзине — показываем quantity_layout
            holder.addToCartButton.setVisibility(View.GONE);
            holder.quantityLayout.setVisibility(View.VISIBLE);
            holder.quantityText.setText(String.valueOf(cartProduct.getQuantity()));
        } else {
            // Товара нет в корзине — показываем кнопку "В корзину"
            holder.addToCartButton.setVisibility(View.VISIBLE);
            holder.quantityLayout.setVisibility(View.GONE);
        }

        // Проверяем количество товара
        if (product.getQuantity() <= 0) {
            holder.addToCartButton.setText("Нет товара");
            holder.addToCartButton.setEnabled(false);
            holder.addToCartButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.disabled_button_color)));
            holder.quantityLayout.setVisibility(View.GONE);
        } else {
            holder.addToCartButton.setText("В корзину");
            holder.addToCartButton.setEnabled(true);
            holder.addToCartButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.Aquamarine)));
        }

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

            if (product.getQuantity() <= 0) {
                Toast.makeText(context, "Товара нет в наличии", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = auth.getCurrentUser().getUid();
            product.setQuantity(1); // Устанавливаем количество 1 при добавлении в корзину
            db.collection("users").document(userId)
                    .collection("cart").document(product.getArticle())
                    .set(product)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Товар добавлен в корзину", Toast.LENGTH_SHORT).show();
                        // После добавления в корзину обновляем UI
                        cartItems.put(product.getArticle(), product);
                        holder.addToCartButton.setVisibility(View.GONE);
                        holder.quantityLayout.setVisibility(View.VISIBLE);
                        holder.quantityText.setText(String.valueOf(product.getQuantity()));
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        holder.decreaseQuantityButton.setOnClickListener(v -> {
            if (auth.getCurrentUser() == null) {
                Toast.makeText(context, "Пожалуйста, авторизуйтесь", Toast.LENGTH_SHORT).show();
                return;
            }

            Product cartItem = cartItems.get(product.getArticle());
            if (cartItem != null) {
                int quantity = cartItem.getQuantity();
                if (quantity > 1) {
                    quantity--;
                    cartItem.setQuantity(quantity);
                    String userId = auth.getCurrentUser().getUid();
                    int finalQuantity = quantity;
                    db.collection("users").document(userId)
                            .collection("cart").document(product.getArticle())
                            .set(cartItem)
                            .addOnSuccessListener(aVoid -> {
                                holder.quantityText.setText(String.valueOf(finalQuantity));
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    // Если количество становится 0, удаляем из корзины
                    String userId = auth.getCurrentUser().getUid();
                    db.collection("users").document(userId)
                            .collection("cart").document(product.getArticle())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                cartItems.remove(product.getArticle());
                                holder.addToCartButton.setVisibility(View.VISIBLE);
                                holder.quantityLayout.setVisibility(View.GONE);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            }
        });

        holder.increaseQuantityButton.setOnClickListener(v -> {
            if (auth.getCurrentUser() == null) {
                Toast.makeText(context, "Пожалуйста, авторизуйтесь", Toast.LENGTH_SHORT).show();
                return;
            }

            Product cartItem = cartItems.get(product.getArticle());
            if (cartItem != null) {
                String userId = auth.getCurrentUser().getUid();
                // Проверяем доступное количество товара в Firestore
                db.collection("products")
                        .whereEqualTo("article", product.getArticle())
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                Long availableQuantity = queryDocumentSnapshots.getDocuments().get(0).getLong("quantity");
                                if (availableQuantity != null) {
                                    int currentQuantity = cartItem.getQuantity();
                                    int newQuantity = currentQuantity + 1;

                                    if (newQuantity <= availableQuantity) {
                                        // Если новое количество не превышает доступное, обновляем
                                        cartItem.setQuantity(newQuantity);
                                        db.collection("users").document(userId)
                                                .collection("cart").document(product.getArticle())
                                                .set(cartItem)
                                                .addOnSuccessListener(aVoid -> {
                                                    holder.quantityText.setText(String.valueOf(newQuantity));
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(context, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    } else {
                                        Toast.makeText(context, "Недостаточно товара на складе", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(context, "Ошибка: не удалось получить доступное количество", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(context, "Товар не найден в базе данных", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Ошибка проверки количества: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    @Override
    public int getItemCount() {
        return favouritesList.size();
    }

    static class FavouritesViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage, favoriteIcon, decreaseQuantityButton, increaseQuantityButton;
        TextView titleTextView, articleTextView, priceTextView, quantityText;
        Button addToCartButton;
        LinearLayout quantityLayout;

        public FavouritesViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image);
            favoriteIcon = itemView.findViewById(R.id.favorite_icon);
            titleTextView = itemView.findViewById(R.id.product_title);
            articleTextView = itemView.findViewById(R.id.product_article);
            priceTextView = itemView.findViewById(R.id.product_price);
            addToCartButton = itemView.findViewById(R.id.add_to_cart_button);
            quantityLayout = itemView.findViewById(R.id.quantity_layout);
            decreaseQuantityButton = itemView.findViewById(R.id.decrease_quantity_button);
            increaseQuantityButton = itemView.findViewById(R.id.increase_quantity_button);
            quantityText = itemView.findViewById(R.id.quantity_text);
        }
    }
}