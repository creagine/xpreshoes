package com.creaginetech.expresshoes;

import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.creaginetech.expresshoes.Common.Common;
import com.creaginetech.expresshoes.Database.Database;
import com.creaginetech.expresshoes.Model.Order;
import com.creaginetech.expresshoes.Model.Rating;
import com.creaginetech.expresshoes.Model.Service;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import java.util.Arrays;

//pengganti class foodDetailActivity
public class ServiceDetailActivity extends AppCompatActivity implements RatingDialogListener {

    TextView service_name,service_price,service_description;
    ImageView service_image;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton btnRating;
    CounterFab btnCart;
    ElegantNumberButton numberButton;
    RatingBar ratingBar;

    String serviceId="";

    FirebaseDatabase database;
    DatabaseReference service;
    DatabaseReference ratingTbl;

    Service currentService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_detail);

        //Firebase
        database = FirebaseDatabase.getInstance();
        service = database.getReference("shop").child(Common.restaurantSelected)
                .child("detail").child("service");
        ratingTbl = database.getReference("Rating");

        //Init View
        numberButton = (ElegantNumberButton)findViewById(R.id.number_button);
        btnCart = (CounterFab) findViewById(R.id.btnCart);
        btnRating = (FloatingActionButton)findViewById(R.id.btn_rating);
        ratingBar = (RatingBar)findViewById(R.id.ratingBar);

        //Rating
        btnRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRatingDialog();
            }
        });

        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Database(getBaseContext()).addToCart(new Order(
                        Common.currentUser.getPhone(),
                        serviceId,
                        currentService.getServiceName(),
                        numberButton.getNumber(),
                        currentService.getPrice(),
                        currentService.getServiceImage()



                ));

                Toast.makeText(ServiceDetailActivity.this, "Added to Cart", Toast.LENGTH_SHORT).show();
            }
        });

        btnCart.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));

        service_description = (TextView)findViewById(R.id.service_description);
        service_name = (TextView)findViewById(R.id.service_name);
        service_price = (TextView)findViewById(R.id.service_price);
        service_image = (ImageView)findViewById(R.id.img_service);

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);

        //Get Food Id from Intent
        if (getIntent() != null)
            serviceId = getIntent().getStringExtra("serviceId");
        if (!serviceId.isEmpty())
        {
            if (Common.isConnectedToInternet(getBaseContext()))
            {
                getDetailService(serviceId);
                getRatingService(serviceId);
            }
            else {
                Toast.makeText(ServiceDetailActivity.this, "Please Check your connection", Toast.LENGTH_SHORT).show(); //check internet connnection
            }
        }
    }

    private void getRatingService(String serviceId) {
        Query foodRating = ratingTbl.orderByChild("foodId").equalTo(serviceId);

        foodRating.addValueEventListener(new ValueEventListener() {
            int count=0,sum=0;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot:dataSnapshot.getChildren())
                {
                    Rating item = postSnapshot.getValue(Rating.class);
                    sum+= Integer.parseInt(item.getRateValue());
                    count++;
                }
                if (count != 0)
                {
                    float average = sum/count;
                    ratingBar.setRating(average);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showRatingDialog() {
        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNoteDescriptions(Arrays.asList("Very Bad","Not Good","Quite Ok","Very Good","Excellent"))
                .setDefaultRating(1)
                .setTitle("Rate this food")
                .setDescription("Please select some stars and give your feedback")
                .setTitleTextColor(R.color.colorPrimary)
                .setDescriptionTextColor(R.color.colorPrimary)
                .setHint("Please write your comment here...")
                .setHintTextColor(R.color.colorAccent)
                .setCommentTextColor(android.R.color.white)
                .setCommentBackgroundColor(R.color.colorPrimaryDark)
                .setWindowAnimation(R.style.RatingDialogFadeAnim)
                .create(ServiceDetailActivity.this)
                .show();
    }

    private void getDetailService(String serviceId) {
        service.child(serviceId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentService = dataSnapshot.getValue(Service.class);

                //Set Image
                Picasso.with(getBaseContext()).load(currentService.getServiceImage())
                        .into(service_image);

                collapsingToolbarLayout.setTitle(currentService.getServiceName());

                service_price.setText(currentService.getPrice());

                service_name.setText(currentService.getServiceName());

                service_description.setText(currentService.getDescription());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //Rating
    @Override
    public void onNegativeButtonClicked() {

    }

    @Override
    public void onPositiveButtonClicked(int value, String comments) {
        //Get Rating and Upload to firebase
        final Rating rating = new Rating(Common.currentUser.getPhone(),
                serviceId,
                String.valueOf(value),
                comments);
        ratingTbl.child(Common.currentUser.getPhone()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(Common.currentUser.getPhone()).exists())
                {
                    //Remove old value (you can delete or let it be - useless function)
                    ratingTbl.child(Common.currentUser.getPhone()).removeValue();
                    //Update new value
                    ratingTbl.child(Common.currentUser.getPhone()).setValue(rating);

                }
                else
                {
                    //Update new value
                    ratingTbl.child(Common.currentUser.getPhone()).setValue(rating);
                }
                Toast.makeText(ServiceDetailActivity.this, "Thank you for submit rating !", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}
