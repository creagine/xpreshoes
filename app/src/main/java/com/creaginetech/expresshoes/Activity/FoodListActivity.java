package com.creaginetech.expresshoes.Activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.creaginetech.expresshoes.Interface.ItemClickListener;
import com.creaginetech.expresshoes.Model.Category;
import com.creaginetech.expresshoes.Model.Food;
import com.creaginetech.expresshoes.R;
import com.creaginetech.expresshoes.ViewHolder.FoodViewHolder;
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

public class FoodListActivity extends AppCompatActivity {

    //variabel recyclerview
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    //variabel firebase
    FirebaseDatabase mFirebaseInstance;
    DatabaseReference mFirebaseDatabase, mFirebaseDatabase1;

    //inisialisasi variabel categoryId
    String categoryId="";

    String foodId="";

    Category currentCategory;

    TextView food_name;
    ImageView food_image;

    ElegantNumberButton numberButton;

    CollapsingToolbarLayout collapsingToolbarLayout;

    //recycler adapter
    public static FirebaseRecyclerAdapter<Food,FoodViewHolder> adapter;

    //search Functionality
    FirebaseRecyclerAdapter<Food,FoodViewHolder> searchAdapter;
    List<String> suggestList = new ArrayList<>();
    MaterialSearchBar materialSearchBar;

    //Bottomsheet
    LinearLayout layoutBottomSheet;
    public static BottomSheetBehavior sheetBehavior;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        //Firebase
        mFirebaseInstance = FirebaseDatabase.getInstance();
        mFirebaseDatabase = mFirebaseInstance.getReference("Foods");
        mFirebaseDatabase1 = mFirebaseInstance.getReference("Category");

        //Bottomsheet
        layoutBottomSheet = findViewById(R.id.bottom_sheet);

        numberButton = findViewById(R.id.number_button);

        //ImageView
        food_image = findViewById(R.id.img_food);

        //collapsing toolbar
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);

        //init recycler view
        recyclerView = findViewById(R.id.recycler_food);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //ambil nilai dari intent HomeFragment
        if (getIntent() != null)
            categoryId = getIntent().getStringExtra("CategoryId");
        if (!categoryId.isEmpty() && categoryId != null)
        {
            //load list berdasarkan nilai yg dikirim dari HomeFragment
            loadListFood(categoryId);
            getDetailFood(categoryId);
        }
        
        //Search
        materialSearchBar = findViewById(R.id.searchBar);
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

        //Bottomsheet
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);
        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN: { }
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED: { }
                    break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
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

        layoutBottomSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(FoodListActivity.this, CartActivity.class);
                startActivity(intent);
            }
        });


//        adapter.getItem(adapter.getItemCount()).getNumber();

//        for (int i = 0; i < adapter.getItemCount(); i++) {
//            String text = tv.getText().toString();
//            tv.setText(text + adapter.getItem(i).getFruit() + " -> " + adapter.getItem().getNumber() + "\n");
//        }



    }

    private void startSearch(CharSequence text)  {
        searchAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(
                Food.class,
                R.layout.food_item,
                FoodViewHolder.class,
                mFirebaseDatabase.orderByChild("name1").equalTo(text.toString()) //Compare name
        ) {
            @Override
            protected void populateViewHolder(FoodViewHolder viewHolder, Food model, int position) {
                viewHolder.food_name.setText(model.getName1());
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.food_image);

                final Food local = model;

//                viewHolder.setItemClickListener(new ItemClickListener() {
//                    @Override
//                    public void onClick(View view, int position, boolean isLongClick) {
//                        //Start new activity
//                        Intent foodDetail = new Intent(FoodListActivity.this,FoodDetailActivity.class);
//                        foodDetail.putExtra("FoodId",searchAdapter.getRef(position).getKey()); // Send Food Id to new activity
//                        startActivity(foodDetail);
//                    }
//                });
            }
        };
        recyclerView.setAdapter(searchAdapter); // Set adapter for Recycler View is Search result
    }

    //load list setelah kolom suggest diisi
    private void loadSuggest()     {
        mFirebaseDatabase.orderByChild("menuId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot:dataSnapshot.getChildren())
                        {
                            Food item = postSnapshot.getValue(Food.class);
                            suggestList.add(item.getName1()); // Add name of food to suggest list
                        }

                    }

                    //versi firlaa
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }

                });
    }

    //load list default
    private void loadListFood(String categoryId) {
        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(Food.class,R.layout.food_item,
                FoodViewHolder.class,mFirebaseDatabase.orderByChild("menuId").equalTo(categoryId)){
            // like : Select * from Foods where menuId =
            @Override
            protected void populateViewHolder(FoodViewHolder viewHolder, Food model, int position) {
                viewHolder.food_name.setText(model.getName1());
                viewHolder.food_price.setText(model.getPrice());
                viewHolder.food_description.setText(model.getDescription());
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.food_image);

                final Food local = model;

//                viewHolder.setItemClickListener(new ItemClickListener() {
//                    @Override
//                    public void onClick(View view, int position, boolean isLongClick) {
//                        //Start new activity
//                        Intent foodDetail = new Intent(FoodListActivity.this,FoodDetailActivity.class);
//                        foodDetail.putExtra("FoodId",adapter.getRef(position).getKey()); // Send Food Id to new activity
//                        startActivity(foodDetail);
//                    }
//                });

            }
        };

//        //Set Adapter
//        Log.d("TAG",""+adapter.getItemCount());
        recyclerView.setAdapter(adapter);
    }

    //ambil info detail food
    private void getDetailFood(String categoryId) {
        mFirebaseDatabase1.child(categoryId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentCategory = dataSnapshot.getValue(Category.class);

                //Set Image
                Picasso.with(getBaseContext()).load(currentCategory.getImage())
                        .into(food_image);

                collapsingToolbarLayout.setTitle(currentCategory.getName());
//
//                food_price.setText(currentFood.getPrice());

//                food_name.setText(currentFood.getName1());

//                food_description.setText(currentFood.getDescription());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
