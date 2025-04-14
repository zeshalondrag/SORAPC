package com.example.sorapc;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * <summary>
 * Класс CardAdapter используется для отображения списка банковских карт в RecyclerView.
 * Он обеспечивает отображение частично скрытых номеров карт и сроков действия, а также
 * предоставляет функционал удаления карты из списка.
 * </summary>
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