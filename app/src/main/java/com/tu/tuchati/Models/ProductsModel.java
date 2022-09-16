package com.tu.tuchati.Models;

public class ProductsModel {
    private String productId,productTitle,productDescription,productCategory
            ,productCondition,productPrice,discountPrice,moreThanOneOrder,discountAvailable,productImage,
            timestamp,uid;

    public ProductsModel() {
    }

    public ProductsModel(String productId, String productTitle, String productDescription, String productCategory, String productCondition, String productPrice, String discountPrice, String moreThanOneOrder, String discountAvailable, String productImage, String timestamp, String uid) {
        this.productId = productId;
        this.productTitle = productTitle;
        this.productDescription = productDescription;
        this.productCategory = productCategory;
        this.productCondition = productCondition;
        this.productPrice = productPrice;
        this.discountPrice = discountPrice;
        this.moreThanOneOrder = moreThanOneOrder;
        this.discountAvailable = discountAvailable;
        this.productImage = productImage;
        this.timestamp = timestamp;
        this.uid = uid;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public String getProductCondition() {
        return productCondition;
    }

    public void setProductCondition(String productCondition) {
        this.productCondition = productCondition;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public String getDiscountPrice() {
        return discountPrice;
    }

    public void setDiscountPrice(String discountPrice) {
        this.discountPrice = discountPrice;
    }

    public String getMoreThanOneOrder() {
        return moreThanOneOrder;
    }

    public void setMoreThanOneOrder(String moreThanOneOrder) {
        this.moreThanOneOrder = moreThanOneOrder;
    }

    public String getDiscountAvailable() {
        return discountAvailable;
    }

    public void setDiscountAvailable(String discountAvailable) {
        this.discountAvailable = discountAvailable;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
