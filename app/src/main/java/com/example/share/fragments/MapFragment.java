package com.example.share.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.share.R;
import com.example.share.activities.AnnouncementDetailsActivity;
import com.example.share.databinding.FragmentMapBinding;
import com.example.share.models.Announcement;
import com.example.share.utilities.Constants;
import com.example.share.utilities.PreferenceManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.N)
public class MapFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    private FragmentMapBinding binding;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        binding.idSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = binding.idSearchView.getQuery().toString();
                List<Address> addressList = null;

                if (location != null || location.equals("")) {
                    Geocoder geocoder = new Geocoder(getContext());

                    try {
                        addressList = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (addressList.size() == 0) {
                        Toast.makeText(getContext(), "Local não encontrado", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    Address address = addressList.get(0);

                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
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

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
        preferenceManager = new PreferenceManager(getContext());
        database = FirebaseFirestore.getInstance();
        return binding.getRoot();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        @SuppressLint("MissingPermission") Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();

        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    Log.d("location", location.toString());
                    Log.d("latitude", "" + location.getLatitude());
                    Log.d("longitude", "" + location.getLongitude());

                    LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    MarkerOptions options = new MarkerOptions();
                    options.position(myLocation);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 20));
                } else {
                    Log.e("location error", "location was null...");
                }
            }
        });

        locationTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("location error", e.getLocalizedMessage());
            }
        });

        setMapPoints(mMap);
    }

    private void setMapPoints(GoogleMap mMap) {
        database.collection(Constants.KEY_COLLECTION_ANNOUNCEMENTS)
                .get()
                .addOnCompleteListener(task -> {
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    HashMap<Marker, Announcement> hashMap = new HashMap<>();

                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (currentUserId.equals(queryDocumentSnapshot.getString(Constants.KEY_USER_ID))) {
                                continue;
                            }

                            Announcement announcement = new Announcement();
                            announcement.setId(queryDocumentSnapshot.getId());
                            announcement.setTitle(queryDocumentSnapshot.getString(Constants.KEY_ANNOUNCEMENT_TITLE));
                            announcement.setDescription(queryDocumentSnapshot.getString(Constants.KEY_ANNOUNCEMENT_DESCRIPTION));
                            announcement.setImage(queryDocumentSnapshot.getString(Constants.KEY_ANNOUNCEMENT_IMAGE));
                            announcement.setType(queryDocumentSnapshot.getString(Constants.KEY_ANNOUNCEMENT_TYPE));
                            announcement.setNumberBathrooms(queryDocumentSnapshot.getLong(Constants.KEY_ANNOUNCEMENT_NUMBER_BATHROOMS).intValue());
                            announcement.setNumberBedrooms(queryDocumentSnapshot.getLong(Constants.KEY_ANNOUNCEMENT_NUMBER_BEDROOMS).intValue());
                            announcement.setNumberResidents(queryDocumentSnapshot.getLong(Constants.KEY_ANNOUNCEMENT_NUMBER_RESIDENTS).intValue());
                            announcement.setPrice(queryDocumentSnapshot.getDouble(Constants.KEY_ANNOUNCEMENT_PRICE));
                            announcement.setLatPoint(queryDocumentSnapshot.getDouble(Constants.KEY_ANNOUNCEMENT_LAT_POINT));
                            announcement.setLngPoint(queryDocumentSnapshot.getDouble(Constants.KEY_ANNOUNCEMENT_LNG_POINT));
                            announcement.setUserId(queryDocumentSnapshot.getString(Constants.KEY_USER_ID));

                            LatLng coodinates = new LatLng(announcement.getLatPoint(), announcement.getLngPoint());

                            MarkerOptions options = new MarkerOptions();
                            options.position(coodinates);
                            options.title(announcement.getTitle());

                            if (announcement.getType().equals("Casa")) {
                                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_point_marker_house));
                            } else {
                                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_point_marker_ap));
                            }

                            Marker marker = mMap.addMarker(options);
                            hashMap.put(marker, announcement);
                        }
                    }

                    mMap.setOnInfoWindowClickListener(marker -> {
                        Announcement announcement = hashMap.get(marker);

                        Intent intent = new Intent(getContext(), AnnouncementDetailsActivity.class);
                        intent.putExtra(Constants.KEY_ANNOUNCEMENT, announcement);
                        startActivity(intent);
                    });
                });
    }

    ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts
                            .RequestMultiplePermissions(), result -> {
                        Boolean fineLocationGranted = result.getOrDefault(
                                Manifest.permission.ACCESS_FINE_LOCATION, false);
                        Boolean coarseLocationGranted = result.getOrDefault(
                                Manifest.permission.ACCESS_COARSE_LOCATION, false);
                        if (!fineLocationGranted && !coarseLocationGranted) {
                            Toast.makeText(getContext(), "Permissão de localização recusada!", Toast.LENGTH_SHORT).show();
                        }
                    }
            );
}