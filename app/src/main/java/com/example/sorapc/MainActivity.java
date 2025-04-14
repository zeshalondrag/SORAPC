package com.example.sorapc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * <summary>
 * Класс MainActivity представляет главный экран приложения SORAPC.
 * Обеспечивает отображение промо-акций, популярных товаров и навигацию по категориям каталога.
 * Поддерживает интеграцию с Firebase Firestore для загрузки данных и Glide для отображения изображений.
 * </summary>
 */

public class MainActivity extends AppCompatActivity {

    private ViewPager2 promotionsPager;
    private ViewPager2 topProductsPager;
    private Button gamingPcButton, workstationButton;
    private ImageView gamingPcImage, workstationImage;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        promotionsPager = findViewById(R.id.promotions_pager);
        topProductsPager = findViewById(R.id.top_products_pager);
        gamingPcButton = findViewById(R.id.gaming_pc_button);
        workstationButton = findViewById(R.id.workstation_button);
        gamingPcImage = findViewById(R.id.gaming_pc_image);
        workstationImage = findViewById(R.id.workstation_image);

        setupPromotionsPager();
        setupTopProductsPager();

        Glide.with(this)
                .load("https://storage.yandexcloud.net/sorapc/%D0%9F%D1%80%D0%BE%D1%87%D0%B5%D0%B5/%D0%98%D0%B3%D1%80%D0%BE%D0%B2%D1%8B%D0%B5%20%D0%9F%D0%9A%20(%D0%9A%D0%B0%D1%80%D1%82%D0%BE%D1%87%D0%BA%D0%B0).png")
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(gamingPcImage);

        Glide.with(this)
                .load("https://storage.yandexcloud.net/sorapc/%D0%9F%D1%80%D0%BE%D1%87%D0%B5%D0%B5/%D0%A0%D0%B0%D0%B1%D0%BE%D1%87%D0%B8%D0%B5%20%D1%81%D1%82%D0%B0%D0%BD%D1%86%D0%B8%D0%B8%20(%D0%9A%D0%B0%D1%80%D1%82%D0%BE%D1%87%D0%BA%D0%B0).png")
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(workstationImage);

        gamingPcButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CatalogActivity.class);
            intent.putExtra("category", "Игровые ПК");
            startActivity(intent);
        });

        workstationButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CatalogActivity.class);
            intent.putExtra("category", "Рабочие станции");
            startActivity(intent);
        });

        View headerView = findViewById(R.id.header);
        new Header(headerView, this);
        new BottomNavigation(this, R.id.bottom_main);
    }

    private void setupPromotionsPager() {
        List<Promotion> promotions = new ArrayList<>();
        promotions.add(new Promotion("Розыгрыш SORAPC DYNAMIC\nВыйграй мощный ПК.", "https://storage.yandexcloud.net/sorapc/%D0%91%D0%B0%D0%BD%D0%B5%D1%80/%D0%A0%D0%BE%D0%B7%D1%8B%D0%B3%D1%80%D1%8B%D1%88%20DYNAMIC.png"));
        promotions.add(new Promotion("NVIDIA GeForce RTX 50 Series\nУже в компьютерах SORAPC.", "https://storage.yandexcloud.net/sorapc/%D0%91%D0%B0%D0%BD%D0%B5%D1%80/NVIDIA%20GeForce%20RTX%2050%20Series.png"));
        promotions.add(new Promotion("Kaspersky Premium\nПри покупке ПК SORAPC.", "https://storage.yandexcloud.net/sorapc/%D0%91%D0%B0%D0%BD%D0%B5%D1%80/Kaspersky%20Premium.png"));

        PromotionAdapter adapter = new PromotionAdapter(promotions);
        promotionsPager.setAdapter(adapter);

        promotionsPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                promotionsPager.postDelayed(() -> {
                    int nextPosition = (position + 1) % promotions.size();
                    promotionsPager.setCurrentItem(nextPosition, true);
                }, 3500);
            }
        });
    }

    private void setupTopProductsPager() {
        db.collection("products")
                .orderBy("salesCount", Query.Direction.DESCENDING)
                .limit(3)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> topProducts = new ArrayList<>();
                    for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                        Product product = queryDocumentSnapshots.getDocuments().get(i).toObject(Product.class);
                        if (product != null) {
                            topProducts.add(product);
                        }
                    }

                    TopProductAdapter adapter = new TopProductAdapter(topProducts);
                    topProductsPager.setAdapter(adapter);

                    topProductsPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                        @Override
                        public void onPageSelected(int position) {
                            super.onPageSelected(position);
                            topProductsPager.postDelayed(() -> {
                                int nextPosition = (position + 1) % topProducts.size();
                                topProductsPager.setCurrentItem(nextPosition, true);
                            }, 3500);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                });
    }
}

class Promotion {
    private String title;
    private String imageUrl;

    public Promotion(String title, String imageUrl) {
        this.title = title;
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}

class PromotionAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<PromotionAdapter.PromotionViewHolder> {
    private List<Promotion> promotions;

    public PromotionAdapter(List<Promotion> promotions) {
        this.promotions = promotions;
    }

    @Override
    public PromotionViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
        View view = android.view.LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_promotion, parent, false);
        return new PromotionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PromotionViewHolder holder, int position) {
        Promotion promotion = promotions.get(position);
        holder.titleTextView.setText(promotion.getTitle());
        com.bumptech.glide.Glide.with(holder.itemView.getContext())
                .load(promotion.getImageUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return promotions.size();
    }

    static class PromotionViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        android.widget.ImageView imageView;
        android.widget.TextView titleTextView;

        public PromotionViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.promotion_image);
            titleTextView = itemView.findViewById(R.id.promotion_title);
        }
    }
}

class TopProductAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<TopProductAdapter.TopProductViewHolder> {
    private List<Product> products;

    public TopProductAdapter(List<Product> products) {
        this.products = products;
    }

    @Override
    public TopProductViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
        View view = android.view.LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_top_product, parent, false);
        return new TopProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TopProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.titleTextView.setText(product.getTitle());
        com.bumptech.glide.Glide.with(holder.itemView.getContext())
                .load(product.getImg())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class TopProductViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        android.widget.ImageView imageView;
        android.widget.TextView titleTextView;

        public TopProductViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.top_product_image);
            titleTextView = itemView.findViewById(R.id.top_product_title);
        }
    }
}