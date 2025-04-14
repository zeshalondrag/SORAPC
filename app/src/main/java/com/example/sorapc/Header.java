package com.example.sorapc;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Класс Header реализует логику работы заголовка (header) приложения.
 *
 * Основное назначение:
 * - Обеспечивает взаимодействие с элементами заголовка, такими как логотип и профиль.
 * - Реагирует на события кликов по элементам заголовка.
 *
 * Основные функции:
 * - Перенаправление пользователя на главный экран (MainActivity) при нажатии на логотип.
 * - Проверка авторизации пользователя и отображение профиля (ProfileActivity) при нажатии на иконку профиля.
 * - Уведомление пользователя через Toast, если он не авторизован.
 *
 * Поля:
 * - context: Контекст активности или фрагмента, где используется заголовок.
 * - mAuth: Экземпляр FirebaseAuth для проверки авторизации пользователя.
 *
 * Конструктор:
 * - Header(View headerView, Context context): Инициализирует элементы заголовка и задаёт обработчики событий для логотипа и иконки профиля.
 *
 * Особенности:
 * - Интеграция с FirebaseAuth для проверки состояния авторизации пользователя.
 * - Использует Intent для перехода между экранами приложения.
 * - Отображает уведомления через Toast для неавторизованных пользователей.
 */

public class Header {
    private Context context;
    private FirebaseAuth mAuth;

    public Header(View headerView, Context context) {
        this.context = context;
        this.mAuth = FirebaseAuth.getInstance();

        ImageView logo = headerView.findViewById(R.id.logo2);
        ImageView profile = headerView.findViewById(R.id.profile);

        logo.setOnClickListener(v -> {
            Intent intent = new Intent(context, MainActivity.class);
            context.startActivity(intent);
        });

        profile.setOnClickListener(v -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(context,
                        "Авторизируйтесь или зарегистрируйтесь",
                        Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(context, ProfileActivity.class);
                context.startActivity(intent);
            }
        });
    }
}