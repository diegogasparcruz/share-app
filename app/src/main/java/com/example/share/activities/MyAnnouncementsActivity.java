package com.example.share.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.share.adapters.MyAnnouncementsAdapter;
import com.example.share.databinding.ActivityMyAnnouncementsBinding;
import com.example.share.listeners.AnnouncementListener;
import com.example.share.models.Announcement;
import com.example.share.utilities.Constants;
import com.example.share.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyAnnouncementsActivity extends BaseActivity implements AnnouncementListener {
    private ActivityMyAnnouncementsBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyAnnouncementsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();
        setListeners();
        getMyAnnouncements();
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.fabNewAdverts.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), AddNewAnnouncementActivity.class);
            startActivity(intent);
        });
    }

    private void getMyAnnouncements() {
        loading(true);

        database.collection(Constants.KEY_COLLECTION_ANNOUNCEMENTS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);

                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Announcement> announcements = new ArrayList<>();

                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (!currentUserId.equals(queryDocumentSnapshot.getString(Constants.KEY_USER_ID))) {
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
                            announcement.setStatus(queryDocumentSnapshot.getLong(Constants.KEY_ANNOUNCEMENT_STATUS).intValue());

                            announcements.add(announcement);
                        }
                        if (announcements.size() > 0) {
                            MyAnnouncementsAdapter myAnnouncementsAdapter = new MyAnnouncementsAdapter(announcements, this);
                            binding.myAnnouncementsRecyclerView.setAdapter(myAnnouncementsAdapter);
                            binding.myAnnouncementsRecyclerView.setVisibility(View.VISIBLE);
                        } else {
                            showErrorMessage();
                        }
                    } else {
                        showErrorMessage();
                    }
                });
    }

    private void showErrorMessage() {
        binding.textErrorMessage.setText(String.format("%s", "Nenhum anúncio encontrado!"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCardAnnouncementClicked(Announcement announcement) {
        Intent intent = new Intent(getApplicationContext(), AddNewAnnouncementActivity.class);
        intent.putExtra(Constants.KEY_EDIT_ANNOUNCEMENT, announcement);
        startActivity(intent);
        finish();
    }
}