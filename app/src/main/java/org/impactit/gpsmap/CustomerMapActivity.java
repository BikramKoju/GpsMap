package org.impactit.gpsmap;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;

    private Button customerlogoutButton, callCabButton;


    private String customerId;
    private LatLng customerPickupLocation;
    private int radius = 1;
    private Boolean driverFound = false;
    private String driverFoundId;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    Marker driverMarker;
    private DatabaseReference customerDatabaseRef;
    private DatabaseReference driverAvailableRef;
    private DatabaseReference driverRef;
    private DatabaseReference driverLocationRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        customerDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Customers Request");
        driverAvailableRef = FirebaseDatabase.getInstance().getReference().child("Drivers Available");
        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("Drivers working");

        customerlogoutButton = findViewById(R.id.logout);
        callCabButton = findViewById(R.id.call_a_car);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        customerlogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                LogOutCustomer();

            }
        });

        callCabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GeoFire geoFire = new GeoFire(customerDatabaseRef);
                geoFire.setLocation(customerId, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()));


                customerPickupLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                mMap.addMarker(new MarkerOptions().position(customerPickupLocation).title("pickup customer from here"));
//                Toast.makeText(CustomerMapActivity.this, "called", Toast.LENGTH_SHORT).show();
                callCabButton.setText("Getting your driver...");
                getClosestDriverCab();
            }
        });
    }

    private void getClosestDriverCab() {
        GeoFire geoFire = new GeoFire(driverAvailableRef);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(customerPickupLocation.latitude, customerPickupLocation.longitude), radius);

        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound) {
                    driverFound = true;
                    driverFoundId = key;

                    driverRef = FirebaseDatabase.getInstance().getReference().child("users").child("drivers").child(driverFoundId);
                    HashMap driverMap = new HashMap();
                    driverMap.put("CustomerRideID", customerId);
                    driverRef.updateChildren(driverMap);

                    gettingDriverLocation();
                    callCabButton.setText("Looking for driver location");
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!driverFound) {
                    radius = radius + 1;
                    getClosestDriverCab();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void gettingDriverLocation() {
        driverLocationRef.child(driverFoundId).child("l")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            List<Object> driverLocationMap = (List<Object>) dataSnapshot.getValue();
                            double LocationLat = 0;
                            double LocationLng = 0;
                            callCabButton.setText("Driver Found");

                            if (driverLocationMap.get(0) != null) {
                                LocationLat = Double.parseDouble(driverLocationMap.get(0).toString());
                            }
                            if (driverLocationMap.get(1) != null) {

                                LocationLng = Double.parseDouble(driverLocationMap.get(1).toString());
                            }

                            LatLng DriverLatLnd = new LatLng(LocationLat, LocationLng);
                            if (driverMarker != null) {
                                driverMarker.remove();
                            }

                            Location location1 = new Location("");
                            location1.setLatitude(customerPickupLocation.latitude);
                            location1.setLongitude(customerPickupLocation.longitude);


                            Location location2 = new Location("");
                            location2.setLatitude(DriverLatLnd.latitude);
                            location2.setLongitude(DriverLatLnd.longitude);

                            float distance= location1.distanceTo(location2);
                            callCabButton.setText("driver found" + String.valueOf(distance));

                            driverMarker = mMap.addMarker(new MarkerOptions().position(DriverLatLnd).title("your driver is here"));


                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleApiclient();
        mMap.setMyLocationEnabled(true);
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

        lastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));


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
    }


    private void LogOutCustomer() {
        Intent welcomeIntent = new Intent(CustomerMapActivity.this, MainActivity.class);
        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(welcomeIntent);
        finish();


    }
}
