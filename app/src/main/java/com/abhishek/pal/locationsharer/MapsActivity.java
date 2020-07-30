package com.abhishek.pal.locationsharer;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.UnknownHostException;

public class MapsActivity extends FragmentActivity implements LocationListener, OnMapReadyCallback {

    public final int mPeriodicTimeout = 5000;
    private GoogleMap mGoogleMap;
    private LocationManager mLocationManager;
    private UserLocationInfo mMyLocation;
    public UserLocationInfo[] mNeighLocation=new UserLocationInfo[10];//mNeighLocation2;
    private int mNumOfNeigh;
    private UserLocationInfo[] mMyNeighborInfo;
    private Runnable mClientThread;
    private Thread mThread;
    public Bitmap b;
    public BitmapDescriptor icon = null;
    String path;
    String url;
    Intent i;
    public int count=0;
    boolean doubleBackToExitPressedOnce = false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(String.valueOf(this), "In Oncreate");
        i = getIntent();
        String s = i.getStringExtra("name");
        Log.e(String.valueOf(this), s);
        url = i.getStringExtra("url");
        Log.e(String.valueOf(this), url + "00");
//        for (int z=0;z<10;z++)
//        {
//            mNeighLocation[z]=new UserLocationInfo();
//        }
//        if(url.equals(""))
//        {
//            Log.e(String.valueOf(this),"in If");
//            icon = BitmapDescriptorFactory.fromResource(R.drawable.download);
//        }
//        else
//        {
//            Log.e(String.valueOf(this),"in else");
//            path=i.getStringExtra("dir");
//            try {
//                File f=new File(path, "profile.jpg");
//                b = BitmapFactory.decodeStream(new FileInputStream(f));
//                //ImageView img=(ImageView)findViewById(R.id.imgPicker);
//                //img.setImageBitmap(b);
//            }
//            catch (FileNotFoundException e)
//            {
//                e.printStackTrace();
//            }
//
//            icon = BitmapDescriptorFactory.fromBitmap(b);
//
//        }
        Log.e(String.valueOf(this), "hi");
        mMyLocation = new UserLocationInfo(s);

        // Create The client thread object here, but don't spawn now
        try {
            mClientThread = new SocketListenerClientThread(this);
            //mThread = new Thread(mClientThread);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        //show error dialog if GoolglePlayServices not available
        if (!isGooglePlayServicesAvailable(this)) {
            Toast.makeText(this, "Sorry we could not connect to Google Play services Required to Display the map. Thank You. Bye ", Toast.LENGTH_SHORT).show();
            finish();
        }
        setContentView(R.layout.activity_maps);
        SupportMapFragment supportMapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.v("LSA", "Location Changed");


        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        mMyLocation.setLocation(latitude, longitude);

        // Google map not yet ready for use, hence don't proceed further
        // Next time when we will get the location-change notification, we will do the needful
        if (null == mGoogleMap) {
            return;
        }

        // Add marker of my own, before that remove the previous marker
        if (mMyLocation.getMarker() != null) {
            mMyLocation.getMarker().remove();
        }
        // Add the latest marker now
        LatLng myLocation = mMyLocation.getLocation();
        MarkerOptions m = new MarkerOptions();
        m.position(myLocation);
        if (!url.equals("")) {
            path = i.getStringExtra("dir");
            try {
                File f = new File(path, "profile.jpg");
                b = BitmapFactory.decodeStream(new FileInputStream(f));
                //ImageView img=(ImageView)findViewById(R.id.imgPicker);
                //img.setImageBitmap(b);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            //
            // b = getCroppedBitmap(b);
            m.icon(BitmapDescriptorFactory.fromBitmap(b));
        } else {
            m.icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon));
        }
        m.title(mMyLocation.getUserId());
        Marker marker = mGoogleMap.addMarker(m);
        marker.showInfoWindow();
        mMyLocation.setMarker(marker);
//        mMyLocation.setMarker(mGoogleMap.addMarker(new MarkerOptions().position(myLocation).showInfoWindows()
//                .title(mMyLocation.getUserId())
//                //.snippet("and snippet")
//                //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))));
//                    .icon(icon)));
        // On first time, move the camera - then onwards it is upto the user
        if (null == mThread) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
            mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(17));
        }

        // Now check if any of my neighbor are available, if so then mark their markers
        for (int c = 0; c <count; c++) {
            if (mNeighLocation[c] != null) {

                // Remove the previous marker (if any)
                if (mNeighLocation[c].getMarker() != null) {
                    // Remove the previous marker
                    mNeighLocation[c].getMarker().remove();
                }

                mNeighLocation[c].setMarker(mGoogleMap.addMarker(new MarkerOptions().position(mNeighLocation[c].getLocation())
                        .title(mNeighLocation[c].getUserId())
                        //.snippet("and snippet")
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon))));
                mNeighLocation[c].getMarker().showInfoWindow();
            }
        }
//            if (mNeighLocation2 != null) {
//
//                // Remove the previous marker (if any)
//                if (mNeighLocation2.getMarker() != null) {
//                    // Remove the previous marker
//                    mNeighLocation2.getMarker().remove();
//                }
//
//                mNeighLocation2.setMarker(mGoogleMap.addMarker(new MarkerOptions().position(mNeighLocation2.getLocation())
//                        .title(mNeighLocation2.getUserId())
//                        //.snippet("and snippet")
//                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon))));
//                mNeighLocation2.getMarker().showInfoWindow();
//            }


            //TextView locationTv = (TextView) findViewById(R.id.latlongLocation);
            //locationTv.setText("My Latitude:" + latitude + ", Longitude:" + longitude);
            //locationTv.setText("My Neighbor:" + mNeighLocation.getLocation().latitude + ", Longitude:" + mNeighLocation.getLocation().longitude);

            // Every thing ready, now thread should be created (if not already created)

            if (null == mThread) {
                mThread = new Thread(mClientThread);
                mThread.start();
            }
    }


    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //mMap = googleMap;
        Log.v("LSA", "On map ready");
        mGoogleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mGoogleMap.setMyLocationEnabled(true);
        //this.locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        //Criteria criteria = new Criteria();
        //String bestProvider = locationManager.getBestProvider(criteria, true);
        //Location location = locationManager.getLastKnownLocation(bestProvider);
        //if (location != null) {
        //    onLocationChanged(location);
        //}
        //this.locationManager.requestLocationUpdates(bestProvider, 10000, 0, this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v("LSA", "On start");
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = mLocationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = mLocationManager.getLastKnownLocation(bestProvider);
        if (location != null) {
            onLocationChanged(location);
        }
        mLocationManager.requestLocationUpdates(bestProvider, mPeriodicTimeout, 0, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v("LSA", "Cleanup done");
        mLocationManager.removeUpdates(this);
        // Shall we not stop the client thread ? How ?
        // For the time being compromising with the optimization aspect
    }

    // User defined function
    public boolean isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 2404).show();
            }
            return false;
        }
        return true;
    }

    public UserLocationInfo getMyLocationInfo() {
        return mMyLocation;
    }

    public void setNeighbourLocation(UserLocationInfo neighLocation[]) {
        for (int c = 0; c <count; c++) {
            if (null == mNeighLocation[c]) {
                mNeighLocation[c] = neighLocation[c];
            } else {
                mNeighLocation[c].setLocation(neighLocation[c].getLocation().latitude,
                        neighLocation[c].getLocation().longitude);
            }
//
//            if (null == mNeighLocation2) {
//                mNeighLocation2 = neighLocation2;
//            } else {
//                mNeighLocation2.setLocation(neighLocation2.getLocation().latitude,
//                        neighLocation2.getLocation().longitude);
//            }

        }
    }
//    public Bitmap getCroppedBitmap(Bitmap bitmap) {
//        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
//                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(output);
//
//        final int color = 0xff424242;
//        final Paint paint = new Paint();
//        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
//
//        paint.setAntiAlias(true);
//        canvas.drawARGB(0, 0, 0, 0);
//        paint.setColor(color);
//        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
//        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
//                bitmap.getWidth() / 2, paint);
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
//        canvas.drawBitmap(bitmap, rect, rect, paint);
//        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
//        //return _bmp;
//        return output;
//    }

//    @Override
//    protected void onPause()
//    {
//        super.onPause();
//        Log.d(String.valueOf(this),"In OnPause");
//    }


    //Latest addition
//
//    @Override
//    public void onBackPressed() {
//        if (doubleBackToExitPressedOnce) {
//            super.onBackPressed();
//            return;
//        }
//
//        this.doubleBackToExitPressedOnce = true;
//        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
//
//        new Handler().postDelayed(new Runnable() {
//
//            @Override
//            public void run() {
//                doubleBackToExitPressedOnce=false;
//            }
//        }, 2000);
//    }


    @Override
    public void onBackPressed()
    {
        moveTaskToBack(true);
    }


}





















//
//import android.app.Activity;
//import android.content.ContextWrapper;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.location.Criteria;
//import android.location.Location;
//import android.location.LocationListener;
//import android.location.LocationManager;
//import android.os.Build;
//import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.app.FragmentActivity;
//import android.util.Log;
//import android.widget.Toast;
//
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.GoogleApiAvailability;
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.location.LocationRequest;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.SupportMapFragment;
//import com.google.android.gms.maps.model.BitmapDescriptorFactory;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.Marker;
//import com.google.android.gms.maps.model.MarkerOptions;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.net.UnknownHostException;
//
////import com.google.android.gms.location.LocationRequest;
////import com.google.android.gms.location.LocationServices;
//
//public class MapsActivity extends FragmentActivity implements LocationListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
//        GoogleApiClient.OnConnectionFailedListener {
//
//    File directory;
//    private static final String TAG = MainActivity.class.getSimpleName();
//    private GoogleMap mMap;
//    private String url;
//    public final int mPeriodicTimeout = 5000;
//    private GoogleMap mGoogleMap;
//    private LocationManager mLocationManager;
//    private UserLocationInfo mMyLocation;
//    private UserLocationInfo mNeighLocation;
//    private int mNumOfNeigh;
//    private UserLocationInfo[] maMyNeighborInfo;
//    private Runnable mClientThread;
//    private Thread mThread;
//    protected LocationRequest mLocationRequest;
//    GoogleApiClient mGoogleApiClient;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        Intent i = getIntent();
//        String name = i.getStringExtra("name");
//        mMyLocation = new UserLocationInfo(name);
//
//        // Create The client thread object here, but don't spawn now
//        try {
//            mClientThread = new SocketListenerClientThread(this);
//            //mThread = new Thread(mClientThread);
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
//        //Intent i=getIntent();
//        //String t=i.getStringExtra("id");
//        directory = (File) i.getExtras().get("id");
//
//        //show error dialog if GoolglePlayServices not available
//        if (!isGooglePlayServicesAvailable(this)) {
//            Toast.makeText(this, "Sorry we could not connect to Google Play services Required to Display the map. Thank You. Bye ", Toast.LENGTH_SHORT).show();
//            finish();
//        }
//
//        setContentView(R.layout.activity_maps);
//        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
////        Intent intent = getIntent();
////        url = intent.getStringExtra("id");
//        SupportMapFragment supportMapFragment =
//                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
//        supportMapFragment.getMapAsync(this);
//    }
//
//
//    /**
//     * Manipulates the map once available.
//     * This callback is triggered when the map is ready to be used.
//     * This is where we can add markers or lines, add listeners or move the camera. In this case,
//     * we just add a marker near Sydney, Australia.
//     * If Google Play services is not installed on the device, the user will be prompted to install
//     * it inside the SupportMapFragment. This method will only be triggered once the user has
//     * installed Google Play services and returned to the app.
//     */
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
////        mMap = googleMap;
////
////        // Add a marker in Sydney and move the camera
////        LatLng sydney = new LatLng(-34, 151);
////        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
////        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
////
//        //mMap = googleMap;
//        Log.v("LSA", "On map ready");
//        mGoogleMap = googleMap;
//
//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                requestPermissions(new String[]
//                        {
//                                android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION,
//                                android.Manifest.permission.INTERNET
//                        }, 100);
//            }
//
//
//            return;
//        }
//        mGoogleMap.setMyLocationEnabled(true);
////        buildGoogleApiClient();
////
////        mGoogleApiClient.connect();
//
//
//        //this.locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//        //Criteria criteria = new Criteria();
//        //String bestProvider = locationManager.getBestProvider(criteria, true);
//        //Location location = locationManager.getLastKnownLocation(bestProvider);
//        //if (location != null) {
//        //    onLocationChanged(location);
//        //}
//        //this.locationManager.requestLocationUpdates(bestProvider, 10000, 0, this);
////        mGoogleMap.getCameraPosition();
////        mGoogleMap.getMaxZoomLevel();
//        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(, 17));
//
//    }
//
//
//
//    protected synchronized void buildGoogleApiClient() {
//        Toast.makeText(this, "buildGoogleApiClient", Toast.LENGTH_SHORT).show();
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addApi(LocationServices.API)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .build();
//        Toast.makeText(this, "hey there", Toast.LENGTH_SHORT).show();
//
//    }
//
//    @Override
//    public void onLocationChanged(Location location) {
//
//        Log.v("LSA", "Location Changed");
//
//        double latitude = location.getLatitude();
//        double longitude = location.getLongitude();
//        mMyLocation.setLocation(latitude, longitude);
//
//        // Google map not yet ready for use, hence don't proceed further
//        // Next time when we will get the location-change notification, we will do the needful
//        if (null == mGoogleMap) {
//            return;
//        }
//
//        // Add marker of my own, before that remove the previous marker
//        if (mMyLocation.getMarker() != null) {
//            mMyLocation.getMarker().remove();
//        }
////        Bitmap bm=null;
////        try {
////            bm= Glide.with(getApplicationContext()).load(url)
////                    .asBitmap()
////                    .into(100,100)
////                    .get();
////        } catch (InterruptedException e) {
////            e.printStackTrace();
////        } catch (ExecutionException e) {
////            e.printStackTrace();
////        }
//        //.into(imgProfilePic);
//                //.diskCacheStrategy(DiskCacheStrategy.ALL)
//
//        // Add the latest marker now
//
////        try {
////            URL url1 = new URL(url);
////            HttpURLConnection connection = (HttpURLConnection) url1.openConnection();
////            connection.setDoInput(true);
////            connection.connect();
////            InputStream input = connection.getInputStream();
////            Bitmap myBitmap = BitmapFactory.decodeStream(input);
////
////            String data1 = String.valueOf(String.format("/sdcard/%d.jpg", System.currentTimeMillis()));
////
////            FileOutputStream stream = new FileOutputStream(data1);
////
////            ByteArrayOutputStream outstream = new ByteArrayOutputStream();
////            myBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outstream);
////            byte[] byteArray = outstream.toByteArray();
////
////            stream.write(byteArray);
////            stream.close();
////
////            Toast.makeText(getApplicationContext(), "Downloading Completed", Toast.LENGTH_SHORT).show();
////        } catch (Exception e) {
////            e.printStackTrace();
////            Toast.makeText(getApplicationContext(), "ERROR!!!", Toast.LENGTH_SHORT).show();
////        }
//        Bitmap b=null;
//        ContextWrapper cw = new ContextWrapper(getApplicationContext());
//        // path to /data/data/yourapp/app_data/imageDir
//        //File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
//        String directory ="/storage/emulated/0/Marker";
//        try {
//            File f=new File(directory, "Ami.jpg");
//            b = BitmapFactory.decodeStream(new FileInputStream(f));
//            //ImageView img=(ImageView)findViewById(R.id.imgPicker);
//            //img.setImageBitmap(b);
//        }
//        catch (FileNotFoundException e)
//        {
//            e.printStackTrace();
//        }
//
//        LatLng myLocation = mMyLocation.getLocation();
//        mMyLocation.setMarker(mGoogleMap.addMarker(new MarkerOptions().position(myLocation)
//                .title(mMyLocation.getUserId())
//                //.snippet("and snippet")
//                //.icon(BitmapDescriptorFactory.fromBitmap(b))));
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))));
//        Log.v("LSA", "Marker Set");
//        // On first time, move the camera - then onwards it is upto the user
//        if (null == mThread) {
//            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation,17));
//            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UserLoc, 17));
//            mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
//        }
//
//        // Now check if any of my neighbor are available, if so then mark their markers
//        if (mNeighLocation != null) {
//
//            // Remove the previous marker (if any)
//            if (mNeighLocation.getMarker() != null) {
//                // Remove the previous marker
//                mNeighLocation.getMarker().remove();
//            }
//            mNeighLocation.setMarker(mGoogleMap.addMarker(new MarkerOptions().position(mNeighLocation.getLocation())
//                    .title(mNeighLocation.getUserId())
//                    //.snippet("and snippet")
//                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.download))));
//        }
//
//        //TextView locationTv = (TextView) findViewById(R.id.latlongLocation);
//        //locationTv.setText("My Latitude:" + latitude + ", Longitude:" + longitude);
//        //locationTv.setText("My Neighbor:" + mNeighLocation.getLocation().latitude + ", Longitude:" + mNeighLocation.getLocation().longitude);
//
//        // Every thing ready, now thread should be created (if not already created)
//        if (null == mThread) {
//            mThread = new Thread(mClientThread);
//            mThread.start();
//        }
//    }
//
//    @Override
//    public void onStatusChanged(String s, int i, Bundle bundle) {
//        Log.v("LSA", "Status Changed");
//    }
//
//    @Override
//    public void onProviderEnabled(String s) {
//        Log.v("LSA", "Provider Enabled");
//    }
//
//    @Override
//    public void onProviderDisabled(String s) {
//        Log.v("LSA", "Provider Disabled");
//        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        Log.v("LSA", "On start");
//        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//        Criteria criteria = new Criteria();
//        String bestProvider = mLocationManager.getBestProvider(criteria, true);
//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                requestPermissions(new String[]
//                        {
//                                android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION,
//                                android.Manifest.permission.INTERNET
//                        }, 100);
//            }
//
//            return;
//        }
//        //Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//
//        Location location = mLocationManager.getLastKnownLocation(bestProvider);
//        if (location != null) {
//            onLocationChanged(location);
//            //onConnected(Bundle.EMPTY);
//        }
////        if(mLastLocation!=null)
////        {
////            onLocationChanged(mLastLocation);
////        }
//        mLocationManager.requestLocationUpdates(bestProvider, mPeriodicTimeout, 0, this);
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        Log.v("LSA", "Cleanup done");
//        mLocationManager.removeUpdates(this);
//        // Shall we not stop the client thread ? How ?
//        // For the time being compromising with the optimization aspect
//    }
//
//    // User defined function
//    public boolean isGooglePlayServicesAvailable(Activity activity) {
//
//        Log.v("LSA", "Is Google Play Services Available");
//
//        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
//        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
//        if(status != ConnectionResult.SUCCESS) {
//            if(googleApiAvailability.isUserResolvableError(status)) {
//                googleApiAvailability.getErrorDialog(activity, status, 2404).show();
//            }
//            return false;
//        }
//        return true;
//    }
//
//    public UserLocationInfo getMyLocationInfo() {
//
//        Log.v("LSA", "getMyLocationInfo");
//
//        return mMyLocation;
//    }
//
//    public void setNeighbourLocation(UserLocationInfo neighLocation) {
//
//        Log.v("LSA", "Set NeighbourLocation");
//
//        if(null == mNeighLocation) {
//            mNeighLocation = neighLocation;
//        }
//        else {
//            mNeighLocation.setLocation(neighLocation.getLocation().latitude,
//                    neighLocation.getLocation().longitude);
//        }
//    }
//
//
//    @Override
//    public void onConnected(@Nullable Bundle bundle) {
//        Log.v("LSA", "onConnected");
//
//        Toast.makeText(this, "onConnected", Toast.LENGTH_SHORT).show();
//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                requestPermissions(new String[]
//                        {
//                                android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION,
//                                android.Manifest.permission.INTERNET
//                        }, 100);
//            }
//            return;
//        }
//        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//        LatLng latLng;
//        if (mLastLocation != null) {
//            //place marker at current position
//            //mGoogleMap.clear();
//            latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
//            MarkerOptions markerOptions = new MarkerOptions();
//            markerOptions.position(latLng);
//            markerOptions.title("Current Position");
//            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
//            Marker currLocationMarker = mGoogleMap.addMarker(markerOptions);
//        }
//        mLocationRequest = new LocationRequest();
//        mLocationRequest.setInterval(5000); //5 seconds
//        mLocationRequest.setFastestInterval(3000); //3 seconds
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
//        //mLocationRequest.setSmallestDisplacement(0.1F); //1/10 meter
//
//        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,
//                (com.google.android.gms.location.LocationListener) this);
//
//    }
//
//    @Override
//    public void onConnectionSuspended(int i) {
//        Log.v("LSA", "onConnectionSuspended");
//
//
//        Toast.makeText(this,"onConnectionSuspended",Toast.LENGTH_SHORT).show();
//        // The connection to Google Play services was lost for some reason. We call connect() to
//        // attempt to re-establish the connection.
//        Log.i(TAG, "Connection suspended");
//        //mGoogleApiClient.connect();
//    }
//
//    @Override
//    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//        Log.v("LSA", "onConnectionFailed");
//
//        Toast.makeText(this,"onConnectionFailed",Toast.LENGTH_SHORT).show();
//        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
//        // onConnectionFailed.
//        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
//
//    }
//
//}
