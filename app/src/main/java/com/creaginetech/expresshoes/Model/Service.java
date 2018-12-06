package com.creaginetech.expresshoes.Model;

//pengganti class Food
public class Service {
    private String serviceName, image, description, price, discount, serviceId;

    public Service() {
    }

    public Service(String serviceName, String image, String description, String price, String discount, String serviceId) {
        this.serviceName = serviceName;
        this.image = image;
        this.description = description;
        this.price = price;
        this.discount = discount;
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }
}
