package com.example.sorapc;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Класс BottomNavigation представляет собой обработчик для нижней навигационной панели приложения.
 *
 * Основное назначение:
 * - Обеспечивает переключение между основными экранами приложения (Главная, Каталог, Корзина, Поддержка).
 * - Следит за состоянием выбранного элемента на панели навигации.
 *
 * Основные функции:
 * - Инициализация нижней навигационной панели и установка слушателя для обработки нажатий.
 * - Обработка нажатий пользователя и навигация на соответствующий экран, если текущий экран отличается.
 *
 * Особенности:
 * - Проверяет, находится ли пользователь уже на выбранной активности, чтобы избежать лишних переходов.
 * - Завершает текущую активность при переходе на другую для предотвращения дублирования экранов.
 */

public class BottomNavigation implements BottomNavigationView.OnNavigationItemSelectedListener {

    private final Activity activity;
    private final BottomNavigationView bottomNavigationView;

    public BottomNavigation(Activity activity, int selectedItemId) {
        this.activity = activity;
        this.bottomNavigationView = activity.findViewById(R.id.bottomNavigation);
        this.bottomNavigationView.setOnNavigationItemSelectedListener(this);
        this.bottomNavigationView.setSelectedItemId(selectedItemId);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.bottom_main) {
            if (!(activity instanceof MainActivity)) {
                Intent intent = new Intent(activity, MainActivity.class);
                activity.startActivity(intent);
                activity.finish();
            }
            return true;
        } else if (itemId == R.id.bottom_catalog) {
            if (!(activity instanceof CatalogActivity)) {
                Intent intent = new Intent(activity, CatalogActivity.class);
                activity.startActivity(intent);
                activity.finish();
            }
            return true;
        } else if (itemId == R.id.bottom_cart) {
            if (!(activity instanceof CartActivity)) {
                Intent intent = new Intent(activity, CartActivity.class);
                activity.startActivity(intent);
                activity.finish();
            }
            return true;
        } else if (itemId == R.id.bottom_support) {
            if (!(activity instanceof SupportActivity)) {
                Intent intent = new Intent(activity, SupportActivity.class);
                activity.startActivity(intent);
                activity.finish();
            }
            return true;
        }

        return false;
    }
}