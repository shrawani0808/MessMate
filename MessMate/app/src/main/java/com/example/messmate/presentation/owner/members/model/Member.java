package com.example.messmate.presentation.owner.members.model;

public class Member {

    private String documentId;
    private String ownerId;
    private String memberUid;
    private String name;
    private String email;
    private String phone;

    public Member() {
        // Required by Firestore
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}