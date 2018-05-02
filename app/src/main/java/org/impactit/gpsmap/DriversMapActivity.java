package org.impactit.gpsmap;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class DriversMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;
    private final int MY_LOCATION_REQUEST_CODE = 100;

    private Button logoutDriverButton;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference assignedCustomerRef, assignedCustomerPickupRef;
    private String driverID, customerID="";

    private Boolean currentLogoutDriverStatus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drivers_map);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        driverID = mAuth.getCurrentUser().getUid();

        logoutDriverButton = findViewById(R.id.driver_logout_btn);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        logoutDriverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                currentLogoutDriverStatus = true;
                DisconnectTheDriver();
                mAuth.signOut();
                LogoutDriver();
            }
        });

        getAssignedCustomerRequest();

    }

    private void getAssignedCustomerRequest() {
        assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("users")
                .child("drivers").child(driverID).child("CustomerRideID");

        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    customerID = dataSnapshot.getValue().toString();

                    getAssignedPickUpLocation();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getAssignedPickUpLocation() {
        assignedCustomerPickupRef = FirebaseDatabase.getInstance().getReference().child("customer request").child(customerID).child("l");

        assignedCustomerPickupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<Object> customerLocationMap = (List<Object>) dataSnapshot.getValue();
                    double LocationLat = 0;
                    double LocationLng = 0;

                    if (customerLocationMap.get(0) != null) {
                        LocationLat = Double.parseDouble(customerLocationMap.get(0).toString());
                    }
                    if (customerLocationMap.get(1) != null) {

                        LocationLng = Double.parseDouble(customerLocationMap.get(1).toString());
                    }

                    LatLng DriverLatLnd = new LatLng(LocationLat, LocationLng);
                    mMap.addMarker(new MarkerOptions().position(DriverLatLnd).title("pickup location"));

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void LogoutDriver() {

        Intent welcomeintent = new Intent(DriversMapActivity.this, MainActivity.class);
        welcomeintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(welcomeintent);
        finish();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            } else {
                checkLocationPermission();

            }
        }


       /* // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            //Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app need the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(DriversMapActivity.this,
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        1);
                            }
                        })
                        .create()
                        .show();
            } else {
                //No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_LOCATION_REQUEST_CODE);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_LOCATION_REQUEST_CODE: {
                //If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.

                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (googleApiClient == null) {
                            buildGoogleApiclient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (getApplicationContext() != null) {
            lastLocation = location;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12));

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

//        Data mdata = new Data(location.getLatitude(),location.getLongitude());

            DatabaseReference DriverAvailabilityRef = FirebaseDatabase.getInstance().getReference().child("Drivers Available");
            GeoFire geoFireAvailabilty = new GeoFire(DriverAvailabilityRef);

            DatabaseReference driverworkingREf = FirebaseDatabase.getInstance().getReference().child("drivers working");
            GeoFire geoFireWorking = new GeoFire(driverworkingREf);


            switch (customerID) {
                case "":
                    geoFireWorking.removeLocation(userId);
                    geoFireAvailabilty.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;
                default:
                    geoFireAvailabilty.removeLocation(userId);
                    geoFireWorking.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;

            }


//        DriverAvailabilityRef.setValue(mdata);
//        DatabaseReference mDatabase;
//        mDatabase = FirebaseDatabase.getInstance().getReference();
//        mDatabase.child("asd").push().setValue(mdata);


        }
    }

    protected synchronized void buildGoogleApiclient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (!currentLogoutDriverStatus) {
            DisconnectTheDriver();
        }

    }

    private void DisconnectTheDriver() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference DriverAvailabilityRef = FirebaseDatabase.getInstance().getReference().child("Drivers Available");

        GeoFire geoFire = new GeoFire(DriverAvailabilityRef);
        geoFire.removeLocation(userId);
    }

}
