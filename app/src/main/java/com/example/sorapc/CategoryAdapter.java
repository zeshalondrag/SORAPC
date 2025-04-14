package com.example.sorapc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Класс CategoryAdapter представляет собой адаптер для отображения списка категорий в RecyclerView.
 *
 * Основное назначение:
 * - Отображение данных о категориях, включая их название.
 * - Обеспечение возможности редактирования и удаления категории через соответствующие кнопки.
 *
 * Основные функции:
 * - Отображение списка категорий с использованием layout-файла (item_category).
 * - Реализация интерфейса обратного вызова OnCategoryActionListener для обработки действий пользователя
 *   (редактирование и удаление категорий).
 *
 * Поля:
 * - categories: Список объектов Category, представляющих категории.
 * - context: Контекст активности или фрагмента, где используется адаптер.
 * - actionListener: Интерфейс обратного вызова для обработки действий пользователя.
 *
 * Вложенный класс:
 * - CategoryViewHolder: ViewHolder, содержащий ссылки на элементы интерфейса для отображения и управления категориями.
 */

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categories;
    private Context context;
    private OnCategoryActionListener actionListener;

    public interface OnCategoryActionListener {
        void onEditCategory(Category category);
        void onDeleteCategory(Category category);
    }

    public CategoryAdapter(Context context, List<Category> categories, OnCategoryActionListener listener) {
        this.context = context;
        this.categories = categories;
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.categoryTitle.setText(category.getTitle());
        holder.editButton.setOnClickListener(v -> actionListener.onEditCategory(category));
        holder.deleteButton.setOnClickListener(v -> actionListener.onDeleteCategory(category));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryTitle;
        Button editButton, deleteButton;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryTitle = itemView.findViewById(R.id.category_title);
            editButton = itemView.findViewById(R.id.edit_category_button);
            deleteButton = itemView.findViewById(R.id.delete_category_button);
        }
    }
}