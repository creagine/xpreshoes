package com.creaginetech.expresshoes.Fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.creaginetech.expresshoes.Common.Common;
import com.creaginetech.expresshoes.HomeActivity;
import com.creaginetech.expresshoes.Interface.ItemClickListener;
import com.creaginetech.expresshoes.Model.Shop;
import com.creaginetech.expresshoes.R;
import com.creaginetech.expresshoes.ServiceListActivity;
import com.creaginetech.expresshoes.ViewHolder.ShopViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

//gantinya homefragment, udah pake model shop
public class HomeNewFragment extends Fragment {

    //var recycler
    RecyclerView recyclerView;

    //var refresf layout
    SwipeRefreshLayout swipeRefreshLayout;

    //firebase recycler adapter
    FirebaseRecyclerAdapter<Shop,ShopViewHolder> adapter;

    public HomeNewFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //View - cp-28 (Refresh layout)
        swipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(Common.isConnectedToInternet(getActivity().getBaseContext())) //cp15 check intrnt

                    //load list resto
                    loadRestaurant();

                else {
                    Toast.makeText(getActivity().getBaseContext(), "Please Check your connection", Toast.LENGTH_SHORT).show(); //check internet connnection
                    return;
                }
            }
        });

        //Default, load for first time
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if(Common.isConnectedToInternet(getActivity().getBaseContext())) //cp15 check intrnt

                    //load list resto
                    loadRestaurant();

                else {
                    Toast.makeText(getActivity().getBaseContext(), "Please Check your connection", Toast.LENGTH_SHORT).show(); //check internet connnection
                    return;
                }
            }
        });

        //init recycler restaurant
        recyclerView = (RecyclerView)view.findViewById(R.id.recycler_restaurant);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity())); //grid layout halaman home

    return view;
    }

    //method load resto
    private void loadRestaurant() {

        //firebase recycler, model RESTAURANT
        FirebaseRecyclerOptions<Shop> options = new FirebaseRecyclerOptions.Builder<Shop>()
                .setQuery(FirebaseDatabase.getInstance()
                                .getReference()
                                .child("Restaurants")
                        ,Shop.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Shop, ShopViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ShopViewHolder viewHolder, int position, @NonNull Shop model) {

                viewHolder.txt_shop_name.setText(model.getName());

                Picasso.with(getActivity().getBaseContext()).load(model.getImage())
                        .into(viewHolder.img_shop);

                final Shop clickItem = model;

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        //Get CategoryId and send to new Activity
                        Intent foodList = new Intent(getActivity(), ServiceListActivity.class);

                        //When user select restaurant, we will save restaurant id to select category of this restaurant
                        Common.restaurantSelected = adapter.getRef(position).getKey();

                        startActivity(foodList);

                    }
                });
            }

            @Override
            public ShopViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.shop_item, parent, false);
                return new ShopViewHolder(itemView);
            }
        };



        adapter.startListening();
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);

    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onResume() {
        super.onResume();
        //show item in food list when click back from food detail
        if (adapter != null)
            adapter.startListening();
    }

}
