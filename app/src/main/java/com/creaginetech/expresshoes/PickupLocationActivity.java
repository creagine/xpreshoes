package com.creaginetech.expresshoes;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.creaginetech.expresshoes.Common.Common;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

public class PickupLocationActivity extends AppCompatActivity {

    Button btnSelectMap, btnRecentLocation;
    String selectedLocationLatlng, locality;
    LatLng coordinate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickup_location);

        btnSelectMap = findViewById(R.id.buttonSelectFromMap);
        btnRecentLocation = findViewById(R.id.buttonRecentLocation);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                String lat = String.valueOf(place.getLatLng().latitude);
                String lng = String.valueOf(place.getLatLng().longitude);
                String latlng = lat+","+lng;

                Common.pickupLocationSelected = latlng;

                Common.pickupLocationNameSelected = place.getName().toString();

                Intent intent = new Intent(PickupLocationActivity.this, CartFixedActivity.class);
                startActivity(intent);
                finish();

            }

            @Override
            public void onError(Status status) {

            }
        });

        btnSelectMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(PickupLocationActivity.this, SelectFromMapActivity.class);
                startActivity(intent);
                finish();

            }
        });

        btnRecentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ActivityCompat.checkSelfPermission(PickupLocationActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(PickupLocationActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }


                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                Criteria criteria = new Criteria();
                String provider = locationManager.getBestProvider(criteria, true);
                Location location = locationManager.getLastKnownLocation(provider);

                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    coordinate = new LatLng(latitude, longitude);
                }

                Geocoder geocoder = new Geocoder(PickupLocationActivity.this);

                try {

                    List<Address> addressList = geocoder.getFromLocation(coordinate.latitude, coordinate.longitude, 1);
                    if (addressList != null && addressList.size() > 0) {
                        locality = addressList.get(0).getAddressLine(0);
//                        String country = addressList.get(0).getCountryName();
                        if (!locality.isEmpty()) {
                            selectedLocationLatlng = coordinate.toString();
                            //rapikan format jadi lat,lng
                            String lat = String.valueOf(coordinate.latitude);
                            String lng = String.valueOf(coordinate.longitude);
                            Common.pickupLocationSelected = lat+","+lng;
                            Common.pickupLocationNameSelected = locality;

                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent(PickupLocationActivity.this, CartFixedActivity.class);
                startActivity(intent);
                finish();

            }
        });

    }
}
