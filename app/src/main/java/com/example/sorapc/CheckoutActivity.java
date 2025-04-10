package com.example.sorapc;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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

public class CheckoutActivity extends AppCompatActivity {
    private ImageView backIcon;
    private RecyclerView checkoutRecyclerView;
    private CheckoutAdapter checkoutAdapter;
    private List<Product> checkoutList;
    private TextView itemsCountText, totalPriceText, commissionText, commissionAmountText, totalAmountText;
    private RadioGroup paymentMethodGroup;
    private RadioButton paymentCard, paymentCash;
    private CheckBox termsCheckbox;
    private Button checkoutButton;
    private boolean isCardPayment = true;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private int processedProducts = 0;

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

        checkoutList = (ArrayList<Product>) getIntent().getSerializableExtra("cartItems");
        if (checkoutList == null) {
            checkoutList = new ArrayList<>();
        }

        checkoutAdapter = new CheckoutAdapter(this, checkoutList);
        checkoutRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        checkoutRecyclerView.setAdapter(checkoutAdapter);

        updateButtonState();
        updateSummary();

        paymentMethodGroup.setOnCheckedChangeListener((group, checkedId) -> {
            isCardPayment = checkedId == R.id.payment_card;
            updateSummary();
        });

        termsCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            checkoutButton.setEnabled(isChecked);
            updateButtonState();
        });

        checkoutButton.setOnClickListener(v -> processOrder());

        backIcon.setOnClickListener(v -> onBackPressed());

        View headerView = findViewById(R.id.header);
        new Header(headerView, this);
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

        // Создаём заказ
        Order order = new Order(
                orderedItems,
                totalPrice,
                commission,
                finalTotal,
                new Date(),
                getArticlesList()
        );

        // Сохраняем заказ в подколлекцию orders
        db.collection("users").document(userId)
                .collection("orders")
                .add(order)
                .addOnSuccessListener(documentReference -> {
                    // После успешного сохранения заказа очищаем корзину
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