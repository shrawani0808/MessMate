package com.example.messmate.presentation.owner.members.model;

public class Member {

    private String documentId;
    private String ownerId;
    private String memberUid;

    private String name;
    private String email;
    private String phone;

    // =========================================================
    // PAYMENT CONFIGURATION
    // =========================================================

    // "daily" or "monthly"
    private String paymentType;

    // =========================================================
    // MEAL CONFIGURATION
    // =========================================================

    private boolean lunchEnabled;
    private boolean dinnerEnabled;

    // =========================================================
    // DAILY RATES
    // Same rates are used for BOTH lunch and dinner
    // =========================================================

    private double fullRate;
    private double halfRate;

    // =========================================================
    // MONTHLY PACKAGE AMOUNTS
    // Lunch and Dinner have separate package amounts
    // =========================================================

    private double monthlyLunchAmount;
    private double monthlyDinnerAmount;


    // =========================================================
    // FIRESTORE CONSTRUCTOR
    // =========================================================

    public Member() {
        // Required by Firestore
    }


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public Member(
            String ownerId,
            String memberUid,
            String name,
            String email,
            String phone) {

        this.ownerId = ownerId;
        this.memberUid = memberUid;
        this.name = name;
        this.email = email;
        this.phone = phone;

        this.paymentType = "daily";

        this.lunchEnabled = true;
        this.dinnerEnabled = true;

        this.fullRate = 0;
        this.halfRate = 0;

        this.monthlyLunchAmount = 0;
        this.monthlyDinnerAmount = 0;
    }


    // =========================================================
    // DOCUMENT ID
    // =========================================================

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }


    // =========================================================
    // OWNER ID
    // =========================================================

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }


    // =========================================================
    // MEMBER UID
    // =========================================================

    public String getMemberUid() {
        return memberUid;
    }

    public void setMemberUid(String memberUid) {
        this.memberUid = memberUid;
    }


    // =========================================================
    // NAME
    // =========================================================

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    // =========================================================
    // EMAIL
    // =========================================================

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    // =========================================================
    // PHONE
    // =========================================================

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


    // =========================================================
    // PAYMENT TYPE
    // =========================================================

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }


    // =========================================================
    // LUNCH ENABLED
    // =========================================================

    public boolean isLunchEnabled() {
        return lunchEnabled;
    }

    public void setLunchEnabled(boolean lunchEnabled) {
        this.lunchEnabled = lunchEnabled;
    }


    // =========================================================
    // DINNER ENABLED
    // =========================================================

    public boolean isDinnerEnabled() {
        return dinnerEnabled;
    }

    public void setDinnerEnabled(boolean dinnerEnabled) {
        this.dinnerEnabled = dinnerEnabled;
    }


    // =========================================================
    // DAILY FULL RATE
    // =========================================================

    public double getFullRate() {
        return fullRate;
    }

    public void setFullRate(double fullRate) {
        this.fullRate = fullRate;
    }


    // =========================================================
    // DAILY HALF RATE
    // =========================================================

    public double getHalfRate() {
        return halfRate;
    }

    public void setHalfRate(double halfRate) {
        this.halfRate = halfRate;
    }


    // =========================================================
    // MONTHLY LUNCH AMOUNT
    // =========================================================

    public double getMonthlyLunchAmount() {
        return monthlyLunchAmount;
    }

    public void setMonthlyLunchAmount(double monthlyLunchAmount) {
        this.monthlyLunchAmount = monthlyLunchAmount;
    }


    // =========================================================
    // MONTHLY DINNER AMOUNT
    // =========================================================

    public double getMonthlyDinnerAmount() {
        return monthlyDinnerAmount;
    }

    public void setMonthlyDinnerAmount(double monthlyDinnerAmount) {
        this.monthlyDinnerAmount = monthlyDinnerAmount;
    }
}