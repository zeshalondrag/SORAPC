package com.example.sorapc;

import java.io.Serializable;

/**
 * Класс Product представляет товар в приложении SORAPC.
 *
 * Основное назначение:
 * - Хранение информации о товаре, используемой в приложении, включая характеристики, цену, количество, категорию и статус избранного.
 * - Позволяет передавать объекты товаров между компонентами приложения благодаря реализации интерфейса Serializable.
 *
 * Основные поля:
 * - article: Уникальный артикул товара.
 * - caseName: Название корпуса товара.
 * - cooling: Тип системы охлаждения.
 * - cpu: Процессор.
 * - description: Описание товара.
 * - gpu: Видеокарта.
 * - img: URL изображения товара.
 * - motherboard: Материнская плата.
 * - power: Блок питания.
 * - price: Цена товара.
 * - ram: Оперативная память.
 * - ssd: Тип и объем SSD.
 * - title: Название товара.
 * - isFavorite: Статус избранного (true - в избранном).
 * - category: Категория товара.
 * - quantity: Доступное количество на складе.
 * - salesCount: Количество продаж товара.
 *
 * Конструкторы:
 * - Product(): Пустой конструктор для сериализации/десериализации.
 * - Product(String article, String caseName, String cooling, String cpu, String description, ...): Конструктор для инициализации всех полей.
 *
 * Геттеры и сеттеры:
 * - Предоставляют доступ к каждому полю и позволяют изменять их значения.
 *
 * Особенности:
 * - Поле category изменено с int на String для хранения текстовых категорий.
 * - Поля isFavorite и quantity позволяют управлять статусом избранного и количеством товара в реальном времени.
 * - Поддерживает сериализацию для передачи объекта между компонентами Android.
 */

public class Product implements Serializable {
    private String article;
    private String caseName;
    private String cooling;
    private String cpu;
    private String description;
    private String gpu;
    private String img;
    private String motherboard;
    private String power;
    private long price;
    private String ram;
    private String ssd;
    private String title;
    private boolean isFavorite;
    private String category; // Изменено с int на String
    private int quantity;
    private long salesCount;

    public Product() {}

    public Product(String article, String caseName, String cooling, String cpu, String description,
                   String gpu, String img, String motherboard, String power, long price,
                   String ram, String ssd, String title, boolean isFavorite, String category, int quantity, long salesCount) {
        this.article = article;
        this.caseName = caseName;
        this.cooling = cooling;
        this.cpu = cpu;
        this.description = description;
        this.gpu = gpu;
        this.img = img;
        this.motherboard = motherboard;
        this.power = power;
        this.price = price;
        this.ram = ram;
        this.ssd = ssd;
        this.title = title;
        this.isFavorite = isFavorite;
        this.category = category;
        this.quantity = quantity;
        this.salesCount = salesCount;
    }

    public String getArticle() { return article; }
    public void setArticle(String article) { this.article = article; }

    public String getCaseName() { return caseName; }
    public void setCaseName(String caseName) { this.caseName = caseName; }

    public String getCooling() { return cooling; }
    public void setCooling(String cooling) { this.cooling = cooling; }

    public String getCpu() { return cpu; }
    public void setCpu(String cpu) { this.cpu = cpu; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getGpu() { return gpu; }
    public void setGpu(String gpu) { this.gpu = gpu; }

    public String getImg() { return img; }
    public void setImg(String img) { this.img = img; }

    public String getMotherboard() { return motherboard; }
    public void setMotherboard(String motherboard) { this.motherboard = motherboard; }

    public String getPower() { return power; }
    public void setPower(String power) { this.power = power; }

    public long getPrice() { return price; }
    public void setPrice(long price) { this.price = price; }

    public String getRam() { return ram; }
    public void setRam(String ram) { this.ram = ram; }

    public String getSsd() { return ssd; }
    public void setSsd(String ssd) { this.ssd = ssd; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public long getSalesCount() { return salesCount; }
    public void setSalesCount(long salesCount) { this.salesCount = salesCount; }
}