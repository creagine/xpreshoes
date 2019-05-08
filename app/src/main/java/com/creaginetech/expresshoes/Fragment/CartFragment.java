//package com.creaginetech.expresshoes.Fragment;
//
//import android.Manifest;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.pm.PackageManager;
//import android.graphics.Color;
//import android.location.Location;
//import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.design.widget.Snackbar;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.app.Fragment;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.support.v7.widget.helper.ItemTouchHelper;
//import android.text.TextUtils;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.CompoundButton;
//import android.widget.EditText;
//import android.widget.RadioButton;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
////import com.creaginetech.expresshoes.CartActivity;
//import com.creaginetech.expresshoes.Common.Common;
//import com.creaginetech.expresshoes.Database.Database;
//import com.creaginetech.expresshoes.Helper.RecyclerItemTouchHelper;
//import com.creaginetech.expresshoes.Interface.RecyclerItemTouchHelperListener;
//import com.creaginetech.expresshoes.Model.MyResponse;
//import com.creaginetech.expresshoes.Model.Notification;
//import com.creaginetech.expresshoes.Model.Order;
//import com.creaginetech.expresshoes.Model.Request;
//import com.creaginetech.expresshoes.Model.Sender;
//import com.creaginetech.expresshoes.Model.Token;
//import com.creaginetech.expresshoes.R;
//import com.creaginetech.expresshoes.Remote.APIService;
//import com.creaginetech.expresshoes.ViewHolder.CartAdapter;
//import com.creaginetech.expresshoes.ViewHolder.CartViewHolder;
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.GooglePlayServicesUtil;
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.common.api.Status;
//import com.google.android.gms.location.LocationListener;
//import com.google.android.gms.location.LocationRequest;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.location.places.Place;
//import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
//import com.google.android.gms.location.places.ui.PlaceSelectionListener;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.Query;
//import com.google.firebase.database.ValueEventListener;
//import com.rengwuxian.materialedittext.MaterialEditText;
//
//import java.text.NumberFormat;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Locale;
//
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
//
//public class CartFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
//        GoogleApiClient.OnConnectionFailedListener,LocationListener, RecyclerItemTouchHelperListener {
//
//    RecyclerView recyclerView;
//    RecyclerView.LayoutManager layoutManager;
//
//    FirebaseDatabase database;
//    DatabaseReference requests;
//
//    public TextView txtTotalPrice, txtTotalItems; //public agar bisa dipanggil di cartAdapter
//    Button btnPlace;
//
//    String address;
//
//    List<Order> cart = new ArrayList<>();
//
//    CartAdapter adapter;
//
//    APIService mService;
//
//    Place shippingAddress;
//
//    RelativeLayout rootLayout;
//
//    //Location-cp 36
//    private LocationRequest mLocationRequest;
//    private GoogleApiClient mGoogleApiClient;
//    private Location mLastLocation;
//
//    private static final int UPDATE_INTERVAL = 5000;
//    private static final int FATEST_INTERVAL = 3000;
//    private static final int DISPLACEMENT = 10;
//
//
//    private static final int LOCATION_REQUEST_CODE = 9999;
//    private static final int PLAY_SERVICES_REQUEST = 9997;
//
//
//    public CartFragment() {
//        // Required empty public constructor
//    }
//
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        View view = inflater.inflate(R.layout.fragment_cart, container, false);
//
//        //Runtime permission - cp 36
//        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
//        {
//            ActivityCompat.requestPermissions(getActivity()  ,new String[]
//                    {
//                            Manifest.permission.ACCESS_COARSE_LOCATION,
//                            Manifest.permission.ACCESS_FINE_LOCATION
//                    },LOCATION_REQUEST_CODE);
//
//        }
//        else
//        {
//            if (checkPlayServices()) //if have play service on device
//            {
//                buildGoogleApiClient();
//                createLocationRequest();
//            }
//        }
//
//        //Init Service
//        mService = Common.getFCMService();
//
//        rootLayout = (RelativeLayout)view.findViewById(R.id.rootLayout);
//
//        //Firebase
//        database = FirebaseDatabase.getInstance();
//        requests=database.getReference("Restaurants").child(Common.restaurantSelected).child("Requests");
//
//        //Init
//        recyclerView = (RecyclerView)view.findViewById(R.id.listCart);
//        recyclerView.setHasFixedSize(true);
//        layoutManager = new LinearLayoutManager(getActivity());
//        recyclerView.setLayoutManager(layoutManager);
//
//        //Swipe to delete
//        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0,ItemTouchHelper.LEFT,this);
//        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
//
//        txtTotalPrice = (TextView)view.findViewById(R.id.total);
//        txtTotalItems = view.findViewById(R.id.totalItemsCart);
//        btnPlace = (Button)view.findViewById(R.id.btnPlaceOrder);
//
//        btnPlace.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                if (cart.size() > 0)
//                    showAlertDialog();
//                else
//                    Toast.makeText(getActivity(), "Your cart is empty !", Toast.LENGTH_SHORT).show();
//
//            }
//        });
//
//        loadListFood();
//
//        return view;
//    }
//
//    private void createLocationRequest() {
//        mLocationRequest = new LocationRequest();
//        mLocationRequest.setInterval(UPDATE_INTERVAL);
//        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
//    }
//
//    private synchronized void buildGoogleApiClient() {
//
//        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(LocationServices.API).build();
//
//        mGoogleApiClient.connect();
//    }
//
//    private boolean checkPlayServices() {
//        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
//        if (resultCode != ConnectionResult.SUCCESS)
//        {
//            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
//                GooglePlayServicesUtil.getErrorDialog(resultCode,getActivity(),PLAY_SERVICES_REQUEST).show();
//            else
//            {
//                Toast.makeText(getActivity(), "This device is not supported", Toast.LENGTH_SHORT).show();
//            }
//            return false;
//        }
//        return true;
//    }
//
//    private void showAlertDialog() {
//        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
//        alertDialog.setTitle("One more step!");
//        alertDialog.setMessage("Enter your address: ");
//
//
//        LayoutInflater inflater = this.getLayoutInflater();
//        View order_address_comment = inflater.inflate(R.layout.order_address_comment,null);
//
////        final MaterialEditText edtAddress = (MaterialEditText)order_address_comment.findViewById(R.id.edtAddress);
//        final PlaceAutocompleteFragment edtAddress = (PlaceAutocompleteFragment)getActivity().getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
//        //Hide search icon before fragment
//        edtAddress.getView().findViewById(R.id.place_autocomplete_search_button).setVisibility(View.GONE);
//        //set hint for autocomplete edit text
//        ((EditText)edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
//                .setHint("Enter your address");
//        //set text size
//        ((EditText)edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
//                .setTextSize(14);
//
//        //Get Address from place autocomplete
//        edtAddress.setOnPlaceSelectedListener(new PlaceSelectionListener() {
//            @Override
//            public void onPlaceSelected(Place place) {
//                shippingAddress=place;
//            }
//
//            @Override
//            public void onError(Status status) {
//                Log.e("ERROR",status.getStatusMessage());
//
//            }
//        });
//
//        final MaterialEditText edtComment = (MaterialEditText)order_address_comment.findViewById(R.id.edtComment);
//
//        //Radio
//        final RadioButton radioHomeAddress = (RadioButton) order_address_comment.findViewById(R.id.radioHomeAddress);
//
//        //Event Radio
//        radioHomeAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked)
//                {
//                    if (Common.currentUser.getHomeAddress() != null ||
//                            !TextUtils.isEmpty(Common.currentUser.getHomeAddress()))
//                    {
//                        address = Common.currentUser.getHomeAddress();
//                        ((EditText) edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
//                                .setText(address);
//                    }
//                    else
//                    {
//                        Toast.makeText(getActivity(), "Please update your Home Address", Toast.LENGTH_SHORT).show();
//                    }
//                }
//
//            }
//        });
//
//        alertDialog.setView(order_address_comment);
//        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);
//
//        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                //Create new request
//                Request request = new Request(
//                        Common.currentUser.getPhone(),
//                        Common.currentUser.getName(),
//                        shippingAddress.getAddress().toString(),
//                        txtTotalPrice.getText().toString(),
//                        "0", // status
//                        edtComment.getText().toString(),
//                        String.format("%s,%s",shippingAddress.getLatLng().latitude,shippingAddress.getLatLng().longitude),
//                        Common.restaurantSelected,
//                        cart
//                );
//
//                //Submit to Firebase
//                //We will using System.CurrentMilli to key
//                String order_number = String.valueOf(System.currentTimeMillis());
//                requests.child(order_number)
//                        .setValue(request);
//                //Delete Cart
//                new Database(getActivity().getBaseContext()).cleanCart(Common.currentUser.getPhone());
//
//                sendNotificationOrder(order_number);
//
////                Toast.makeText(CartActivity.this, "Thank you , Order Place", Toast.LENGTH_SHORT).show();
////                finish();
//
//
//                //Remove fragment agar tidak crash saat order lagi
//                getFragmentManager().beginTransaction()
//                        .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
//                        .commit();
//
//
//            }
//        });
//
//        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                dialogInterface.dismiss();
//
//                //Remove fragment agar tidak crash saat order lagi
//                getFragmentManager().beginTransaction()
//                        .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
//                        .commit();
//
//
//            }
//        });
//
//        alertDialog.show();
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode)
//        {
//            case LOCATION_REQUEST_CODE:
//            {
//                if (grantResults.length >0&& grantResults[0] == PackageManager.PERMISSION_GRANTED)
//                {
//                    if (checkPlayServices()) //if have play service on device
//                    {
//                        buildGoogleApiClient();
//                        createLocationRequest();
//                    }
//                }
//            }
//        }
//    }
//
//    private void sendNotificationOrder(final String order_number) {
//        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
//        Query data = tokens.orderByChild("serverToken").equalTo(true); //get all node with isServerToken is true
//        data.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot postSnapshot:dataSnapshot.getChildren())
//                {
//                    Token serverToken = postSnapshot.getValue(Token.class);
//
//                    //Create raw payload to send
//                    Notification notification = new Notification("Expreshoes","You have new order "+order_number);
//                    Sender content = new Sender(serverToken.getToken(),notification);
//
//                    mService.sendNotification(content)
//                            .enqueue(new Callback<MyResponse>() {
//                                @Override
//                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
//                                    //only run when get result
//                                    if (response.code() == 200) {
//                                        if (response.body().success == 1) {
//                                            Toast.makeText(getActivity(), "Thank you , Order Place", Toast.LENGTH_LONG).show();
//                                        } else {
//                                            Toast.makeText(getActivity(), "Failed !!!", Toast.LENGTH_SHORT).show();
//                                        }
//                                    }
//                                }
//                                @Override
//                                public void onFailure(Call<MyResponse> call, Throwable t) {
//                                    Log.e("ERROR",t.getMessage());
//                                }
//                            });
//
//                }
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }
//
//    private void loadListFood() {
//        cart = new Database(getActivity()).getCarts(Common.currentUser.getPhone());
//        adapter = new CartAdapter(cart,this);
//        adapter.notifyDataSetChanged(); //Cp 15
//        recyclerView.setAdapter(adapter);
//
//        //Calculate total price
//        int total = 0;
//        for (Order order:cart)
//            total+=(Integer.parseInt(order.getPrice()))*(Integer.parseInt(order.getQuantity()));
//        Locale locale = new Locale("en","US");
//        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
//
//        int totalitems = 0;
//        for (Order order:cart)
//            totalitems +=((Integer.parseInt(order.getQuantity())));
//
//        txtTotalItems.setText(String.valueOf(totalitems));
//        txtTotalPrice.setText(fmt.format(total));
//    }
//
//    @Override
//    public boolean onContextItemSelected(MenuItem item) {
//        if(item.getTitle().equals(Common.DELETE))
//            deleteCart(item.getOrder());
//        return true;
//    } // CP 15 Delete cart
//
//    private void deleteCart(int position) {
//        //WE will remove item at List<Order> by position
//        cart.remove(position);
//        //After that, we will delete all old data from SQLite
//        new Database(getActivity()).cleanCart(Common.currentUser.getPhone());
//        //And fin  al, we will update new data from List<Order> to SQLite
//        for (Order item:cart)
//            new Database(getActivity()).addToCart(item);
//        //Refresh
//        loadListFood();
//    }
//
//    //cp 36
//    @Override
//    public void onConnected(@Nullable Bundle bundle) {
//        displayLocation();
//        startLocationUpdates();
//    }
//
//    private void startLocationUpdates() {
//        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
//        {
//            return;
//        }
//        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
//    }
//
//    private void displayLocation() {
//        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//        if (mLastLocation != null)
//        {
//            Log.d("LOCATION","Your location : "+mLastLocation.getLatitude()+","+mLastLocation.getLongitude());
//        }
//        else
//        {
//            Log.d("LOCATION","Could not get your location");
//        }
//    }
//
//    @Override
//    public void onConnectionSuspended(int i) {
//        mGoogleApiClient.connect();
//    }
//
//    @Override
//    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//
//    }
//
//    @Override
//    public void onLocationChanged(Location location) {
//        mLastLocation = location;
//        displayLocation();
//    }
//
//    @Override
//    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
//        if (viewHolder instanceof CartViewHolder)
//        {
//            String name = ((CartAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition()).getProductName();
//
//            final Order deleteItem = ((CartAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition());
//            final int deleteIndex = viewHolder.getAdapterPosition();
//
//            adapter.removeItem(deleteIndex);
//            new Database(getActivity().getBaseContext()).removeFromCart(deleteItem.getProductId(),Common.currentUser.getPhone());
//
//            //Update tctTotal di cart
//            //Calculate total price
//            int total = 0;
//            List<Order> orders = new Database(getActivity().getBaseContext()).getCarts(Common.currentUser.getPhone());
//
//            for (Order itemCart : orders)
//                total+=(Integer.parseInt(itemCart.getPrice()))*(Integer.parseInt(itemCart.getQuantity()));
//            Locale locale = new Locale("en","US");
//            NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
//
//            int totalitems = 0;
//            for (Order order:cart)
//                totalitems +=((Integer.parseInt(order.getQuantity())));
//
//            txtTotalItems.setText(String.valueOf(totalitems));
//
//            txtTotalPrice.setText(fmt.format(total));
//
//            //Make Snackbar
//            Snackbar snackbar = Snackbar.make(rootLayout,name + "Removed from cart !",Snackbar.LENGTH_SHORT);
//            snackbar.setAction("UNDO", new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    adapter.restoreItem(deleteItem,deleteIndex);
//                    new Database(getActivity().getBaseContext()).addToCart(deleteItem);
//
//                    //Update tctTotal di cart
//                    //Calculate total price
//                    int total = 0;
//                    List<Order> orders = new Database(getActivity().getBaseContext()).getCarts(Common.currentUser.getPhone());
//                    for (Order itemCart : orders)
//                        total+=(Integer.parseInt(itemCart.getPrice()))*(Integer.parseInt(itemCart.getQuantity()));
//                    Locale locale = new Locale("en","US");
//                    NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
//
//                    int totalitems = 0;
//                    for (Order order:cart)
//                        totalitems +=((Integer.parseInt(order.getQuantity())));
//
//                    txtTotalItems.setText(String.valueOf(totalitems));
//
//                    txtTotalPrice.setText(fmt.format(total));
//                }
//            });
//            snackbar.setActionTextColor(Color.YELLOW);
//            snackbar.show();
//        }
//    }
//
//}
