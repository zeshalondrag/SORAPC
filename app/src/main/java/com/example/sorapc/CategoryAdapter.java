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
 * <summary>
 * Класс CategoryAdapter используется для отображения списка категорий в RecyclerView.
 * Он предоставляет возможность редактирования и удаления категорий с помощью интерфейса обратного вызова.
 * </summary>
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