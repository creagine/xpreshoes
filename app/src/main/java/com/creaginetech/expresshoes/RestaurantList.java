package com.creaginetech.expresshoes;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.creaginetech.expresshoes.Common.Common;
import com.creaginetech.expresshoes.Interface.ItemClickListener;
import com.creaginetech.expresshoes.Model.Category;
import com.creaginetech.expresshoes.Model.Restaurant;
import com.creaginetech.expresshoes.ViewHolder.MenuViewHolder;
import com.creaginetech.expresshoes.ViewHolder.RestaurantViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class RestaurantList extends AppCompatActivity {

    AlertDialog waitingDialog;
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeRefreshLayout;


    FirebaseRecyclerAdapter<Restaurant,RestaurantViewHolder> adapter;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_list);

        //View - cp-28 (Refresh layout)
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(Common.isConnectedToInternet(getBaseContext())) //cp15 check intrnt
                    loadRestaurant();
                else {
                    Toast.makeText(getBaseContext(), "Please Check your connection", Toast.LENGTH_SHORT).show(); //check internet connnection
                    return;
                }
            }
        });

        //Default, load for first time
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if(Common.isConnectedToInternet(getBaseContext())) //cp15 check intrnt
                    loadRestaurant();
                else {
                    Toast.makeText(getBaseContext(), "Please Check your connection", Toast.LENGTH_SHORT).show(); //check internet connnection
                    return;
                }
            }
        });

        //Load restaurant
        recyclerView = (RecyclerView)findViewById(R.id.recycler_restaurant);
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); //grid layout halaman home


    }

    private void loadRestaurant() {

        FirebaseRecyclerOptions<Restaurant> options = new FirebaseRecyclerOptions.Builder<Restaurant>()
                .setQuery(FirebaseDatabase.getInstance()
                                .getReference()
                                .child("Restaurants")
                        ,Restaurant.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Restaurant, RestaurantViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RestaurantViewHolder viewHolder, int position, @NonNull Restaurant model) {
                viewHolder.txt_restaurant_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.img_restaurant);
                final Restaurant clickItem = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Get CategoryId and send to new Activity
                        Intent foodList = new Intent(RestaurantList.this, HomeActivity.class);
                        //When user select restaurant, we will save restaurant id to select category of this restaurant
                        Common.restaurantSelected = adapter.getRef(position).getKey();
                        startActivity(foodList);
                    }
                });
            }

            @Override
            public RestaurantViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.restaurant_item, parent, false);
                return new RestaurantViewHolder(itemView);
            }
        };



        adapter.startListening();
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);

    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //show item in food list when click back from food detail
        if (adapter != null)
            adapter.startListening();
    }
}
