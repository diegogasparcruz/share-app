package com.example.share.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.share.adapters.BookmarksAdapter;
import com.example.share.databinding.ActivityBookmarksBinding;
import com.example.share.listeners.AnnouncementListener;
import com.example.share.models.Announcement;
import com.example.share.utilities.Constants;
import com.example.share.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BookmarksActivity extends BaseActivity implements AnnouncementListener {
    private ActivityBookmarksBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookmarksBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();
        setListeners();
        getMyBookmarks();
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    private void getMyBookmarks() {
        loading(true);
        List<Announcement> bookmarks = new ArrayList<>();
        database.collection("bookmarks")
                .whereEqualTo("userId", preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo("status", 1)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.getResult().size() == 0) {
                       loading(false);
                       showErrorMessage();
                       return;
                    }
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            if (documentSnapshot.getLong("status").intValue() == 0) {
                                return;
                            }
                            database.collection(Constants.KEY_COLLECTION_ANNOUNCEMENTS)
                                    .document(documentSnapshot.getString("announcementId"))
                                    .get()
                                    .addOnCompleteListener(doc -> {
                                        if (doc.isSuccessful() && doc.getResult() != null) {
                                            Announcement announcement = new Announcement();
                                            announcement.setId(doc.getResult().getId());
                                            announcement.setTitle(doc.getResult().getString("title"));
                                            announcement.setDescription(doc.getResult().getString("description"));
                                            announcement.setImage(doc.getResult().getString("image"));
                                            announcement.setType(doc.getResult().getString("type"));
                                            announcement.setNumberBathrooms((doc.getResult().getLong("numberBathrooms")).intValue());
                                            announcement.setNumberBedrooms((doc.getResult().getLong("numberBedrooms")).intValue());
                                            announcement.setNumberResidents((doc.getResult().getLong("numberResidents")).intValue());
                                            announcement.setPrice(doc.getResult().getDouble("price"));
                                            announcement.setLatPoint(doc.getResult().getDouble("latPoint"));
                                            announcement.setLngPoint(doc.getResult().getDouble("lngPoint"));
                                            announcement.setUserId(doc.getResult().getString("userId"));
                                            announcement.setStatus((doc.getResult().getLong("status")).intValue());

                                            bookmarks.add(announcement);
                                        }
                                        if (bookmarks.size() > 0) {
                                            loading(false);
                                            BookmarksAdapter bookmarksAdapter = new BookmarksAdapter(bookmarks, getApplicationContext());
                                            binding.bookmarksRecyclerView.setAdapter(bookmarksAdapter);
                                            binding.bookmarksRecyclerView.setVisibility(View.VISIBLE);
                                        }
                                    });
                        }
                    } else {
                        showErrorMessage();
                        loading(false);
                    }
                })
                .addOnFailureListener(exception -> {
                    showToast(exception.getMessage());
                    loading(false);
                });
    }

    private void showErrorMessage() {
        binding.textErrorMessage.setText(String.format("%s", "Nenhum anúncio salvo encontrado!"));
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
        Intent intent = new Intent(getApplicationContext(), AnnouncementDetailsActivity.class);
        intent.putExtra(Constants.KEY_ANNOUNCEMENT, announcement);
        startActivity(intent);
        finish();
    }
}