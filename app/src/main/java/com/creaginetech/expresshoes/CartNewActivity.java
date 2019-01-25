package com.creaginetech.expresshoes;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.creaginetech.expresshoes.Common.Common;
import com.creaginetech.expresshoes.Database.Database;
import com.creaginetech.expresshoes.Helper.RecyclerItemTouchHelper;
import com.creaginetech.expresshoes.Interface.RecyclerItemTouchHelperListener;
import com.creaginetech.expresshoes.Model.MyResponse;
import com.creaginetech.expresshoes.Model.Notification;
import com.creaginetech.expresshoes.Model.Order;
import com.creaginetech.expresshoes.Model.Request;
import com.creaginetech.expresshoes.Model.Sender;
import com.creaginetech.expresshoes.Model.Token;
import com.creaginetech.expresshoes.Remote.APIService;
import com.creaginetech.expresshoes.ViewHolder.CartAdapter;
import com.creaginetech.expresshoes.ViewHolder.CartViewHolder;
import com.creaginetech.expresshoes.network.AppUtils;
import com.creaginetech.expresshoes.network.FetchAddressIntentService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartNewActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,LocationListener, RecyclerItemTouchHelperListener {

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
    Button btnPlace;
    ConstraintLayout rootLayout;

    String address;

    //CART ADAPTER
    List<Order> cart = new ArrayList<>();
    CartAdapter adapter;

    APIService mService;

    //Location-cp 36
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static final int UPDATE_INTERVAL = 5000;
    private static final int FATEST_INTERVAL = 3000;
    private static final int DISPLACEMENT = 10;

    private static final int LOCATION_REQUEST_CODE = 9999;
    private static final int PLAY_SERVICES_REQUEST = 9997;

    /**
     * Receiver registered with this activity to get the response from FetchAddressIntentService.
     */
    private AddressResultReceiver mResultReceiver;
    /**
     * The formatted location address.
     */
    protected String mSreetOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_new);

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
            if (checkPlayServices()) //if have play service on device
            {
                buildGoogleApiClient();
                createLocationRequest();
            }
        }

        //Init FCM Service buat notif
        mService = Common.getFCMService();

        rootLayout = findViewById(R.id.rootLayout);

        //Firebase DATABASE
        database = FirebaseDatabase.getInstance();
        requestReference = database.getReference("order").child(Common.restaurantSelected);

        //Init RECYCLER
        recyclerView = findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //Swipe to delete
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0,ItemTouchHelper.LEFT,this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        //ngambil detail alamat dari recent location
        mResultReceiver = new AddressResultReceiver(new Handler());

        //init widget
        txtTotalPrice = findViewById(R.id.total);
        txtTotalItems = findViewById(R.id.totalItemsCart);
        txtAlamat = findViewById(R.id.textViewAlamat);
        btnPlace = findViewById(R.id.btnPlaceOrder);

        //BUTTON PLACE ORDER
        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (cart.size() > 0) {

                    //Create new request
                    Request request = new Request(
                            Common.currentUser.getPhone(),
                            Common.currentUser.getName(),
                            txtAlamat.getText().toString(),
                            txtTotalPrice.getText().toString(),
                            "0", // status
                            String.format("%s,%s",mLastLocation.getLatitude(),mLastLocation.getLongitude()),
                            Common.restaurantSelected,
                            cart
                    );

                    //Submit to Firebase
                    //We will using System.CurrentMilli to key
                    String order_number = String.valueOf(System.currentTimeMillis());
                    requestReference.child(order_number)
                            .setValue(request);

                    //Delete Cart
                new Database(getBaseContext()).cleanCart(Common.currentUser.getPhone());
                    Toast.makeText(CartNewActivity.this, "Thank you , Order Placed", Toast.LENGTH_SHORT).show();
                finish();

                } else {
                    Toast.makeText(CartNewActivity.this, "Your cart is empty !", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //BUTTON ALAMAT PENGIRIMAN
        txtAlamat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(CartNewActivity.this, OjekActivity.class);
                startActivity(intent);

            }
        });

        loadListFood();

    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private synchronized void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS)
        {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICES_REQUEST).show();
            else
            {
                Toast.makeText(this, "This device is not supported", Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case LOCATION_REQUEST_CODE:
            {
                if (grantResults.length >0&& grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if (checkPlayServices()) //if have play service on device
                    {
                        buildGoogleApiClient();
                        createLocationRequest();
                    }
                }
            }
        }
    }

    private void sendNotificationOrder(final String order_number) {

        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");

        Query data = tokens.orderByChild("serverToken").equalTo(true); //get all node with isServerToken is true

        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot:dataSnapshot.getChildren())
                {
                    Token serverToken = postSnapshot.getValue(Token.class);

                    //Create raw payload to send
                    Notification notification = new Notification("Expreshoes","You have new order "+order_number);
                    Sender content = new Sender(serverToken.getToken(),notification);

                    mService.sendNotification(content)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    //only run when get result
                                    if (response.code() == 200) {
                                        if (response.body().success == 1) {
                                            Toast.makeText(CartNewActivity.this, "Thank you , Order Place", Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(CartNewActivity.this, "Failed !!!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                    Log.e("ERROR",t.getMessage());
                                }
                            });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadListFood() {
        cart = new Database(this).getCarts(Common.currentUser.getPhone());
        adapter = new CartAdapter(cart,this);
        adapter.notifyDataSetChanged(); //Cp 15
        recyclerView.setAdapter(adapter);

        //Calculate total price
        int total = 0;
        for (Order order:cart)
            total+=(Integer.parseInt(order.getPrice()))*(Integer.parseInt(order.getQuantity()));

        int totalitems = 0;
        for (Order order:cart)
            totalitems +=((Integer.parseInt(order.getQuantity())));

        txtTotalItems.setText(String.valueOf(totalitems));
        txtTotalPrice.setText(NumberFormat.getInstance(Locale.GERMAN).format(total));
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

    //cp 36
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null)
        {
            Log.d("LOCATION","Your location : "+mLastLocation.getLatitude()+","+mLastLocation.getLongitude());

            try {

                Location mLocation = new Location("");
                mLocation.setLatitude(+mLastLocation.getLatitude());
                mLocation.setLongitude(mLastLocation.getLongitude());

                startIntentService(mLocation);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        else
        {
            Log.d("LOCATION","Could not get your location");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();
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
            int total = 0;
            List<Order> orders = new Database(this.getBaseContext()).getCarts(Common.currentUser.getPhone());

            for (Order itemCart : orders)
                total+=(Integer.parseInt(itemCart.getPrice()))*(Integer.parseInt(itemCart.getQuantity()));

            int totalitems = 0;
            for (Order order:cart)
                totalitems +=((Integer.parseInt(order.getQuantity())));

            txtTotalItems.setText(String.valueOf(totalitems));

            txtTotalPrice.setText(NumberFormat.getInstance(Locale.GERMAN).format(total));

            //Make Snackbar
            Snackbar snackbar = Snackbar.make(rootLayout,name + "Removed from cart !",Snackbar.LENGTH_SHORT);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.restoreItem(deleteItem,deleteIndex);
                    new Database(getBaseContext()).addToCart(deleteItem);

                    //Update tctTotal di cart
                    //Calculate total price
                    int total = 0;
                    List<Order> orders = new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
                    for (Order itemCart : orders)
                        total+=(Integer.parseInt(itemCart.getPrice()))*(Integer.parseInt(itemCart.getQuantity()));

                    int totalitems = 0;
                    for (Order order:cart)
                        totalitems +=((Integer.parseInt(order.getQuantity())));

                    txtTotalItems.setText(String.valueOf(totalitems));

                    txtTotalPrice.setText(NumberFormat.getInstance(Locale.GERMAN).format(total));
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }

    //method buat recent location
    //START
    /**
     * Receiver for data sent from FetchAddressIntentService.
     */
    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         * Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string or an error message sent from the intent service.
            mSreetOutput = resultData.getString(AppUtils.LocationConstants.LOCATION_DATA_STREET);
            displayAddressOutput();

            // Show a toast message if an address was found.
            if (resultCode == AppUtils.LocationConstants.SUCCESS_RESULT) {
                //  showToast(getString(R.string.address_found));


            }


        }

    }

    protected void displayAddressOutput() {
        try {
            if (mSreetOutput != null)

                txtAlamat.setText(mSreetOutput);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates an intent, adds location data to it as an extra, and starts the intent service for
     * fetching an address.
     */
    protected void startIntentService(Location mLocation) {
        // Create an intent for passing to the intent service responsible for fetching the address.
        Intent intent = new Intent(this, FetchAddressIntentService.class);

        // Pass the result receiver as an extra to the service.
        intent.putExtra(AppUtils.LocationConstants.RECEIVER, mResultReceiver);

        // Pass the location data as an extra to the service.
        intent.putExtra(AppUtils.LocationConstants.LOCATION_DATA_EXTRA, mLocation);

        // Start the service. If the service isn't already running, it is instantiated and started
        // (creating a process for it if needed); if it is running then it remains running. The
        // service kills itself automatically once all intents are processed.
        startService(intent);
    }
    //END

    @Override
    protected void onStart() {
        super.onStart();
        try {
            mGoogleApiClient.connect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {

        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

}
