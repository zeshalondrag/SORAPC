package com.example.sorapc;

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

/**
 * <summary>
 * Класс OrdersHistoryActivity предоставляет пользователю интерфейс для просмотра истории заказов.
 * Загружает данные о заказах текущего пользователя из Firebase Firestore и отображает их в RecyclerView.
 * Предоставляет уведомление, если история заказов пуста.
 * </summary>
 */

public class OrdersHistoryActivity extends AppCompatActivity {
    private ImageView backIcon;
    private TextView noOrdersText;
    private RecyclerView ordersList;
    private List<Order> orders = new ArrayList<>();
    private OrderAdapter orderAdapter;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders_history);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        backIcon = findViewById(R.id.back_icon);
        noOrdersText = findViewById(R.id.no_orders_text);
        ordersList = findViewById(R.id.orders_list);

        ordersList.setLayoutManager(new LinearLayoutManager(this));
        orderAdapter = new OrderAdapter(orders);
        ordersList.setAdapter(orderAdapter);

        backIcon.setOnClickListener(v -> onBackPressed());

        View headerView = findViewById(R.id.header);
        new Header(headerView, this);

        loadOrders();
    }

    private void loadOrders() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Пожалуйста, авторизуйтесь", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .collection("orders")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Ошибка загрузки заказов: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    orders.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot document : value) {
                            Order order = document.toObject(Order.class);
                            orders.add(order);
                        }
                    }

                    if (orders.isEmpty()) {
                        noOrdersText.setVisibility(View.VISIBLE);
                        ordersList.setVisibility(View.GONE);
                    } else {
                        noOrdersText.setVisibility(View.GONE);
                        ordersList.setVisibility(View.VISIBLE);
                    }

                    orderAdapter.notifyDataSetChanged();
                });
    }
}