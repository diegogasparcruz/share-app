package com.example.share.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.share.R;
import com.example.share.databinding.ActivityAddLocationNewAnnouncementBinding;
import com.example.share.models.Announcement;
import com.example.share.utilities.Constants;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.List;

public class AddLocationNewAnnouncementActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private ActivityAddLocationNewAnnouncementBinding binding;
    private Announcement announcement;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddLocationNewAnnouncementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        binding.idSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = binding.idSearchView.getQuery().toString();
                List<Address> addressList = null;

                if (location != null || location.equals("")) {
                    Geocoder geocoder = new Geocoder(getApplicationContext());

                    try {
                        addressList = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if(addressList.size() == 0) {
                        mMap.clear();
                        Toast.makeText(getApplicationContext(), "Local não encontrado", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    Address address = addressList.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(latLng).title(location));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        mapFragment.getMapAsync(this);
        announcement = (Announcement) getIntent().getSerializableExtra(Constants.KEY_ANNOUNCEMENT);
        database = FirebaseFirestore.getInstance();
        setListeners();
    }

    private void setListeners() {
        binding.buttonFinish.setOnClickListener(v -> saveAnnouncement());
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonFinish.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonFinish.setVisibility(View.VISIBLE);
        }
    }

    private void saveAnnouncement() {
        loading(true);

        if(announcement.getId() != null) {
            database.collection(Constants.KEY_COLLECTION_ANNOUNCEMENTS)
                    .document(announcement.getId())
                    .set(announcement)
                    .addOnSuccessListener(documentReference -> {
                        loading(false);
                        Intent intent = new Intent(this, MyAnnouncementsActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(exception -> {
                        loading(false);
                        showToast(exception.getMessage());
                    });
        } else {
            database.collection(Constants.KEY_COLLECTION_ANNOUNCEMENTS)
                    .add(announcement)
                    .addOnSuccessListener(documentReference -> {
                        loading(false);
                        Intent intent = new Intent(this, MyAnnouncementsActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(exception -> {
                        loading(false);
                        showToast(exception.getMessage());
                    });
        }


    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        LatLng coodinates = new LatLng(announcement.getLatPoint(), announcement.getLngPoint());

        MarkerOptions options = new MarkerOptions();
        options.position(coodinates);

        mMap.addMarker(options);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coodinates, 20));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(point));

                announcement.setLatPoint(point.latitude);
                announcement.setLngPoint(point.longitude);
            }
        });
    }
}