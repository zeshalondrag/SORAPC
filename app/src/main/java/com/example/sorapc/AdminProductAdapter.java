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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.AdminProductViewHolder> {

    private Context context;
    private List<Product> productList;
    private List<Product> filteredList;
    private FirebaseFirestore db;
    private OnEditProductListener editProductListener;
    private OnDeleteProductListener deleteProductListener; // Callback для удаления

    // Интерфейс для обработки нажатия на кнопку "Изменить"
    public interface OnEditProductListener {
        void onEditProduct(Product product);
    }

    // Интерфейс для обработки нажатия на кнопку "Удалить"
    public interface OnDeleteProductListener {
        void onDeleteProduct(Product product);
    }

    public AdminProductAdapter(Context context, List<Product> productList, OnEditProductListener editListener, OnDeleteProductListener deleteListener) {
        this.context = context;
        this.productList = productList;
        this.filteredList = new ArrayList<>(productList);
        this.db = FirebaseFirestore.getInstance();
        this.editProductListener = editListener;
        this.deleteProductListener = deleteListener;
    }

    @NonNull
    @Override
    public AdminProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.admin_item_product, parent, false);
        return new AdminProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminProductViewHolder holder, int position) {
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

        holder.editProductButton.setOnClickListener(v -> {
            if (editProductListener != null) {
                editProductListener.onEditProduct(product);
            }
        });

        holder.deleteProductButton.setOnClickListener(v -> {
            if (deleteProductListener != null) {
                deleteProductListener.onDeleteProduct(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        for (Product product : productList) {
            if (query.isEmpty() ||
                    product.getArticle().toLowerCase().contains(query.toLowerCase()) ||
                    product.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(product);
            }
        }

        notifyDataSetChanged();
    }

    static class AdminProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView titleTextView, articleTextView, priceTextView;
        Button editProductButton, deleteProductButton;

        public AdminProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image);
            titleTextView = itemView.findViewById(R.id.product_title);
            articleTextView = itemView.findViewById(R.id.product_article);
            priceTextView = itemView.findViewById(R.id.product_price);
            editProductButton = itemView.findViewById(R.id.edit_product_button);
            deleteProductButton = itemView.findViewById(R.id.delete_product_button);
        }
    }
}