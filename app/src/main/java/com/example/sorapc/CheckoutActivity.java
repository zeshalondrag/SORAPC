package com.example.sorapc;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * <summary>
 * Класс CheckoutActivity предоставляет функционал оформления заказа.
 * Предоставляет пользователю возможность выбрать способ оплаты (карта или наличные), рассчитать итоговую сумму с учетом комиссии,
 * завершить оформление заказа с обновлением данных в Firebase Firestore и отправить электронный чек на email пользователя.
 * </summary>
 */

public class CheckoutActivity extends AppCompatActivity {
    private ImageView backIcon;
    private RecyclerView checkoutRecyclerView;
    private CheckoutAdapter checkoutAdapter;
    private List<Product> checkoutList;
    private TextView itemsCountText, totalPriceText, commissionText, commissionAmountText, totalAmountText;
    private LinearLayout noCardsContainer; // Обновляем на контейнер
    private RadioGroup paymentMethodGroup;
    private RadioButton paymentCard, paymentCash;
    private CheckBox termsCheckbox;
    private Button checkoutButton;
    private Spinner cardSpinner;
    private ImageView priceFilterIcon;
    private boolean isCardPayment = true;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private int processedProducts = 0;
    private List<Card> cards = new ArrayList<>();
    private Card selectedCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Пожалуйста, авторизуйтесь", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        backIcon = findViewById(R.id.back_icon);
        checkoutRecyclerView = findViewById(R.id.checkout_recycler_view);
        itemsCountText = findViewById(R.id.items_count_text);
        totalPriceText = findViewById(R.id.total_price_text);
        commissionText = findViewById(R.id.commission_text);
        commissionAmountText = findViewById(R.id.commission_amount_text);
        totalAmountText = findViewById(R.id.total_amount_text);
        paymentMethodGroup = findViewById(R.id.payment_method_group);
        paymentCard = findViewById(R.id.payment_card);
        paymentCash = findViewById(R.id.payment_cash);
        termsCheckbox = findViewById(R.id.terms_checkbox);
        checkoutButton = findViewById(R.id.checkout_button);
        cardSpinner = findViewById(R.id.card_spinner);
        noCardsContainer = findViewById(R.id.no_cards_container); // Инициализируем контейнер
        priceFilterIcon = findViewById(R.id.price_filter_icon);

        checkoutList = (ArrayList<Product>) getIntent().getSerializableExtra("cartItems");
        if (checkoutList == null) {
            checkoutList = new ArrayList<>();
        }

        checkoutAdapter = new CheckoutAdapter(this, checkoutList);
        checkoutRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        checkoutRecyclerView.setAdapter(checkoutAdapter);

        loadCards();

        updateButtonState();
        updateSummary();

        paymentMethodGroup.setOnCheckedChangeListener((group, checkedId) -> {
            isCardPayment = checkedId == R.id.payment_card;
            updateCardSelectionVisibility();
            updateSummary();
        });

        termsCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            checkoutButton.setEnabled(isChecked && (!isCardPayment || !cards.isEmpty()));
            updateButtonState();
        });

        cardSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCard = cards.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCard = null;
            }
        });

        checkoutButton.setOnClickListener(v -> {
            if (isCardPayment && selectedCard == null) {
                Toast.makeText(this, "Пожалуйста, выберите карту для оплаты", Toast.LENGTH_SHORT).show();
                return;
            }
            processOrder();
        });

        backIcon.setOnClickListener(v -> onBackPressed());

        View headerView = findViewById(R.id.header);
        new Header(headerView, this);
    }

    private void loadCards() {
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .collection("cards")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    cards.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Card card = document.toObject(Card.class);
                        card.setCardId(document.getId());
                        cards.add(card);
                    }
                    updateCardSelectionVisibility();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка загрузки карт: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateCardSelectionVisibility() {
        if (isCardPayment) {
            if (cards.isEmpty()) {
                noCardsContainer.setVisibility(View.VISIBLE); // Показываем контейнер с иконкой и текстом
                cardSpinner.setVisibility(View.GONE);
                priceFilterIcon.setVisibility(View.GONE);
                checkoutButton.setEnabled(false);
            } else {
                noCardsContainer.setVisibility(View.GONE); // Скрываем контейнер
                cardSpinner.setVisibility(View.VISIBLE);
                priceFilterIcon.setVisibility(View.VISIBLE);

                List<String> cardDisplayList = new ArrayList<>();
                for (Card card : cards) {
                    String lastFourDigits = card.getCardNumber().substring(card.getCardNumber().length() - 4);
                    cardDisplayList.add("Карта **** " + lastFourDigits);
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cardDisplayList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                cardSpinner.setAdapter(adapter);

                if (!cards.isEmpty()) {
                    selectedCard = cards.get(0);
                    cardSpinner.setSelection(0);
                }

                checkoutButton.setEnabled(termsCheckbox.isChecked());
            }
        } else {
            noCardsContainer.setVisibility(View.GONE); // Скрываем контейнер
            cardSpinner.setVisibility(View.GONE);
            priceFilterIcon.setVisibility(View.GONE);
            checkoutButton.setEnabled(termsCheckbox.isChecked());
        }
        updateButtonState();
    }

    private void updateSummary() {
        int itemCount = checkoutList.size();
        itemsCountText.setText("Товары (" + itemCount + ")");

        long totalPrice = 0;
        for (Product product : checkoutList) {
            totalPrice += product.getPrice() * product.getQuantity();
        }

        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        DecimalFormat decimalFormat = new DecimalFormat("#,### ₽", symbols);
        String formattedPrice = decimalFormat.format(totalPrice);
        totalPriceText.setText(formattedPrice);

        long commission = 0;
        if (isCardPayment) {
            commission = (long) (totalPrice * 0.01);
            findViewById(R.id.commission_layout).setVisibility(View.VISIBLE);
            commissionAmountText.setText(decimalFormat.format(commission));
        } else {
            findViewById(R.id.commission_layout).setVisibility(View.GONE);
        }

        long finalTotal = totalPrice + commission;
        totalAmountText.setText(decimalFormat.format(finalTotal));
    }

    private void updateButtonState() {
        if (checkoutButton.isEnabled()) {
            checkoutButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.Aquamarine)));
        } else {
            checkoutButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.disabled_button_color)));
        }
    }

    private void processOrder() {
        String userId = auth.getCurrentUser().getUid();
        String userEmail = auth.getCurrentUser().getEmail();

        if (checkoutList.isEmpty()) {
            Toast.makeText(this, "Корзина пуста", Toast.LENGTH_SHORT).show();
            return;
        }

        processedProducts = 0;

        for (Product product : checkoutList) {
            String productArticle = product.getArticle();
            int quantityToReduce = product.getQuantity();

            db.collection("products")
                    .whereEqualTo("article", productArticle)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                            Long currentQuantity = document.getLong("quantity");
                            Long currentSalesCount = document.getLong("salesCount") != null ? document.getLong("salesCount") : 0L;
                            if (currentQuantity != null && currentQuantity >= quantityToReduce) {
                                String productId = document.getId();
                                db.collection("products").document(productId)
                                        .update(
                                                "quantity", currentQuantity - quantityToReduce,
                                                "salesCount", currentSalesCount + quantityToReduce
                                        )
                                        .addOnSuccessListener(aVoid -> {
                                            synchronized (this) {
                                                processedProducts++;
                                                if (processedProducts == checkoutList.size()) {
                                                    checkAllProductsProcessed(userId, userEmail);
                                                }
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Ошибка обновления данных товара: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            finish();
                                        });
                            } else {
                                Toast.makeText(this, "Недостаточно товара: " + product.getTitle(), Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } else {
                            Toast.makeText(this, "Товар не найден: " + product.getTitle(), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Ошибка поиска товара: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    });
        }
    }

    private void checkAllProductsProcessed(String userId, String userEmail) {
        db.collection("products")
                .whereIn("article", getArticlesList())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean allProcessed = true;
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Long currentQuantity = document.getLong("quantity");
                        Product product = checkoutList.stream()
                                .filter(p -> p.getArticle().equals(document.getString("article")))
                                .findFirst()
                                .orElse(null);
                        if (product != null && currentQuantity != null) {
                            if (currentQuantity < 0) {
                                allProcessed = false;
                                Toast.makeText(this, "Ошибка: отрицательное количество товара " + product.getTitle(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    if (allProcessed) {
                        clearCart(userId, userEmail);
                    } else {
                        Toast.makeText(this, "Не удалось обработать заказ. Проверьте корзину.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка проверки товаров: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private List<String> getArticlesList() {
        List<String> articles = new ArrayList<>();
        for (Product product : checkoutList) {
            articles.add(product.getArticle());
        }
        return articles;
    }

    private void clearCart(String userId, String userEmail) {
        List<Product> orderedItems = new ArrayList<>(checkoutList);
        long totalPrice = orderedItems.stream().mapToLong(product -> product.getPrice() * product.getQuantity()).sum();
        long commission = isCardPayment ? (long) (totalPrice * 0.01) : 0;
        long finalTotal = totalPrice + commission;

        Order order = new Order(
                orderedItems,
                totalPrice,
                commission,
                finalTotal,
                new Date(),
                getArticlesList()
        );

        db.collection("users").document(userId)
                .collection("orders")
                .add(order)
                .addOnSuccessListener(documentReference -> {
                    db.collection("users").document(userId).collection("cart")
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                for (DocumentSnapshot document : queryDocumentSnapshots) {
                                    document.getReference().delete();
                                }
                                checkoutList.clear();
                                checkoutAdapter.notifyDataSetChanged();
                                sendElectronicReceipt(userEmail, orderedItems, totalPrice, commission, finalTotal);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Ошибка очистки корзины: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                finish();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка сохранения заказа: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void sendElectronicReceipt(String userEmail, List<Product> orderedItems, long totalPrice, long commission, long finalTotal) {
        StringBuilder itemsHtml = new StringBuilder();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        DecimalFormat decimalFormat = new DecimalFormat("#,### ₽", symbols);

        for (Product product : orderedItems) {
            long productTotal = product.getPrice() * product.getQuantity();
            itemsHtml.append("<p>")
                    .append(product.getTitle())
                    .append(" (x").append(product.getQuantity()).append("): ")
                    .append(decimalFormat.format(productTotal))
                    .append("</p>");
        }

        String date = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date());

        String paymentMethod = isCardPayment ? "Картой" : "Наличными";
        String cardDetails = "";
        if (isCardPayment && selectedCard != null) {
            String lastFourDigits = selectedCard.getCardNumber().substring(selectedCard.getCardNumber().length() - 4);
            cardDetails = "<p><strong>Карта:</strong> **** " + lastFourDigits + "</p>";
        }

        String htmlContent = "<html>" +
                "<body style='font-family: Arial, sans-serif; color: #333; background-color: #f4f4f4; padding: 20px;'>" +
                "<div style='max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);'>" +
                "<div style='background-color: #1c2526; color: #ffffff; padding: 20px; text-align: center; border-top-left-radius: 10px; border-top-right-radius: 10px;'>" +
                "<h1 style='margin: 0; font-size: 24px;'>SORAPC</h1>" +
                "</div>" +
                "<div style='padding: 20px;'>" +
                "<h2 style='color: #1c2526; font-size: 20px; margin-bottom: 15px;'>Чек покупки</h2>" +
                "<p><strong>Дата:</strong> " + date + "</p>" +
                "<p><strong>Пользователь:</strong> " + userEmail + "</p>" +
                "<p><strong>Способ оплаты:</strong> " + paymentMethod + "</p>" +
                cardDetails +
                "<p><strong>Товары:</strong></p>" +
                "<div style='background-color: #f9f9f9; padding: 15px; border-left: 4px solid #1c2526;'>" +
                itemsHtml.toString() +
                "</div>" +
                "<p><strong>Сумма товаров:</strong> " + decimalFormat.format(totalPrice) + "</p>" +
                "<p><strong>Комиссия:</strong> " + decimalFormat.format(commission) + "</p>" +
                "<p><strong>Итого:</strong> " + decimalFormat.format(finalTotal) + "</p>" +
                "</div>" +
                "<div style='background-color: #f4f4f4; padding: 15px; text-align: center; border-bottom-left-radius: 10px; border-bottom-right-radius: 10px;'>" +
                "<p style='color: #666; font-size: 12px; margin: 0;'>Свяжитесь с нами: <a href='mailto:sorapc.support@gmail.com' style='color: #1c2526;'>sorapc.support@gmail.com</a> | +7 (999) 123-45-67</p>" +
                "<p style='color: #666; font-size: 12px; margin: 5px 0 0 0;'>© 2025 SORAPC. Все права защищены.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";

        new Thread(() -> {
            try {
                Properties properties = new Properties();
                properties.put("mail.smtp.auth", "true");
                properties.put("mail.smtp.starttls.enable", "true");
                properties.put("mail.smtp.host", "smtp.gmail.com");
                properties.put("mail.smtp.port", "587");

                final String senderEmail = "sorapc.store@gmail.com";
                final String senderPassword = "plwc xoyo artf zfor";

                Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(senderEmail, senderPassword);
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(senderEmail));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(userEmail));
                message.setSubject("Чек покупки");

                MimeBodyPart mimeBodyPart = new MimeBodyPart();
                mimeBodyPart.setContent(htmlContent, "text/html; charset=utf-8");

                MimeMultipart multipart = new MimeMultipart();
                multipart.addBodyPart(mimeBodyPart);

                message.setContent(multipart);

                Transport.send(message);

                runOnUiThread(() -> {
                    Intent intent = new Intent(CheckoutActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                });

            } catch (MessagingException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Ошибка отправки чека: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CheckoutActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                });
            }
        }).start();
    }
}