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
 * Класс OrdersHistoryActivity представляет собой активность для отображения истории заказов пользователя.
 *
 * Основное назначение:
 * - Предоставление пользователю удобного интерфейса для просмотра всех оформленных заказов.
 * - Загрузка данных о заказах из Firebase Firestore и отображение их в RecyclerView.
 *
 * Основные функции:
 * - Загрузка и отображение списка заказов, связанных с текущим пользователем.
 * - Указание, если у пользователя пока нет оформленных заказов.
 * - Обновление интерфейса в реальном времени при изменении данных о заказах в базе данных.
 *
 * Поля:
 * - backIcon: Иконка для возврата на предыдущий экран.
 * - noOrdersText: Текстовое поле для отображения сообщения об отсутствии заказов.
 * - ordersList: RecyclerView для отображения списка заказов.
 * - orders: Список объектов Order, представляющих заказы.
 * - orderAdapter: Адаптер для управления отображением заказов.
 * - auth: Экземпляр FirebaseAuth для проверки авторизации пользователя.
 * - db: Экземпляр FirebaseFirestore для работы с базой данных.
 *
 * Методы:
 * - onCreate: Выполняет инициализацию интерфейса и вызывает загрузку заказов.
 * - loadOrders: Загружает заказы текущего пользователя из Firestore и обновляет интерфейс.
 *
 * Особенности:
 * - Проверка авторизации пользователя перед загрузкой заказов.
 * - Интеграция с Firebase Firestore для получения данных о заказах.
 * - Динамическое обновление списка заказов при изменении данных в Firestore.
 * - Удобный пользовательский интерфейс с сообщением о пустой истории заказов.
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