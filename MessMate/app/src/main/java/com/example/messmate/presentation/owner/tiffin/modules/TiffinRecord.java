package com.example.messmate.presentation.owner.tiffin.modules;

public class TiffinRecord {

    private String documentId;

    private String ownerId;
    private String memberUid;
    private String memberDocumentId;

    private String memberName;
    private String phone;

    private String date;

    private String tiffin;

    private boolean dinner;


    public TiffinRecord() {
        // Required by Firestore
    }


    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }


    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }


    public String getMemberUid() {
        return memberUid;
    }

    public void setMemberUid(String memberUid) {
        this.memberUid = memberUid;
    }


    public String getMemberDocumentId() {
        return memberDocumentId;
    }

    public void setMemberDocumentId(String memberDocumentId) {
        this.memberDocumentId = memberDocumentId;
    }


    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }


    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


    public String getTiffin() {
        return tiffin;
    }

    public void setTiffin(String tiffin) {
        this.tiffin = tiffin;
    }


    public boolean isDinner() {
        return dinner;
    }

    public void setDinner(boolean dinner) {
        this.dinner = dinner;
    }
}