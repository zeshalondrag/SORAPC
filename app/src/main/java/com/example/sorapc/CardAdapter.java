package com.example.sorapc;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Класс CardAdapter представляет собой адаптер для отображения списка банковских карт в RecyclerView.
 *
 * Основное назначение:
 * - Отображение данных о банковских картах, таких как номер карты (частично скрытый) и срок действия.
 * - Обеспечение функциональности для управления списком карт, включая удаление элементов.
 *
 * Основные функции:
 * - Инициализация карточек в RecyclerView с использованием layout-файла (item_card).
 * - Обработка отображения частично скрытого номера карты (показываются только последние 4 цифры).
 * - Удаление карты из списка с обновлением интерфейса.
 *
 * Поля:
 * - cards: Список объектов Card, представляющих данные о картах.
 *
 * Вложенный класс:
 * - CardViewHolder: ViewHolder для хранения ссылок на элементы интерфейса, связанные с отображением карты.
 */

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {
    private List<Card> cards;

    public CardAdapter(List<Card> cards) {
        this.cards = cards;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        Card card = cards.get(position);
        holder.cardNumber.setText("**** **** **** " + card.getCardNumber().substring(15));
        holder.cardExpiry.setText(card.getExpiryDate());
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    public void removeCard(int position) {
        cards.remove(position);
        notifyItemRemoved(position);
    }

    public Card getCard(int position) {
        return cards.get(position);
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        TextView cardNumber, cardExpiry;

        CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardNumber = itemView.findViewById(R.id.card_number);
            cardExpiry = itemView.findViewById(R.id.card_expiry);
        }
    }
}