package codefellows_assignment.maptasks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MapView extends FragmentActivity implements OnMapReadyCallback, LocationListener {
    private int CURRENT_MAP_TYPE_INDEX = 1;
    final int REQUEST_PERMISSION_GRANT = 1;

    int[] MAP_TYPES = {
            GoogleMap.MAP_TYPE_NORMAL,
            GoogleMap.MAP_TYPE_HYBRID,
            GoogleMap.MAP_TYPE_NONE,
            GoogleMap.MAP_TYPE_SATELLITE,
            GoogleMap.MAP_TYPE_TERRAIN
    };

    private GoogleMap mMap;
    private LatLng mCurrentLocation;
    private LocationManager locationManager;
    private boolean isFollowing = true;

    int LOCATION_REFRESH_TIME = 1;
    int LOCATION_REFRESH_DISTANCE = 1;


    @Override
    public void onMapReady(GoogleMap googleMap){
        mMap = googleMap;
        loadPreferences();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);
        ButterKnife.bind(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final Intent data = getIntent();

        FirebaseDatabase.getInstance().getReference("tasks")
                .child(data.getStringExtra("id")).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Task task = Task.fromSnapshot(dataSnapshot);
                mMap.addMarker(new MarkerOptions().title("start").position(task.start));
                mMap.addMarker(new MarkerOptions().title("end").position(task.end));

                double centerLat = (task.start.latitude + task.end.latitude)/2;
                double centerLng = (task.end.longitude + task.start.longitude)/2;

                LatLng center = new LatLng(centerLat,centerLng);

                mMap.moveCamera(CameraUpdateFactory.newLatLng(center));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED){
            initializeLocationListener();
        }else{
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_PERMISSION_GRANT);
            };

        }



    @SuppressLint("MissingPermission")
    private void initializeLocationListener() {
    LocationListener listener = this;
    locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,LOCATION_REFRESH_TIME,LOCATION_REFRESH_DISTANCE,listener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if (requestCode == REQUEST_PERMISSION_GRANT && grantResults[0] == RESULT_OK &&
            requestCode == REQUEST_PERMISSION_GRANT && grantResults[1] == RESULT_OK){
            initializeLocationListener();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        if(mMap!=null) {
            savePreferences();
        }
    }

    public void savePreferences(){
        SharedPreferences prefs = getSharedPreferences(
                "assignment.panos_lab",
                Context.MODE_PRIVATE
        );
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("mapType", CURRENT_MAP_TYPE_INDEX);
        editor.putFloat("lat", (float) mMap.getCameraPosition().target.latitude);
        editor.putFloat("long", (float) mMap.getCameraPosition().target.longitude);
        editor.putFloat("zoom", (float) mMap.getCameraPosition().zoom);
        editor.commit();
    }

    public void loadPreferences(){
        SharedPreferences prefs = getSharedPreferences(
                "assignment.panos_lab",
                Context.MODE_PRIVATE
        );
        CURRENT_MAP_TYPE_INDEX = prefs.getInt("mapType",GoogleMap.MAP_TYPE_NORMAL);

        float lat = prefs.getFloat("lat",0);
        float longg = prefs.getFloat("long",0);
        float zoom = prefs.getFloat("zoom",1);

        setMapType();
        mMap.moveCamera(CameraUpdateFactory.zoomTo(zoom));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat,longg)));
    }

    @OnClick(R.id.goToMyLocation)
    public void goToMyLocation() {
        if (mCurrentLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(mCurrentLocation));
        }
    }

    public void setMapType(){
        mMap.setMapType(MAP_TYPES[CURRENT_MAP_TYPE_INDEX]);
    }

    public void setZoom(float zoom){
        mMap.moveCamera(CameraUpdateFactory.zoomTo(zoom));
    }

    @OnClick(R.id.zoomin)
    public void zoomIn(){
        setZoom(mMap.getCameraPosition().zoom+1);
    }

    @OnClick(R.id.zoomout)
    public void zoomOut(){
        setZoom(mMap.getCameraPosition().zoom-1);
    }

//    @OnClick(R.id.savePref)
//    public void saveButton(){
//        savePreferences();
//    }

    @OnClick(R.id.toggleFollow)
    public void toggleFollow(){
        Button flwBtn = findViewById(R.id.toggleFollow);
        if(isFollowing==true){
            isFollowing = false;
            flwBtn.setText("follow me");
        }else{
            isFollowing = true;
            flwBtn.setText("stop following me");
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        mCurrentLocation = latLng;
        if(isFollowing){
            mMap.moveCamera(CameraUpdateFactory.newLatLng(mCurrentLocation));
        }
    }

    @Override
    public void onProviderEnabled(String s){
        Log.d("GPS","gps activated");
    }

    @Override
    public void onProviderDisabled(String s) {
        Log.d("GPS", "gps deactivated");
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle){}

}
