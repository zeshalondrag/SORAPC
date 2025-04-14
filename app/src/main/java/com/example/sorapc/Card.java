package com.example.sorapc;

/**
 * <summary>
 * Класс Card представляет модель данных для хранения информации о банковской карте,
 * включая номер карты, срок действия, CVV-код и уникальный идентификатор документа (cardId).
 * </summary>
 */

public class Card {
    private String cardId; // Новое поле для хранения ID документа карты
    private String cardNumber;
    private String expiryDate;
    private String cvv;

    public Card() {}

    public Card(String cardNumber, String expiryDate, String cvv) {
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
    }

    public String getCardId() { return cardId; }
    public void setCardId(String cardId) { this.cardId = cardId; }
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }
}