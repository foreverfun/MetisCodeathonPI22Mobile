package com.example.metiscodeathonpi22;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MapsMarkerActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener {
    private GoogleMap mMap;
    private GoogleMap.OnMapLongClickListener mapLongClickListener;
    private GoogleMap.OnMapClickListener mapClickListener;
    private List<Marker> markerList = new ArrayList<>();
    private boolean isDrawingEnabled = false;
    private Polygon activePolygon;
    private List<LatLng> polygonPoints = new ArrayList<>();

    private List<Polygon> polygons = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            LatLng usdaDC = new LatLng(38.8867712, -77.0325009);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(usdaDC, 15f);
            mMap.animateCamera(cameraUpdate);

            String address = convertLatLngToAddress(usdaDC);
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(usdaDC)
                    .title("US Department of Agriculture")
                    .snippet(address);

            Marker marker = googleMap.addMarker(markerOptions);
            markerList.add(marker);
            marker.showInfoWindow();

            mMap.setOnMarkerClickListener(this);
            mMap.setOnMapLongClickListener(this);
            mMap.setOnMapClickListener(mapClickListener);

    }

    //Drawing
    @Override
    public void onMapClick(LatLng latLng) {
        if (isDrawingEnabled) {
            // Add the clicked location to the list of polygon points
            polygonPoints.add(latLng);

            // Update the active polygon on the map
            if (activePolygon != null) {
                activePolygon.remove();
            }

            // Create a PolygonOptions object with the updated points and properties
            PolygonOptions polygonOptions = new PolygonOptions()
                    .addAll(polygonPoints)
                    .strokeColor(Color.RED)
                    .fillColor(Color.BLUE)
                    .strokeWidth(5f);

            // Add the updated polygon to the map
            activePolygon = mMap.addPolygon(polygonOptions);
        }
    }
    public void onStartDrawingClick(View view) {
        isDrawingEnabled = true;
        polygonPoints.clear();
        // Enable map clicks
        mMap.setOnMapClickListener(this);

        // Show "Stop Drawing" button and hide "Start Drawing" button
        Button btnStartDrawing = findViewById(R.id.btnStartDrawing);
        Button btnStopDrawing = findViewById(R.id.btnStopDrawing);
        btnStartDrawing.setVisibility(View.GONE);
        btnStopDrawing.setVisibility(View.VISIBLE);
    }
    public void onStopDrawingClick(View view) {
        isDrawingEnabled = false;
        mMap.setOnMapClickListener(null);

        Polygon newPolygon = mMap.addPolygon(new PolygonOptions()
                .addAll(polygonPoints)
                .strokeColor(Color.RED)
                .fillColor(Color.BLUE)
                .strokeWidth(5f));

        polygons.add(newPolygon);

        Button btnStartDrawing = findViewById(R.id.btnStartDrawing);
        Button btnStopDrawing = findViewById(R.id.btnStopDrawing);
        btnStartDrawing.setVisibility(View.VISIBLE);
        btnStopDrawing.setVisibility(View.GONE);
    }

    // enter comment
    @Override
    public void onMapLongClick(LatLng latLng) {
        enterDescriptionDialog(latLng);
    }
    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        showLongToast(this,
                marker.getTitle() + "\n" + marker.getPosition(),
                6000 );
        return false;
    }
    private void enterDescriptionDialog(final LatLng latLng) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("\nEnter comment for the following position");
        builder.setMessage(convertLatLngToAddress(latLng) + ":");
        final EditText input = new EditText(this);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String titleValue = input.getText().toString().trim();
                dropMarker(latLng, titleValue);
                CameraUpdate cameraUpdate =
                        CameraUpdateFactory.newLatLngZoom(latLng, 15f);
                mMap.animateCamera(cameraUpdate);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
    private String convertLatLngToAddress(LatLng latLng) {
        try {
            Geocoder geocoder = new Geocoder(this);
            List<Address> addresses = null;
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            StringBuilder sb = new StringBuilder();
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i));
                }
                sb.deleteCharAt(sb.length() - 2);
            }
            return sb.toString();
        }
        catch(Exception ex) {

        }
        return null;
    }
    private void dropMarker(LatLng latLng, String titleValue) {
            String address = convertLatLngToAddress(latLng);
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .draggable(true)
                    .title(titleValue)
                    .snippet(address);

            Marker marker = mMap.addMarker(markerOptions);
            marker.showInfoWindow();
            markerList.add(marker);
    }
    public static void showLongToast(Context context, String message, int durationInMillis) {
        final Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);

        toast.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
                public void run() {
                    toast.cancel();
                }
            }, durationInMillis);
    }

    //menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Button btnStartDrawing = findViewById(R.id.btnStartDrawing);
        Button btnStopDrawing = findViewById(R.id.btnStopDrawing);
        if (item.getItemId() == R.id.menu_marker_information) {
            showMarkerInformationDialogBox();
            return true;
        } else if (item.getItemId() == R.id.menu_search) {
            showSearchDialogBox(1);
            return true;
        } else if (item.getItemId() == R.id.menu_marker) {
            mMap.clear();
            markerList.clear();
            polygonPoints.clear();
            activePolygon = null;
            polygons.clear();
            btnStartDrawing.setVisibility(View.GONE);
            btnStopDrawing.setVisibility(View.GONE);
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            return true;
        } else if (item.getItemId() == R.id.menu_drawing) {
            mMap.clear();
            polygonPoints.clear();
            activePolygon = null;
            polygons.clear();
            markerList.clear();
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            btnStartDrawing.setVisibility(View.VISIBLE);
            btnStopDrawing.setVisibility(View.GONE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void showMarkerInformationDialogBox() {
        StringBuilder markerInfo = new StringBuilder("Marker Information:\n");
        if (markerList.isEmpty()) {markerInfo.append("none");}
        else {
            for (Marker marker : markerList) {
                String title = marker.getTitle();
                String snippet = marker.getSnippet();
                markerInfo
                        .append("Id: ")
                        .append(marker.getId() + "\n")
                        .append("Title: " + marker.getTitle() + "\n")
                        .append("Position: :" + marker.getSnippet() + "\n\n");
            }
         }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Marker Information");
        builder.setMessage(markerInfo.toString());

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showSearchDialogBox(int options) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Search");
        final EditText input = new EditText(this);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String searchValue = input.getText().toString().trim();
                moveCameraWithSearch(options, searchValue);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
    private void moveCameraWithSearch(int options, String searchValue) {
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = null;
            addresses = geocoder.getFromLocationName(searchValue, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                double latitude = address.getLatitude();
                double longitude = address.getLongitude();

                LatLng location = new LatLng(latitude, longitude);

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(location)
                        .zoom(15) // Adjust the zoom level as needed
                        .build();

                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            } else {
                // Handle the case when the address is not found for the given zip code
            }
        } catch (IOException e) {
            // Handle geocoding errors here
            e.printStackTrace();
        }
    }
}



