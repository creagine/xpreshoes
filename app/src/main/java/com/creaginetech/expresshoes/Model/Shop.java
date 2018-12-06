package com.creaginetech.expresshoes.Model;

//pengganti restaurant
public class Shop {

    private String name,image;

    public Shop() {
    }

    public Shop(String name, String image) {
        this.name = name;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
