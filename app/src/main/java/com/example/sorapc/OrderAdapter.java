package com.example.sorapc;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * <summary>
 * Класс OrderAdapter используется для отображения списка заказов в RecyclerView.
 * Предоставляет информацию о каждом заказе, включая дату, товары с количеством и итоговую сумму.
 * </summary>
 */

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private List<Order> orders;

    public OrderAdapter(List<Order> orders) {
        this.orders = orders;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.orderDate.setText(new SimpleDateFormat("dd.MM.yyyy HH:mm").format(order.getDate()));

        StringBuilder itemsText = new StringBuilder();
        for (Product product : order.getItems()) {
            itemsText.append(product.getTitle()).append(" (x").append(product.getQuantity()).append(")\n");
        }
        holder.orderItems.setText(itemsText.toString().trim());

        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        DecimalFormat decimalFormat = new DecimalFormat("#,### ₽", symbols);
        holder.orderTotal.setText("Итого: " + decimalFormat.format(order.getFinalTotal()));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderDate, orderItems, orderTotal;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderDate = itemView.findViewById(R.id.order_date);
            orderItems = itemView.findViewById(R.id.order_items);
            orderTotal = itemView.findViewById(R.id.order_total);
        }
    }
}