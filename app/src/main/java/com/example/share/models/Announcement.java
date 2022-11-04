package com.example.share.models;

import java.io.Serializable;

public class Announcement implements Serializable {
    private String id;
    private String image;
    private String title;
    private String description;
    private String type;
    private int numberBathrooms;
    private int numberBedrooms;
    private int numberResidents;
    private Double price;
    private String userId;
    private Double latPoint;
    private Double lngPoint;
    private int status;

    public Announcement() {
    }

    public Announcement(String id, String image, String title, String description, String type, int numberBathrooms, int numberBedrooms, int numberResidents, Double price, String userId, Double latPoint, Double lngPoint, int status) {
        this.id = id;
        this.image = image;
        this.title = title;
        this.description = description;
        this.type = type;
        this.numberBathrooms = numberBathrooms;
        this.numberBedrooms = numberBedrooms;
        this.numberResidents = numberResidents;
        this.price = price;
        this.userId = userId;
        this.latPoint = latPoint;
        this.lngPoint = lngPoint;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getNumberBathrooms() {
        return numberBathrooms;
    }

    public void setNumberBathrooms(int numberBathrooms) {
        this.numberBathrooms = numberBathrooms;
    }

    public int getNumberBedrooms() {
        return numberBedrooms;
    }

    public void setNumberBedrooms(int numberBedrooms) {
        this.numberBedrooms = numberBedrooms;
    }

    public int getNumberResidents() {
        return numberResidents;
    }

    public void setNumberResidents(int numberResidents) {
        this.numberResidents = numberResidents;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Double getLatPoint() {
        return latPoint;
    }

    public void setLatPoint(Double latPoint) {
        this.latPoint = latPoint;
    }

    public Double getLngPoint() {
        return lngPoint;
    }

    public void setLngPoint(Double lngPoint) {
        this.lngPoint = lngPoint;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
