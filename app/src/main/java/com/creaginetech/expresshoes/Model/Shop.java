package com.creaginetech.expresshoes.Model;

//pengganti restaurant
public class Shop {

    private String shopName,shopImage;

    public Shop() {
    }

    public Shop(String shopName, String shopImage) {
        this.shopName = shopName;
        this.shopImage = shopImage;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getShopImage() {
        return shopImage;
    }

    public void setShopImage(String shopImage) {
        this.shopImage = shopImage;
    }
}
