package com.creaginetech.expresshoes;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.creaginetech.expresshoes.Common.Common;
import com.creaginetech.expresshoes.Database.Database;
import com.creaginetech.expresshoes.Interface.ItemClickListener;
import com.creaginetech.expresshoes.Model.Order;
import com.creaginetech.expresshoes.Model.Service;
import com.creaginetech.expresshoes.Model.Shop;
import com.creaginetech.expresshoes.ViewHolder.ServiceViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.support.design.widget.BottomSheetBehavior.STATE_EXPANDED;
import static android.support.design.widget.BottomSheetBehavior.STATE_HIDDEN;

public class ServiceListActivity extends AppCompatActivity {

    //variable recycler
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FirebaseRecyclerAdapter<Service, ServiceViewHolder> adapter;

    //variable firebase database
    FirebaseDatabase database;
    DatabaseReference serviceList, shopRef;

    //local database for cart
    Database localDB;

    //variable widget
    BottomSheetBehavior sheetBehavior;
    LinearLayout layoutBottomSheet;
    TextView txtTotalItems, txtTotalPrice, txtShopName, txtShopAddress;
    ImageView shopImage;
    CollapsingToolbarLayout collapsingToolbarLayout;
    ProgressBar progressBarServiceList;

    List<Order> cart = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_list);

        //Firebase database
        database = FirebaseDatabase.getInstance();
        serviceList = database.getReference("Service").child(Common.shopSelected); //EDIT THIS
        shopRef = database.getReference("Shop").child(Common.shopSelected);

        //local database for cart
        localDB = new Database(this);

        //widget init
        layoutBottomSheet = findViewById(R.id.bottom_sheet);
        txtTotalItems = findViewById(R.id.totalitems);
        txtTotalPrice = findViewById(R.id.totalprice);
        txtShopName = findViewById(R.id.shop_name);
        txtShopAddress = findViewById(R.id.textViewShopAddress);
        shopImage = findViewById(R.id.img_shop);
        collapsingToolbarLayout = findViewById(R.id.collapsing);
        recyclerView = findViewById(R.id.recycler_service);
        progressBarServiceList = findViewById(R.id.progressBarServiceList);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);

        //bottom sheet
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);

        loadListService();
        getShopDetail();

        //bottomsheet on click
        layoutBottomSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(ServiceListActivity.this, CartNewActivity.class);
                startActivity(intent);
            }
        });

    }

    private void getShopDetail() {

        shopRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Shop currentShop = dataSnapshot.getValue(Shop.class);

                //Set Image
                Picasso.with(getBaseContext()).load(currentShop.getShopImage())
                        .into(shopImage);

//                collapsingToolbarLayout.setTitle(currentShop.getShopName());

//                setCollapsingToolbarTitle(currentShop.getShopName());

                txtShopName.setText(currentShop.getShopName());
                txtShopAddress.setText(currentShop.getShopAddress());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    //method load list makanan
    private void loadListService() {
        progressBarServiceList.setVisibility(View.VISIBLE);

        //firebase recycler, model Shop
        FirebaseRecyclerOptions<Service> options = new FirebaseRecyclerOptions.Builder<Service>()
                .setQuery(serviceList
                        , Service.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Service, ServiceViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ServiceViewHolder viewHolder, final int position, @NonNull final Service model) {

                progressBarServiceList.setVisibility(View.GONE);

                viewHolder.serviceName.setText(model.getServiceName());
                viewHolder.servicePrice.setText(NumberFormat.getInstance(Locale.GERMAN).format(Integer.parseInt(model.getPrice())));
                Picasso.with(getBaseContext()).load(model.getServiceImage())
                        .into(viewHolder.serviceImage);

                //Add Quick Cart
                viewHolder.quick_cart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        boolean isExists = new Database(getBaseContext()).checkFoodExists(adapter.getRef(position).getKey(), Common.currentUser.getPhone());

                        if (!isExists) {
                            new Database(getBaseContext()).addToCart(new Order(
                                    Common.currentUser.getPhone(),
                                    //copy from foodDetail
                                    adapter.getRef(position).getKey(),
                                    model.getServiceName(),
                                    "1",
                                    model.getPrice(),
                                    model.getServiceImage()

                            ));

                        } else {
                            new Database(getBaseContext()).increaseCart(Common.currentUser.getPhone(), adapter.getRef(position).getKey());

                        }

                        //UPDATE TOTAL ITEMS DI BOTTOM SHEET
                        cart = new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
                        int totalitems = 0;
                        for (Order order : cart)
                            totalitems += ((Integer.parseInt(order.getQuantity())));

                        int total = 0;
                        for (Order order : cart)
                            total += (Integer.parseInt(order.getPrice())) * (Integer.parseInt(order.getQuantity()));

                        txtTotalItems.setText(String.valueOf(totalitems));
                        txtTotalPrice.setText(NumberFormat.getInstance(Locale.GERMAN).format(total));

                        if(totalitems == 0){
                            sheetBehavior.setState(STATE_HIDDEN);
                        }
                        if(totalitems == 1){
                            sheetBehavior.setState(STATE_EXPANDED);
                            sheetBehavior.setSkipCollapsed(true);
                        }

                    }
                });


                //Add favorites
                if (localDB.isFavorites(adapter.getRef(position).getKey(), Common.currentUser.getPhone()))
                    viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);

                //Click to change state of favorites
                viewHolder.fav_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!localDB.isFavorites(adapter.getRef(position).getKey(), Common.currentUser.getPhone())) {
                            localDB.addToFavorites(adapter.getRef(position).getKey(), Common.currentUser.getPhone());
                            viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);
                            Toast.makeText(ServiceListActivity.this, "" + model.getServiceName() + " was added to Favorites", Toast.LENGTH_SHORT).show();
                        } else {
                            localDB.removeFromFavorites(adapter.getRef(position).getKey(), Common.currentUser.getPhone());
                            viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                            Toast.makeText(ServiceListActivity.this, "" + model.getServiceName() + " was removed from Favorites", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                final Service local = model;

                //fungsi ketika item pada list makanan diclick
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Start new activity
                        Intent serviceDetail = new Intent(ServiceListActivity.this, ServiceDetailActivity.class);
                        Common.serviceSelected = adapter.getRef(position).getKey(); // Send service Id to new activity
                        startActivity(serviceDetail);
                    }
                });
            }


            @Override
            public ServiceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.service_item, parent, false);
                return new ServiceViewHolder(itemView);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null)
            adapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //show item in food list when click back from food detail
        if (adapter != null)
            adapter.startListening();

        //TOTAL ITEMS DI BOTTOM SHEET
        //fungsinya ketika pada class foodlist pindah ke CartNew jumlah items pada
        //halaman CartNew ditambah maka ketika kembali ke halaman FoodList, jumlah items
        //di bottomsheet terupdate
        cart = new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
        int totalitems = 0;
        for (Order order : cart)
            totalitems += ((Integer.parseInt(order.getQuantity())));

        int total = 0;
        for (Order order : cart)
            total += (Integer.parseInt(order.getPrice())) * (Integer.parseInt(order.getQuantity()));

        txtTotalItems.setText(String.valueOf(totalitems));
        txtTotalPrice.setText(NumberFormat.getInstance(Locale.GERMAN).format(total));

        if(totalitems == 0){
            sheetBehavior.setState(STATE_HIDDEN);
        }
        if(totalitems == 1){
            sheetBehavior.setState(STATE_EXPANDED);
        }

    }

    private void deletecart() {

        //WE will remove item at List<Order> by position
        cart.removeAll(cart);
        //After that, we will delete all old data from SQLite
        new Database(this).cleanCart(Common.currentUser.getPhone());
        //And final, we will update new data from List<Order> to SQLite
        for (Order item : cart)
            new Database(this).addToCart(item);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        //ketika back atau keluar dari suatu restoran maka pesanan pada restoran sebelumnya akan
        //dihapus dan akan mengulangi pesanan baru lagi setiap memasuki restoran baru
        deletecart();

    }

}
