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
 * Класс ClientAdapter представляет собой адаптер для отображения списка клиентов в RecyclerView.
 *
 * Основное назначение:
 * - Отображение информации о клиентах, включая имя, фамилию, email и роль.
 * - Реализация возможности изменения роли клиента через интерфейс обратного вызова.
 *
 * Основные функции:
 * - Отображение списка клиентов с использованием layout-файла (item_client).
 * - Предоставление кнопки для изменения роли клиента.
 * - Взаимодействие с интерфейсом OnEditRoleListener для обработки изменения роли.
 *
 * Поля:
 * - clients: Список объектов User, представляющих клиентов.
 * - context: Контекст активности или фрагмента, где используется адаптер.
 * - editRoleListener: Интерфейс обратного вызова для обработки изменения роли клиента.
 *
 * Вложенный класс:
 * - ClientViewHolder: ViewHolder для хранения ссылок на элементы интерфейса, связанные с отображением информации о клиенте.
 */

public class ClientAdapter extends RecyclerView.Adapter<ClientAdapter.ClientViewHolder> {

    private List<User> clients;
    private Context context;
    private OnEditRoleListener editRoleListener;

    public interface OnEditRoleListener {
        void onEditRole(User user);
    }

    public ClientAdapter(Context context, List<User> clients, OnEditRoleListener listener) {
        this.context = context;
        this.clients = clients;
        this.editRoleListener = listener;
    }

    @NonNull
    @Override
    public ClientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_client, parent, false);
        return new ClientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientViewHolder holder, int position) {
        User client = clients.get(position);
        holder.clientName.setText(client.getName() + " " + client.getSurname());
        holder.clientEmail.setText(client.getEmail());
        holder.clientRole.setText("Роль: " + client.getRole());
        holder.editRoleButton.setOnClickListener(v -> editRoleListener.onEditRole(client));
    }

    @Override
    public int getItemCount() {
        return clients.size();
    }

    static class ClientViewHolder extends RecyclerView.ViewHolder {
        TextView clientName, clientEmail, clientRole;
        Button editRoleButton;

        public ClientViewHolder(@NonNull View itemView) {
            super(itemView);
            clientName = itemView.findViewById(R.id.client_name);
            clientEmail = itemView.findViewById(R.id.client_email);
            clientRole = itemView.findViewById(R.id.client_role);
            editRoleButton = itemView.findViewById(R.id.edit_role_button);
        }
    }
}