package com.creaginetech.expresshoes.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.creaginetech.expresshoes.Activity.FoodListActivity;
import com.creaginetech.expresshoes.Interface.ItemClickListener;
import com.creaginetech.expresshoes.Model.Category;
import com.creaginetech.expresshoes.R;
import com.creaginetech.expresshoes.ViewHolder.MenuViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    //deklarasi variabel
    private FirebaseDatabase mFirebaseInstance;
    private DatabaseReference mFirebaseDatabase;
    TextView txtFullName;
    RecyclerView recycler_menu;
    RecyclerView.LayoutManager layoutManager;

    //recycler adapter firebase
    FirebaseRecyclerAdapter<Category,MenuViewHolder> adapter;

    //search Functionality
    FirebaseRecyclerAdapter<Category,MenuViewHolder> searchAdapter;
    List<String> suggestList = new ArrayList<>();
    MaterialSearchBar materialSearchBar;

    //constructor
    public HomeFragment() {
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

        //Init Firebase
        mFirebaseInstance = FirebaseDatabase.getInstance();
        mFirebaseDatabase = mFirebaseInstance.getReference("Category");

        //Recycler view
        recycler_menu = view.findViewById(R.id.recycler_menu);
        recycler_menu.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recycler_menu.setLayoutManager(layoutManager);

        loadMenu();

        //Search
        materialSearchBar = view.findViewById(R.id.searchBar);
        materialSearchBar.setHint("Enter your food");

        loadSuggest(); // Write function to load suggest from firebase

        materialSearchBar.setLastSuggestions(suggestList);
        materialSearchBar.setCardViewElevation(10);

        materialSearchBar.addTextChangeListener(new TextWatcher()  {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //When user type their text, we will change suggest list

                List<String> suggest = new ArrayList<String>();
                for (String search:suggestList) // loop in suggest list
                {
                    if (search.toUpperCase().contains(materialSearchBar.getText().toUpperCase()))
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
                    recycler_menu.setAdapter(adapter);
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

        // Inflate the layout for this fragment
        return view;
    }

    //method load menu
    private void loadMenu() {

        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(Category.class, R.layout.menu_item,MenuViewHolder.class, mFirebaseDatabase) {

            @Override
            protected void populateViewHolder(MenuViewHolder viewHolder, Category model, int position) {

                //nama menu diambil dari database
                viewHolder.txtMenuName.setText(model.getName());

                //load gambar pada tiap item
                Picasso.with(getActivity().getBaseContext()).load(model.getImage())
                        .into(viewHolder.imageView);

                //ambil nilai item yg diklik
                final Category clickItem = model;


                //recycler category itemclick
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        //Get CategoryId and send to new Activity
                        Intent foodList = new Intent(getActivity(),FoodListActivity.class);
                        //Because CategoryId
                        foodList.putExtra("CategoryId",adapter.getRef(position).getKey());
                        startActivity(foodList);

                    }
                });

            }

        };

        //set nilai yg diambil dari database kedalam adapter recyclerview
        recycler_menu.setAdapter(adapter);

    }

    private void startSearch(CharSequence text)  {
        searchAdapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(
                Category.class,
                R.layout.menu_item,
                MenuViewHolder.class,
                mFirebaseDatabase.orderByChild("Name").equalTo(text.toString()) //Compare name
        ) {
            @Override
            protected void populateViewHolder(MenuViewHolder viewHolder, Category model, int position) {
                viewHolder.txtMenuName.setText(model.getName());
                Picasso.with(getActivity().getBaseContext()).load(model.getImage())
                        .into(viewHolder.imageView);

                final Category local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Start new activity
                        Intent foodlist = new Intent(getActivity(),FoodListActivity.class);
                        foodlist.putExtra("CategoryId",searchAdapter.getRef(position).getKey()); // Send Food Id to new activity
                        startActivity(foodlist);
                    }
                });
            }
        };
        recycler_menu.setAdapter(searchAdapter); // Set adapter for Recycler View is Search result
    }

    //load list setelah kolom suggest diisi
    private void loadSuggest()     {
        mFirebaseDatabase.orderByChild("Name")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot:dataSnapshot.getChildren())
                        {
                            Category item = postSnapshot.getValue(Category.class);
                            suggestList.add(item.getName()); // Add name of food to suggest list
                        }

                    }

                    //versi firlaa
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }

                });
    }

}
