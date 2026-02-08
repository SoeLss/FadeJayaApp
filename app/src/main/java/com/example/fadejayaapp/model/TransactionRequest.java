package com.example.fadejayaapp.model;

import java.util.List;

public class TransactionRequest {
    private String user_id;
    private String customer_id;
    private double sub_total;
    private double discount_amount;
    private double grand_total;
    private String payment_method;
    private List<CartItem> items;

    // === SETTER (Sesuai panggilan di Activity) ===
    public void setUser_id(String user_id) { this.user_id = user_id; }
    public void setCustomer_id(String customer_id) { this.customer_id = customer_id; }
    public void setSub_total(double sub_total) { this.sub_total = sub_total; }
    public void setDiscount_amount(double discount_amount) { this.discount_amount = discount_amount; }
    public void setGrand_total(double grand_total) { this.grand_total = grand_total; }
    public void setPayment_method(String payment_method) { this.payment_method = payment_method; }
    public void setItems(List<CartItem> items) { this.items = items; }
}