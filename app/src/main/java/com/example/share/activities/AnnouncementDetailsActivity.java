package com.example.share.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.share.R;
import com.example.share.databinding.ActivityAnnouncementDetailsBinding;
import com.example.share.models.Announcement;
import com.example.share.models.User;
import com.example.share.utilities.Constants;
import com.example.share.utilities.PreferenceManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnouncementDetailsActivity extends BaseActivity implements OnMapReadyCallback {
    private ActivityAnnouncementDetailsBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private Announcement announcement;
    private User user;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAnnouncementDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();
        announcement = (Announcement) getIntent().getSerializableExtra(Constants.KEY_ANNOUNCEMENT);
        loadUser();
        loadBookmark();
        setListerners();
    }

    private void setListerners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());

        binding.imageAnnouncement.setImageBitmap(getImage(announcement.getImage()));
        binding.txtTitle.setText(announcement.getTitle());
        binding.txtMoney.setText(announcement.getPrice() + "");
        binding.txtNumberBedrooms.setText(announcement.getNumberBedrooms() + "");
        binding.txtNumberBathrooms.setText(announcement.getNumberBathrooms() + "");
        binding.txtNumberResidents.setText(announcement.getNumberResidents() + "");
        binding.txtDescription.setText(announcement.getDescription());

        binding.imageBookmark.setOnClickListener(v -> {
            HashMap<String, Object> bookmark = new HashMap<>();
            bookmark.put("userId", preferenceManager.getString(Constants.KEY_USER_ID));
            bookmark.put("announcementId", announcement.getId());
            bookmark.put("status", announcement.getStatus());

            database.collection("bookmarks")
                    .add(bookmark)
                    .addOnSuccessListener(documentReference -> {
                        binding.imageBookmark.setVisibility(View.GONE);
                        binding.imageBookmarkSelected.setVisibility(View.VISIBLE);
                    })
                    .addOnFailureListener(exception -> {
                        showToast(exception.getMessage());
                        binding.imageBookmarkSelected.setVisibility(View.GONE);
                        binding.imageBookmark.setVisibility(View.VISIBLE);
                    });
        });

        binding.imageBookmarkSelected.setOnClickListener(v -> {
            database.collection("bookmarks")
                    .whereEqualTo("userId", preferenceManager.getString(Constants.KEY_USER_ID))
                    .whereEqualTo("announcementId", announcement.getId())
                    .get()
                    .addOnCompleteListener(document -> {
                        if(document.isSuccessful()){
                            for(QueryDocumentSnapshot documentSnapshot : document.getResult()){
                                database.collection("bookmarks")
                                        .document(documentSnapshot.getId())
                                        .delete()
                                        .addOnSuccessListener(task -> {
                                            binding.imageBookmarkSelected.setVisibility(View.GONE);
                                            binding.imageBookmark.setVisibility(View.VISIBLE);
                                        })
                                        .addOnFailureListener(exception -> {
                                            showToast(exception.getMessage());
                                            binding.imageBookmark.setVisibility(View.GONE);
                                            binding.imageBookmarkSelected.setVisibility(View.VISIBLE);
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(exception -> {
                        showToast(exception.getMessage());
                        binding.imageBookmark.setVisibility(View.GONE);
                        binding.imageBookmarkSelected.setVisibility(View.VISIBLE);
                    });
        });
    }

    private void loadUser() {
        loading(true);

        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(announcement.getUserId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                loading(false);
                                user = new User();
                                user.setId(document.getId());
                                user.setName(document.getString(Constants.KEY_NAME));
                                user.setEmail(document.getString(Constants.KEY_EMAIL));
                                user.setImage(document.getString(Constants.KEY_IMAGE));

                                binding.txtNameUser.setText(user.getName());
                                binding.imageUser.setImageBitmap(getImage(user.getImage()));

                                binding.imageChat.setOnClickListener(v -> {
                                    Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                                    intent.putExtra(Constants.KEY_USER, user);
                                    startActivity(intent);
                                });
                            } else {
                                loading(false);
                                showToast("Usuário não encontrado!");
                            }
                        } else {
                            loading(false);
                            showToast(task.getException() + "");
                        }
                    }
                });
    }

    private void loadBookmark() {
        database.collection("bookmarks")
                .whereEqualTo("userId", preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo("announcementId", announcement.getId())
                .get()
                .addOnCompleteListener(document -> {
                    if(document.isSuccessful() && document.getResult() != null){
                        for(QueryDocumentSnapshot documentSnapshot : document.getResult()){
                            binding.imageBookmark.setVisibility(View.GONE);
                            binding.imageBookmarkSelected.setVisibility(View.VISIBLE);
                        }
                    } else {
                        binding.imageBookmark.setVisibility(View.VISIBLE);
                        binding.imageBookmarkSelected.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(exception -> {
                    showToast(exception.getMessage());
                    binding.imageBookmark.setVisibility(View.VISIBLE);
                    binding.imageBookmarkSelected.setVisibility(View.GONE);
                });
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.layoutUser.setVisibility(View.INVISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.layoutUser.setVisibility(View.VISIBLE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private Bitmap getImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng coodinates = new LatLng(announcement.getLatPoint(), announcement.getLngPoint());

        MarkerOptions options = new MarkerOptions();
        options.position(coodinates);
        options.title(announcement.getTitle());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coodinates, 20));

        if(announcement.getType().equals("Casa")) {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_point_marker_house));
        } else {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_point_marker_ap));
        }

        mMap.addMarker(options);
    }
}