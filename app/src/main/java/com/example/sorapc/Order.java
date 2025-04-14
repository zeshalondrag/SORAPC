package com.example.sorapc;

import java.util.Date;
import java.util.List;

/**
 * <summary>
 * Класс Order представляет заказ пользователя, включая список товаров, общую стоимость, комиссию, итоговую сумму, дату заказа и список артикулов.
 * Используется для хранения и обработки информации о заказах в приложении.
 * </summary>
 */

public class Order {
    private List<Product> items;
    private long totalPrice;
    private long commission;
    private long finalTotal;
    private Date date;
    private List<String> articles; // Список артикулов для упрощения поиска

    public Order() {}

    public Order(List<Product> items, long totalPrice, long commission, long finalTotal, Date date, List<String> articles) {
        this.items = items;
        this.totalPrice = totalPrice;
        this.commission = commission;
        this.finalTotal = finalTotal;
        this.date = date;
        this.articles = articles;
    }

    public List<Product> getItems() { return items; }
    public void setItems(List<Product> items) { this.items = items; }
    public long getTotalPrice() { return totalPrice; }
    public void setTotalPrice(long totalPrice) { this.totalPrice = totalPrice; }
    public long getCommission() { return commission; }
    public void setCommission(long commission) { this.commission = commission; }
    public long getFinalTotal() { return finalTotal; }
    public void setFinalTotal(long finalTotal) { this.finalTotal = finalTotal; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    public List<String> getArticles() { return articles; }
    public void setArticles(List<String> articles) { this.articles = articles; }
}