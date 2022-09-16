package com.tu.tuchati.Models;

public class FeatureModel {
    private String country,city,shopName,
            shopimage,uid,packageType;
    private Long timestart,timeend;

    public FeatureModel() {
    }

    public FeatureModel(String country, String city, String shopName, String shopimage, String uid, String packageType, Long timestart, Long timeend) {
        this.country = country;
        this.city = city;
        this.shopName = shopName;
        this.shopimage = shopimage;
        this.uid = uid;
        this.packageType = packageType;
        this.timestart = timestart;
        this.timeend = timeend;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getShopimage() {
        return shopimage;
    }

    public void setShopimage(String shopimage) {
        this.shopimage = shopimage;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPackageType() {
        return packageType;
    }

    public void setPackageType(String packageType) {
        this.packageType = packageType;
    }

    public Long getTimestart() {
        return timestart;
    }

    public void setTimestart(Long timestart) {
        this.timestart = timestart;
    }

    public Long getTimeend() {
        return timeend;
    }

    public void setTimeend(Long timeend) {
        this.timeend = timeend;
    }
}
