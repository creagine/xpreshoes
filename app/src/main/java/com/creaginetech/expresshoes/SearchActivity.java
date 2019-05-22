package com.creaginetech.expresshoes;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.creaginetech.expresshoes.Common.Common;
import com.creaginetech.expresshoes.Interface.ItemClickListener;
import com.creaginetech.expresshoes.Model.Shop;
import com.creaginetech.expresshoes.ViewHolder.ShopViewHolder;
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

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    DatabaseReference shopList;

    //var recycler
    RecyclerView recyclerView;

    //firebase recycler adapter
    FirebaseRecyclerAdapter<Shop,ShopViewHolder> searchAdapter;

    //search Functionality
    List<String> suggestList = new ArrayList<>();
    MaterialSearchBar materialSearchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        shopList = FirebaseDatabase.getInstance().getReference("Shop");

        //init recycler shop
        recyclerView = findViewById(R.id.recycler_shop);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Search
        materialSearchBar = findViewById(R.id.searchBar);
        materialSearchBar.setHint("Search shop");
        materialSearchBar.enableSearch();
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
                    recyclerView.setAdapter(searchAdapter);
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                //WHen search finish
                //Show result of search adapter

                if (!text.toString().equals("")) {

                    String capitalizedSearchText = text.toString().substring(0, 1).toUpperCase() + text.toString().substring(1);

                    startSearch(capitalizedSearchText);

                }


            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });

    }

    //method search
    private void startSearch(String capitalizedSearchText) {



        //Create query by name
        Query searchByName = shopList.orderByChild("shopName").startAt(capitalizedSearchText).endAt(capitalizedSearchText + "\uf8ff");

        //Create option with query
        FirebaseRecyclerOptions<Shop> serviceOptions = new FirebaseRecyclerOptions.Builder<Shop>()
                .setQuery(searchByName, Shop.class)
                .build();

        //adapter hasil search
        searchAdapter = new FirebaseRecyclerAdapter<Shop, ShopViewHolder>(serviceOptions) {
            //setup viewholder recyclerview hasil search
            @Override
            protected void onBindViewHolder(@NonNull ShopViewHolder viewHolder, int position, @NonNull Shop model) {

                viewHolder.txt_shop_name.setText(model.getShopName());
                Picasso.with(getBaseContext()).load(model.getShopImage())
                        .into(viewHolder.img_shop);

                final Shop clickItem = model;

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        //Get CategoryId and send to new Activity
                        Intent serviceList = new Intent(SearchActivity.this, ServiceListActivity.class);

                        //When user select shop, we will save shop id to select service of this shop
                        Common.shopSelected = searchAdapter.getRef(position).getKey();

                        startActivity(serviceList);

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
        searchAdapter.startListening();
        recyclerView.setAdapter(searchAdapter); // Set adapter for Recycler View is Search result
    }

    //method suggest pada searchbar
    private void loadSuggest() {
        shopList.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Shop item = postSnapshot.getValue(Shop.class);
                    // Add name of shop to suggest list
                    suggestList.add(item.getShopName());
                }

                materialSearchBar.setLastSuggestions(suggestList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (searchAdapter != null)
            searchAdapter.stopListening();
    }

    @Override
    public void onResume() {
        super.onResume();
        //show item in service list when click back from service detail
        if (searchAdapter != null)
            searchAdapter.startListening();
    }

}
