package com.example.sorapc;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
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
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
            return;
        }

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
                                // Обновляем количество и увеличиваем количество продаж
                                db.collection("products").document(productId)
                                        .update(
                                                "quantity", currentQuantity - quantityToReduce,
                                                "salesCount", currentSalesCount + quantityToReduce
                                        )
                                        .addOnSuccessListener(aVoid -> {
                                            checkAllProductsProcessed(userId, userEmail);
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Ошибка обновления данных товара: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                Toast.makeText(this, "Недостаточно товара: " + product.getTitle(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Товар не найден: " + product.getTitle(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Ошибка поиска товара: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка проверки товаров: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        db.collection("users").document(userId).collection("cart")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().delete();
                    }
                    generateAndSendReceipt(userEmail);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка очистки корзины: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void generateAndSendReceipt(String userEmail) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4 размер (595x842 пикселей)
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // Определяем краски для текста и линий
        Paint titlePaint = new Paint();
        titlePaint.setColor(Color.parseColor("#1c1c1c")); // Aquamarine
        titlePaint.setTextSize(24);
        titlePaint.setTextAlign(Paint.Align.CENTER);

        Paint headerPaint = new Paint();
        headerPaint.setColor(Color.BLACK);
        headerPaint.setTextSize(16);
        headerPaint.setTextAlign(Paint.Align.LEFT);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(12);
        textPaint.setTextAlign(Paint.Align.LEFT);

        Paint linePaint = new Paint();
        linePaint.setColor(Color.parseColor("#1c1c1c")); // Aquamarine для линий
        linePaint.setStrokeWidth(2);

        Paint rightAlignPaint = new Paint();
        rightAlignPaint.setColor(Color.BLACK);
        rightAlignPaint.setTextSize(12);
        rightAlignPaint.setTextAlign(Paint.Align.RIGHT);

        Paint footerPaint = new Paint();
        footerPaint.setColor(Color.GRAY);
        footerPaint.setTextSize(10);
        footerPaint.setTextAlign(Paint.Align.CENTER);

        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        DecimalFormat decimalFormat = new DecimalFormat("#,### ₽", symbols);

        int yPosition = 40;
        int pageWidth = pageInfo.getPageWidth();
        int margin = 40;
        int contentWidth = pageWidth - 2 * margin;

        canvas.drawText("SORAPC", pageWidth / 2, yPosition, titlePaint);
        yPosition += 10;

        canvas.drawLine(margin, yPosition, pageWidth - margin, yPosition, linePaint);
        yPosition += 30;

        canvas.drawText("Чек покупки", margin, yPosition, headerPaint);
        yPosition += 20;
        canvas.drawText("Дата: " + new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date()), margin, yPosition, textPaint);
        yPosition += 15;
        canvas.drawText("Пользователь: " + userEmail, margin, yPosition, textPaint);
        yPosition += 25;

        canvas.drawLine(margin, yPosition, pageWidth - margin, yPosition, linePaint);
        yPosition += 15;

        canvas.drawText("Товары:", margin, yPosition, headerPaint);
        yPosition += 20;

        long totalPrice = 0;
        for (Product product : checkoutList) {
            long productTotal = product.getPrice() * product.getQuantity();
            totalPrice += productTotal;

            String productLine = product.getTitle() + " (x" + product.getQuantity() + ")";
            if (titlePaint.measureText(productLine) > contentWidth - 100) {
                productLine = productLine.substring(0, 30) + "...";
            }
            canvas.drawText(productLine, margin, yPosition, textPaint);

            String priceText = decimalFormat.format(productTotal);
            canvas.drawText(priceText, pageWidth - margin, yPosition, rightAlignPaint);
            yPosition += 20;
        }

        yPosition += 5;
        canvas.drawLine(margin, yPosition, pageWidth - margin, yPosition, linePaint);
        yPosition += 20;

        long commission = isCardPayment ? (long) (totalPrice * 0.01) : 0;

        canvas.drawText("Сумма товаров:", margin, yPosition, textPaint);
        canvas.drawText(decimalFormat.format(totalPrice), pageWidth - margin, yPosition, rightAlignPaint);
        yPosition += 15;

        canvas.drawText("Комиссия:", margin, yPosition, textPaint);
        canvas.drawText(decimalFormat.format(commission), pageWidth - margin, yPosition, rightAlignPaint);
        yPosition += 20;

        canvas.drawText("Итого:", margin, yPosition, textPaint);
        canvas.drawText(decimalFormat.format(totalPrice + commission), pageWidth - margin, yPosition, rightAlignPaint);
        yPosition += 20;

        canvas.drawLine(margin, yPosition, pageWidth - margin, yPosition, linePaint);
        yPosition += 30;

        canvas.drawText("Спасибо за покупку в SORAPC!", pageWidth / 2, yPosition, footerPaint);
        yPosition += 15;
        canvas.drawText("Свяжитесь с нами: sorapc.store@gmail.com | +7 (999) 123-45-67", pageWidth / 2, yPosition, footerPaint);

        document.finishPage(page);

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Receipt_" + System.currentTimeMillis() + ".pdf");
        try {
            document.writeTo(new FileOutputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка сохранения чека: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            document.close();
            return;
        }
        document.close();

        sendEmailWithAttachment(userEmail, file);
    }

    private void sendEmailWithAttachment(String userEmail, File file) {
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

                MimeBodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setText("Ваш чек покупки во вложении.");

                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.attachFile(file);

                MimeMultipart multipart = new MimeMultipart();
                multipart.addBodyPart(messageBodyPart);
                multipart.addBodyPart(attachmentPart);

                message.setContent(multipart);

                Transport.send(message);

                runOnUiThread(() -> {
                    finish();
                });

            } catch (MessagingException | IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Ошибка отправки чека: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}