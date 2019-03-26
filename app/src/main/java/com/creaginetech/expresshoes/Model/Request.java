package com.creaginetech.expresshoes.Model;

import java.util.List;

public class Request {
    private String phone;
    private String name;
    private String address;
    private String total;
    private String status;
    //sementara comment dihilangin
//    private String comment;
    private String latLng;
    private String shopId;
    private List<Order> items; // list of food order

    public Request() {
    }

    public Request(String phone, String name, String address, String total, String status, String latLng, String shopId, List<Order> items) {
        this.phone = phone;
        this.name = name;
        this.address = address;
        this.total = total;
        this.status = status;
        this.latLng = latLng;
        this.shopId = shopId;
        this.items = items;
    }

//    public Request(String phone, String name, String address, String total, String status, String comment, String latLng, String restaurantId, List<Order> foods) {
//        this.phone = phone;
//        this.name = name;
//        this.address = address;
//        this.total = total;
//        this.status = status;
//        this.comment = comment;
//        this.latLng = latLng;
//        this.restaurantId = restaurantId;
//        this.foods = foods;
//    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

//    public String getComment() {
//        return comment;
//    }
//
//    public void setComment(String comment) {
//        this.comment = comment;
//    }

    public String getLatLng() {
        return latLng;
    }

    public void setLatLng(String latLng) {
        this.latLng = latLng;
    }

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public List<Order> getItems() {
        return items;
    }

    public void setItems(List<Order> items) {
        this.items = items;
    }
}
