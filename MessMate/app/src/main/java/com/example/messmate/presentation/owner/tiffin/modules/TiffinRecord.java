package com.example.messmate.presentation.owner.tiffin.modules;

public class TiffinRecord {

    private String documentId;

    private String ownerId;

    private String memberUid;

    private String memberDocumentId;

    private String memberName;

    private String phone;

    private String date;

    // =====================================================
    // MEAL STATUS
    // =====================================================

    // "full", "half", "none"
    private String lunchStatus;

    // "full", "half", "none"
    private String dinnerStatus;


    // =====================================================
    // FIRESTORE CONSTRUCTOR
    // =====================================================

    public TiffinRecord() {
        // Required by Firestore
    }


    // =====================================================
    // DOCUMENT ID
    // =====================================================

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }


    // =====================================================
    // OWNER ID
    // =====================================================

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }


    // =====================================================
    // MEMBER UID
    // =====================================================

    public String getMemberUid() {
        return memberUid;
    }

    public void setMemberUid(String memberUid) {
        this.memberUid = memberUid;
    }


    // =====================================================
    // MEMBER DOCUMENT ID
    // =====================================================

    public String getMemberDocumentId() {
        return memberDocumentId;
    }

    public void setMemberDocumentId(String memberDocumentId) {
        this.memberDocumentId = memberDocumentId;
    }


    // =====================================================
    // MEMBER NAME
    // =====================================================

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }


    // =====================================================
    // PHONE
    // =====================================================

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


    // =====================================================
    // DATE
    // =====================================================

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


    // =====================================================
    // LUNCH STATUS
    // =====================================================

    public String getLunchStatus() {
        return lunchStatus;
    }

    public void setLunchStatus(String lunchStatus) {
        this.lunchStatus = lunchStatus;
    }


    // =====================================================
    // DINNER STATUS
    // =====================================================

    public String getDinnerStatus() {
        return dinnerStatus;
    }

    public void setDinnerStatus(String dinnerStatus) {
        this.dinnerStatus = dinnerStatus;
    }
}