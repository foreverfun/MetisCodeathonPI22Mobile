// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.mapwithmarker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * An activity that displays a Google map with a marker (pin) to indicate a particular location.
 */
// [START maps_marker_on_map_ready]
public class MapsMarkerActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapClickListener
        {

    // [START_EXCLUDE]
    GoogleMap googleMap;
    private GoogleMap mMap;
    private ArrayList<Marker> arrMarkerList;
    private ArrayList<Polyline> arrPolylineList;
    private List<Marker> markerList = new ArrayList<>();

    // [START maps_marker_get_map_async]
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);

        // Get the SupportMapFragment and request notification when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    // [END maps_marker_get_map_async]
    // [END_EXCLUDE]

    // [START_EXCLUDE silent]
    /**
     * Manipulates the map when it's available.
     * The API invokes this callback when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user receives a prompt to install
     * Play services inside the SupportMapFragment. The API invokes this method after the user has
     * installed Google Play services and returned to the app.
     */
    // [END_EXCLUDE]
    // [START maps_marker_on_map_ready_add_marker]
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // [START_EXCLUDE silent]
        // Add a marker in Sydney, Australia,
        // and move the map's camera to the same location.
        // [END_EXCLUDE]
        LatLng sydney = new LatLng(-33.852, 151.211);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        MarkerOptions markerOptions = new MarkerOptions()
                .position(sydney)
                .title("Marker Title")
                .snippet("Marker Description");

        Marker marker = googleMap.addMarker(markerOptions);

        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnMapClickListener(this);

        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                showCommentDialog(latLng);
            }
        });
    }

    private void showCommentDialog(final LatLng latLng) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Comment");

        // Set up the input
        final EditText input = new EditText(this);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String comment = input.getText().toString().trim();
                dropMarker(latLng, comment);
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

    private void dropMarker(LatLng latLng, String comment) {
        // Add a marker to the map
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title("Marker Title")
                .snippet(comment);

        Marker marker = googleMap.addMarker(markerOptions);
        markerList.add(marker);
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {

        Toast.makeText(this,
            "My Position: " + marker.getPosition() +
                    "\nComment: " + marker.getTitle(),
            Toast.LENGTH_LONG)
                .show();
        return false;
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {

        Toast.makeText(this,
                        "My Position: " + latLng,
                        Toast.LENGTH_LONG)
                .show();
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title("Marker Title")
                .snippet("Marker Description");

        Marker marker = mMap.addMarker(markerOptions);
        marker.showInfoWindow();


    }
    // [END maps_marker_on_map_ready_add_marker]
}
// [END maps_marker_on_map_ready]
