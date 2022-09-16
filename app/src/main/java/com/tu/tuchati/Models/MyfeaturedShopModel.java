package com.tu.tuchati.Models;

public class MyfeaturedShopModel {
    private String shopcity,shopCountry,shopimage,shopName,shopuid,packageType;
    private Long timestart,timeend;

    public MyfeaturedShopModel() {
    }

    public MyfeaturedShopModel(String shopcity, String shopCountry, String shopimage, String shopName, String shopuid, String packageType, Long timestart, Long timeend) {
        this.shopcity = shopcity;
        this.shopCountry = shopCountry;
        this.shopimage = shopimage;
        this.shopName = shopName;
        this.shopuid = shopuid;
        this.packageType = packageType;
        this.timestart = timestart;
        this.timeend = timeend;
    }

    public String getShopcity() {
        return shopcity;
    }

    public void setShopcity(String shopcity) {
        this.shopcity = shopcity;
    }

    public String getShopCountry() {
        return shopCountry;
    }

    public void setShopCountry(String shopCountry) {
        this.shopCountry = shopCountry;
    }

    public String getShopimage() {
        return shopimage;
    }

    public void setShopimage(String shopimage) {
        this.shopimage = shopimage;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getShopuid() {
        return shopuid;
    }

    public void setShopuid(String shopuid) {
        this.shopuid = shopuid;
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
