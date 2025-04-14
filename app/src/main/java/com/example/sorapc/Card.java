package com.example.sorapc;

/**
 * Класс Card представляет собой модель данных для хранения информации о банковской карте.
 *
 * Основное назначение:
 * - Содержит данные о карте, включая номер карты, срок действия, CVV-код и уникальный идентификатор документа (cardId).
 * - Предоставляет геттеры и сеттеры для доступа и изменения данных.
 *
 * Поля:
 * - cardId: Уникальный идентификатор документа карты (например, ID в базе данных).
 * - cardNumber: Номер банковской карты.
 * - expiryDate: Срок действия карты в формате MM/YY.
 * - cvv: Трехзначный код безопасности карты.
 *
 * Конструкторы:
 * - Пустой конструктор для инициализации объекта без параметров.
 * - Конструктор с параметрами для инициализации основных полей карты (cardNumber, expiryDate, cvv).
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