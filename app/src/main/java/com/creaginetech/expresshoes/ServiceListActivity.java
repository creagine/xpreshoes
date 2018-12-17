package com.creaginetech.expresshoes;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.creaginetech.expresshoes.Common.Common;
import com.creaginetech.expresshoes.Database.Database;
import com.creaginetech.expresshoes.Interface.ItemClickListener;
import com.creaginetech.expresshoes.Model.Order;
import com.creaginetech.expresshoes.Model.Service;
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
    DatabaseReference serviceList;

    //variable category id
    String categoryId = "";

    //search Functionality
    FirebaseRecyclerAdapter<Service, ServiceViewHolder> searchAdapter;
    List<String> suggestList = new ArrayList<>();
    MaterialSearchBar materialSearchBar;

    //local database for cart
    Database localDB;

    //variable widget
    SwipeRefreshLayout swipeRefreshLayout;
    BottomSheetBehavior sheetBehavior;
    LinearLayout layoutBottomSheet;
    TextView txtTotalItems, txtTotalPrice;

    List<Order> cart = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_list);

        //Firebase database
        database = FirebaseDatabase.getInstance();
        serviceList = database.getReference("shop").child(Common.restaurantSelected)
                .child("detail").child("service");

        //local database for cart
        localDB = new Database(this);

        //Swipe refresh layout
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        //method ketika di refresh
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Get Intent Here
                //mengambil nilai dari intent sebelumnya
                //merupakan nilai CategoryId (restorean) yang mewakili untuk menampilkan menu dari
                //CategoryId / restoran tersebut

                    if (Common.isConnectedToInternet(getBaseContext()))
                        loadListService();
                    else {
                        Toast.makeText(ServiceListActivity.this, "Please Check your connection", Toast.LENGTH_SHORT).show(); //check internet connnection
                        return;
                    }
            }
        });

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {

                    if (Common.isConnectedToInternet(getBaseContext()))
                        loadListService();
                    else {
                        Toast.makeText(ServiceListActivity.this, "Please Check your connection", Toast.LENGTH_SHORT).show(); //check internet connnection
                        return;
                    }

                //Because search function need category so we need paste code here
                //after getIntent categoryId
                //Search
                materialSearchBar = (MaterialSearchBar) findViewById(R.id.searchBar);
                materialSearchBar.setHint("Enter your food");
                loadSuggest(); // Write function to load suggest from firebase

                materialSearchBar.setCardViewElevation(10);
                materialSearchBar.addTextChangeListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        //When user type their text, we will change suggest list

                        List<String> suggest = new ArrayList<String>();
                        for (String search : suggestList) // loop in suggest list
                        {
                            if (search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                                suggest.add(search);
                        }
                        materialSearchBar.setLastSuggestions(suggest);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });
                materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
                    @Override
                    public void onSearchStateChanged(boolean enabled) {
                        //When Search Bar is close
                        //Restore original adapter
                        if (!enabled)
                            recyclerView.setAdapter(adapter);
                    }

                    @Override
                    public void onSearchConfirmed(CharSequence text) {
                        //WHen search finish
                        //Show result of search adapter

                        startSearch(text);


                    }

                    @Override
                    public void onButtonClicked(int buttonCode) {

                    }
                });
            }
        });

        //widget init
        recyclerView = findViewById(R.id.recycler_service);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        //BOTTOMSHEET
        layoutBottomSheet = findViewById(R.id.bottom_sheet);
        txtTotalItems = findViewById(R.id.totalitems);
        txtTotalPrice = findViewById(R.id.totalprice);

        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);
        sheetBehavior.setState(STATE_HIDDEN);

        /**
         * bottom sheet state change listener
         * we are changing button text when sheet changed state
         * */
        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED: {
                    }
                    break;
                    case BottomSheetBehavior.STATE_COLLAPSED: {
                    }
                    break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        //bottomsheet on click
        layoutBottomSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(ServiceListActivity.this, CartNewActivity.class);
                startActivity(intent);
            }
        });

    }

    //method search
    private void startSearch(CharSequence text) {

        //Create query by name
        Query searchByName = serviceList.orderByChild("serviceName").equalTo(text.toString());

        //Create option with query
        FirebaseRecyclerOptions<Service> serviceOptions = new FirebaseRecyclerOptions.Builder<Service>()
                .setQuery(searchByName, Service.class)
                .build();

        //adapter hasil search
        searchAdapter = new FirebaseRecyclerAdapter<Service, ServiceViewHolder>(serviceOptions) {
            //setup viewholder recyclerview hasil search
            @Override
            protected void onBindViewHolder(@NonNull ServiceViewHolder viewHolder, int position, @NonNull Service model) {
                viewHolder.serviceName.setText(model.getServiceName());
                Picasso.with(getBaseContext()).load(model.getServiceImage())
                        .into(viewHolder.serviceImage);

                final Service local = model;

                //fungsi ketika item pada list makanan diclick
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Start new activity
                        Intent serviceDetail = new Intent(ServiceListActivity.this, ServiceDetailActivity.class);
                        serviceDetail.putExtra("serviceId", searchAdapter.getRef(position).getKey()); // Send Food Id to new activity
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
        searchAdapter.startListening();
        recyclerView.setAdapter(searchAdapter); // Set adapter for Recycler View is Search result
    }

    //method suggest pada searchbar
    private void loadSuggest() {
        serviceList.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            Service item = postSnapshot.getValue(Service.class);
                            suggestList.add(item.getServiceName()); // Add name of food to suggest list
                        }

                        materialSearchBar.setLastSuggestions(suggestList);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    //method load list makanan
    private void loadListService() {

        //firebase recycler, model Shop
        FirebaseRecyclerOptions<Service> options = new FirebaseRecyclerOptions.Builder<Service>()
                .setQuery(FirebaseDatabase.getInstance()
                                .getReference()
                                .child("shop").child(Common.restaurantSelected)
                                .child("detail").child("service")
                        ,Service.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Service, ServiceViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ServiceViewHolder viewHolder, final int position, @NonNull final Service model) {
                viewHolder.serviceName.setText(model.getServiceName());
                viewHolder.servicePrice.setText(String.format("$ %s", model.getPrice()));
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
                        for (Order order:cart)
                            totalitems +=((Integer.parseInt(order.getQuantity())));

                        int total = 0;
                        for (Order order:cart)
                            total+=(Integer.parseInt(order.getPrice()))*(Integer.parseInt(order.getQuantity()));
                        Locale locale = new Locale("en","US");
                        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

                        txtTotalItems.setText(String.valueOf(totalitems));
                        txtTotalPrice.setText(fmt.format(total));

                        sheetBehavior.setState(STATE_EXPANDED);
                        Toast.makeText(ServiceListActivity.this, "Added to Cart", Toast.LENGTH_SHORT).show();
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
                        serviceDetail.putExtra("serviceId", adapter.getRef(position).getKey()); // Send service Id to new activity
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
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null)
            adapter.stopListening();
//        searchAdapter.stopListening();
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
        for (Order order:cart)
            totalitems +=((Integer.parseInt(order.getQuantity())));

        int total = 0;
        for (Order order:cart)
            total+=(Integer.parseInt(order.getPrice()))*(Integer.parseInt(order.getQuantity()));
        Locale locale = new Locale("en","US");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

        txtTotalItems.setText(String.valueOf(totalitems));
        txtTotalPrice.setText(fmt.format(total));

    }

    private void deletecart(){

        //WE will remove item at List<Order> by position
        cart.removeAll(cart);
        //After that, we will delete all old data from SQLite
        new Database(this).cleanCart(Common.currentUser.getPhone());
        //And final, we will update new data from List<Order> to SQLite
        for (Order item:cart)
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
