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
 * Класс ClientAdapter используется для отображения списка клиентов в RecyclerView.
 * Он предоставляет возможность отображения информации о клиентах и изменения их роли через интерфейс обратного вызова.
 * </summary>
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