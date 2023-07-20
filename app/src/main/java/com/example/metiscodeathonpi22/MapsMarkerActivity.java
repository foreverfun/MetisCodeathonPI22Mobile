package com.example.metiscodeathonpi22;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
        GoogleMap.OnMarkerClickListener
{
    private GoogleMap mMap;
    private List<Marker> markerList = new ArrayList<>();
    private static final String POST_API_URL =
            "https://5ajph3kb1d.execute-api.us-east-2.amazonaws.com/createGeotag";
    private static final String GET_API_URL =
            "https://5ajph3kb1d.execute-api.us-east-2.amazonaws.com/getGeotag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        fetchDataFromApi();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        LatLng usdaDC = new LatLng(38.8867712,-77.0325009);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(usdaDC, 15f);
        googleMap.animateCamera(cameraUpdate);

        String strLatitude = Location.convert(usdaDC.latitude, Location.FORMAT_DEGREES);
        String strLongitude = Location.convert(usdaDC.longitude, Location.FORMAT_DEGREES);

        MarkerOptions markerOptions = new MarkerOptions()
                .position(usdaDC)
                .title("US Department of Agriculture")
                .snippet(strLatitude + " " + strLongitude);

        Marker marker = googleMap.addMarker(markerOptions);
        markerList.add(marker);
        marker.showInfoWindow();
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                enterDescriptionDialog(latLng);

            }
        });
    }

    private void enterDescriptionDialog(final LatLng latLng) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("\nEnter title for the following position");
        builder.setMessage(latLng.toString() + ":");
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

    private void dropMarker(LatLng latLng, String titleValue) {
        // Add a marker to the map
        String strLatitude = Location.convert(latLng.latitude, Location.FORMAT_DEGREES);
        String strLongtitude = Location.convert(latLng.longitude, Location.FORMAT_DEGREES);

        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(titleValue)
                .snippet(strLatitude + " " + strLongtitude);

        Marker marker = mMap.addMarker(markerOptions);
        marker.showInfoWindow();
        markerList.add(marker);
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Toast.makeText(this,
                marker.getTitle() + "\n" + marker.getPosition(),
                Toast.LENGTH_LONG+300)
                .show();
        return false;
    }

    private void fetchDataFromApi() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(GET_API_URL)
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();

                } else {
                    // Handle error if the request was not successful
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                // Handle the failure case (e.g., network error)
            }
        });
    }

    private void saveDataToApi() {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        String yourJsonRequestBody = "{\"Lat\":\"12.34\",\"Lng\":\"1234\",\"Comment\":\"test\"}";
        RequestBody requestBody = RequestBody.create(mediaType, yourJsonRequestBody);
        Request request = new Request.Builder()
                .url(POST_API_URL)
                .post(requestBody)
                .build();

        try {
            // Execute the API call synchronously
            Response response = client.newCall(request).execute();
            Toast.makeText(this, "here", Toast.LENGTH_LONG).show();
            if (response.isSuccessful())
            {
                String responseData = response.body().string();

            }
            else
            {
                Toast.makeText(this, "failed", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            // Handle network errors here
            e.printStackTrace();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_marker_information) {
            showMarkerInformationDialogBox();
            return true;
        } else if (item.getItemId() == R.id.menu_search_zip_code) {
            showSearchDialogBox(1);
            return true;
        } else if (item.getItemId() == R.id.menu_search_state) {
            showSearchDialogBox(2);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showMarkerInformationDialogBox() {
        StringBuilder markerInfo = new StringBuilder("Marker Information:\n");
        for (Marker marker : markerList) {
            String title = marker.getTitle();
            String snippet = marker.getSnippet();
            markerInfo
                    .append("Id: ")
                    .append(marker.getId() + "\n")
                    .append("Title: " + marker.getTitle() + "\n")
                    .append("Position: :" + marker.getSnippet() + "\n\n");
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
        if (options == 1)
        {
            builder.setTitle("Search by Zip Code: ");
        } else if (options == 2)
        {
            builder.setTitle("Search by State: ");
        }
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
            if (options == 1)
                addresses = geocoder.getFromLocationName(searchValue, 1);
            else if (options == 2)
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



