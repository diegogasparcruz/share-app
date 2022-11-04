package com.example.share.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.share.R;
import com.example.share.activities.AnnouncementDetailsActivity;
import com.example.share.adapters.HomeAdapter;
import com.example.share.databinding.FragmentHomeBinding;
import com.example.share.listeners.AnnouncementListener;
import com.example.share.models.Announcement;
import com.example.share.utilities.Constants;
import com.example.share.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements AnnouncementListener {
    private FragmentHomeBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        preferenceManager = new PreferenceManager(getContext());
        database = FirebaseFirestore.getInstance();
        loadUserDetails();
        loadAnnouncements();
        setListeners();
        return binding.getRoot();
    }

    private void setListeners() {
        binding.imageFilter.setOnClickListener(v -> {
            showFilterAlertDialog();
        });
    }

    @SuppressLint("ResourceType")
    private void showFilterAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme);
        View view = LayoutInflater.from(getContext()).inflate(
                R.layout.layout_dialog_filter, null
        );
        builder.setView(view);
        ((TextView) view.findViewById(R.id.textTitle)).setText("Filtrar por: ");
        RadioGroup radioGroup = view.findViewById(R.id.rdGroup);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);

        view.findViewById(R.id.buttonFilter).setOnClickListener(v -> {
            int radioId = radioGroup.getCheckedRadioButtonId();
            if (radioId == -1) {
                showToast("Nenhuma opção selecionada!");
                return;
            }
            alertDialog.dismiss();

            RadioButton rbTypeSelected = view.findViewById(radioId);
            String type = rbTypeSelected.getText().toString();

            if (type.equals("Casa")) {
                loadAnnouncementsByType(type);
            } else if (type.equals("Apartamento")) {
                loadAnnouncementsByType(type);
            }
        });

        view.findViewById(R.id.buttonEmpty).setOnClickListener(v -> {
            alertDialog.dismiss();
            loadAnnouncements();
        });

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        alertDialog.show();
    }

    private void loadUserDetails() {
        binding.textName.setText(preferenceManager.getString(Constants.KEY_NAME));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }

    private void loadAnnouncements() {
        loading(true);

        database.collection(Constants.KEY_COLLECTION_ANNOUNCEMENTS)
                .whereEqualTo(Constants.KEY_ANNOUNCEMENT_STATUS, 1)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);

                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Announcement> announcements = new ArrayList<>();

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
                            announcement.setStatus(queryDocumentSnapshot.getLong(Constants.KEY_ANNOUNCEMENT_STATUS).intValue());

                            announcements.add(announcement);
                        }
                        if (announcements.size() > 0) {
                            HomeAdapter homeAdapter = new HomeAdapter(announcements, this);
                            binding.homeRecyclerView.setAdapter(homeAdapter);
                            binding.homeRecyclerView.setVisibility(View.VISIBLE);
                        } else {
                            showErrorMessage();
                        }
                    } else {
                        showErrorMessage();
                    }
                });
    }

    private void loadAnnouncementsByType(String type) {
        loading(true);
        database.collection(Constants.KEY_COLLECTION_ANNOUNCEMENTS)
                .whereEqualTo(Constants.KEY_ANNOUNCEMENT_TYPE, type)
                .whereEqualTo(Constants.KEY_ANNOUNCEMENT_STATUS, 1)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);

                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Announcement> announcements = new ArrayList<>();

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
                            announcement.setStatus(queryDocumentSnapshot.getLong(Constants.KEY_ANNOUNCEMENT_STATUS).intValue());

                            announcements.add(announcement);
                        }
                        if (announcements.size() > 0) {
                            HomeAdapter homeAdapter = new HomeAdapter(announcements, this);
                            binding.homeRecyclerView.setAdapter(homeAdapter);
                            binding.homeRecyclerView.setVisibility(View.VISIBLE);
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
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCardAnnouncementClicked(Announcement announcement) {
        Intent intent = new Intent(getContext(), AnnouncementDetailsActivity.class);
        intent.putExtra(Constants.KEY_ANNOUNCEMENT, announcement);
        startActivity(intent);
    }
}