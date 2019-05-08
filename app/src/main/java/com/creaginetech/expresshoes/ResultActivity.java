package com.creaginetech.expresshoes;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {

    public TextView txtPhone, txtAddress, txtName, txtPrice, txtValue, txtLocation, txtRestaurant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent intent = getIntent();

        String phone = intent.getStringExtra("phone");
        String address = intent.getStringExtra("address");
        String name = intent.getStringExtra("name");
        String price = intent.getStringExtra("price");
        String value = intent.getStringExtra("value");
        String location = intent.getStringExtra("location");
        String restaurant = intent.getStringExtra("restaurant");

        txtPhone = findViewById(R.id.textViewPhone);
        txtAddress = findViewById(R.id.textViewAddress);
        txtName = findViewById(R.id.textViewName);
        txtPrice = findViewById(R.id.textViewTotal);
        txtValue = findViewById(R.id.textViewValue);
        txtLocation = findViewById(R.id.textViewLocation);
        txtRestaurant = findViewById(R.id.textViewRestaurant);

        txtPhone.setText(phone);
        txtAddress.setText(address);
        txtName.setText(name);
        txtPrice.setText(price);
        txtValue.setText(value);
        txtLocation.setText(location);
        txtRestaurant.setText(restaurant);


    }
}
