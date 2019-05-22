package com.creaginetech.expresshoes;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.creaginetech.expresshoes.Common.Common;
import com.creaginetech.expresshoes.Database.Database;
import com.creaginetech.expresshoes.Helper.RecyclerItemTouchHelper;
import com.creaginetech.expresshoes.Interface.RecyclerItemTouchHelperListener;
import com.creaginetech.expresshoes.Model.Order;
import com.creaginetech.expresshoes.Model.Request;
import com.creaginetech.expresshoes.Remote.APIService;
import com.creaginetech.expresshoes.ViewHolder.CartAdapter;
import com.creaginetech.expresshoes.ViewHolder.CartViewHolder;
import com.creaginetech.expresshoes.network.ApiServices;
import com.creaginetech.expresshoes.network.InitLibrary;
import com.creaginetech.expresshoes.response.Distance;
import com.creaginetech.expresshoes.response.Duration;
import com.creaginetech.expresshoes.response.LegsItem;
import com.creaginetech.expresshoes.response.ResponseRoute;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartFixedActivity extends AppCompatActivity implements RecyclerItemTouchHelperListener {

    private String API_KEY = "AIzaSyDLrgdsWEVRd2fSPohzzECFikV8GeFxx8A";

    //VARIABLE RECYCLER
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    //VARIABLE FIREBASE DATABASE
    FirebaseDatabase database;
    DatabaseReference requestReference;

    //VARIABLE VIEWS
    public TextView txtTotalPrice;
    public TextView txtTotalItems;
    public TextView txtAlamat; //public agar bisa dipanggil di cartAdapter
    public TextView txtDistance;
    public TextView txtDeliveryFee;
    Button btnPlace;
    ConstraintLayout rootLayout;

    String address;

    public double deliveryFee;

    public int total, totalPayment;

    double pickupLatitude, pickupLongitude, shopLatitude, shopLongitude;

    String[] pickupLatlng, shopLatlng;

    //CART ADAPTER
    List<Order> cart = new ArrayList<>();
    CartAdapter adapter;

    APIService mService;

    private static final int UPDATE_INTERVAL = 5000;
    private static final int FATEST_INTERVAL = 3000;
    private static final int DISPLACEMENT = 10;

    private static final int LOCATION_REQUEST_CODE = 9999;
    private static final int PLAY_SERVICES_REQUEST = 9997;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_new);

        //Init FCM Service buat notif
        mService = Common.getFCMService();

        rootLayout = findViewById(R.id.rootLayout);

        //Firebase DATABASE
        database = FirebaseDatabase.getInstance();
        requestReference = database.getReference("Order");

        //Init RECYCLER
        recyclerView = findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //Swipe to delete
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0,ItemTouchHelper.LEFT,this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        //init widget
        txtTotalPrice = findViewById(R.id.total);
        txtTotalItems = findViewById(R.id.totalItemsCart);
        txtAlamat = findViewById(R.id.textViewAlamat);
        txtDeliveryFee = findViewById(R.id.deliveryFee);
        txtDistance = findViewById(R.id.distance);
        btnPlace = findViewById(R.id.btnPlaceOrder);

        //txt alamat
        txtAlamat.setText(Common.pickupLocationNameSelected);

        pickupLatlng =  Common.pickupLocationSelected.split(",");
        pickupLatitude = Double.parseDouble(pickupLatlng[0]);
        pickupLongitude = Double.parseDouble(pickupLatlng[1]);

        shopLatlng =  Common.shopLocationSelected.split(",");
        shopLatitude = Double.parseDouble(shopLatlng[0]);
        shopLongitude = Double.parseDouble(shopLatlng[1]);

        loadListFood();

        //Location runtime permission - cp 36
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]
                    {
                            android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },LOCATION_REQUEST_CODE);

        }
        else
        {

            if(pickupLatitude!=0){
                getRoutes();
            }

        }

        //BUTTON PLACE ORDER
        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //TODO DISINI HARUSNYA KIRIM NOTIF
                if (cart.size() > 0) {

                    //Create new request
                    Request request = new Request(
                            Common.currentUser.getPhone(),
                            Common.currentUser.getName(),
                            txtAlamat.getText().toString(),
                            txtTotalPrice.getText().toString(),
                            //TODO CEK STATUS INI
                            "0", // status
                            String.format("%s,%s",pickupLatitude,pickupLongitude),
                            Common.shopSelected,
                            cart
                    );

                    //Submit to Firebase
                    //We will using System.CurrentMilli to key
                    String order_number = String.valueOf(System.currentTimeMillis());
                    requestReference.child(order_number)
                            .setValue(request);

                    //Delete Cart
                    new Database(getBaseContext()).cleanCart(Common.currentUser.getPhone());
                    Toast.makeText(CartFixedActivity.this, "Thank you , Order Placed", Toast.LENGTH_SHORT).show();
                    finish();

                    //TODO INTENT KE DETAIL ORDER

                } else {
                    Toast.makeText(CartFixedActivity.this, "Your cart is empty !", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //BUTTON ALAMAT PENGIRIMAN
        txtAlamat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //TODO cek ini
                Intent intent = new Intent(CartFixedActivity.this, PickupLocationActivity.class);
                startActivity(intent);
                finish();

            }
        });

    }

    private void loadListFood() {
        cart = new Database(this).getCarts(Common.currentUser.getPhone());
        adapter = new CartAdapter(cart,this);
        adapter.notifyDataSetChanged(); //Cp 15
        recyclerView.setAdapter(adapter);

        //Calculate total price
        total = 0;
        for (Order order:cart)
            total+=(Integer.parseInt(order.getPrice()))*(Integer.parseInt(order.getQuantity()));

        int totalitems = 0;
        for (Order order:cart)
            totalitems +=((Integer.parseInt(order.getQuantity())));

        totalPayment = total + (int)deliveryFee;

        txtTotalItems.setText(String.valueOf(totalitems));
        txtTotalPrice.setText(NumberFormat.getInstance(Locale.GERMAN).format(totalPayment));

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals(Common.DELETE))
            deleteCart(item.getOrder());
        return true;
    } // CP 15 Delete cart

    public  void deleteCart(int position) {
        //WE will remove item at List<Order> by position
        cart.remove(position);
        //After that, we will delete all old data from SQLite
        new Database(this).cleanCart(Common.currentUser.getPhone());
        //And final, we will update new data from List<Order> to SQLite
        for (Order item:cart)
            new Database(this).addToCart(item);
        //Refresh
        loadListFood();
    }

    //method swipe to delete
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof CartViewHolder)
        {
            String name = ((CartAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition()).getProductName();

            final Order deleteItem = ((CartAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition());
            final int deleteIndex = viewHolder.getAdapterPosition();

            adapter.removeItem(deleteIndex);
            new Database(this.getBaseContext()).removeFromCart(deleteItem.getProductId(),Common.currentUser.getPhone());

            //Update tctTotal di cart
            //Calculate total price
            total = 0;
            List<Order> orders = new Database(this.getBaseContext()).getCarts(Common.currentUser.getPhone());

            for (Order itemCart : orders)
                total+=(Integer.parseInt(itemCart.getPrice()))*(Integer.parseInt(itemCart.getQuantity()));

            int totalitems = 0;
            for (Order order:cart)
                totalitems +=((Integer.parseInt(order.getQuantity())));

            totalPayment = total + (int)deliveryFee;

            txtTotalItems.setText(String.valueOf(totalitems));
            txtTotalPrice.setText(NumberFormat.getInstance(Locale.GERMAN).format(totalPayment));

            //Make Snackbar
            Snackbar snackbar = Snackbar.make(rootLayout,name + "Removed from cart !",Snackbar.LENGTH_SHORT);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.restoreItem(deleteItem,deleteIndex);
                    new Database(getBaseContext()).addToCart(deleteItem);

                    //Update tctTotal di cart
                    //Calculate total price
                    total = 0;
                    List<Order> orders = new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
                    for (Order itemCart : orders)
                        total+=(Integer.parseInt(itemCart.getPrice()))*(Integer.parseInt(itemCart.getQuantity()));

                    int totalitems = 0;
                    for (Order order:cart)
                        totalitems +=((Integer.parseInt(order.getQuantity())));

                    totalPayment = total + (int)deliveryFee;

                    txtTotalItems.setText(String.valueOf(totalitems));
                    txtTotalPrice.setText(NumberFormat.getInstance(Locale.GERMAN).format(totalPayment));
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }

    //method hitung jarak & ongkir
    private void getRoutes(){

        String lokasiUser = pickupLatitude + "," + pickupLongitude;
        String lokasiShop = shopLatitude + "," + shopLongitude;

        // Panggil Retrofit
        ApiServices api = InitLibrary.getInstance();

        // Siapkan request
        Call<ResponseRoute> routeRequest = api.request_route(lokasiUser, lokasiShop, API_KEY);

        // kirim request
        routeRequest.enqueue(new Callback<ResponseRoute>() {
            @Override
            public void onResponse(Call<ResponseRoute> call, Response<ResponseRoute> response) {

                if (response.isSuccessful()){

                    // tampung response ke variable
                    ResponseRoute dataDirection = response.body();

                    LegsItem dataLegs = dataDirection.getRoutes().get(0).getLegs().get(0);

                    // Dapatkan jarak dan waktu
                    Distance dataDistance = dataLegs.getDistance();
                    Duration dataDuration = dataLegs.getDuration();

                    // ambil Nilai buat Widget
                    double price_per_meter = 4; // x1000 per KM
                    deliveryFee = dataDistance.getValue() * price_per_meter; // Jarak * harga permeter

                    // Set Nilai Ke Widget
                    txtDistance.setText("("+dataDistance.getText()+")");
                    txtDeliveryFee.setText(NumberFormat.getInstance(Locale.GERMAN).format(deliveryFee));

                    totalPayment = total + (int)deliveryFee;
                    txtTotalPrice.setText(NumberFormat.getInstance(Locale.GERMAN).format(totalPayment));

                }
            }

            @Override
            public void onFailure(Call<ResponseRoute> call, Throwable t) {
                t.printStackTrace();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
