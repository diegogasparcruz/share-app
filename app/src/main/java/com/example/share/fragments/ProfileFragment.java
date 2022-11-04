package com.example.share.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.share.R;
import com.example.share.adapters.ProfileAdapter;
import com.example.share.databinding.FragmentProfileBinding;
import com.example.share.utilities.Constants;
import com.example.share.utilities.PreferenceManager;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        binding.profileRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.profileRecyclerView
                .addItemDecoration(new DividerItemDecoration(binding.profileRecyclerView.getContext(),
                        DividerItemDecoration.VERTICAL));
        preferenceManager = new PreferenceManager(getContext());

        binding.imageProfile.setImageBitmap(getUserImage(preferenceManager.getString(Constants.KEY_IMAGE)));

        loadProfileAdapter();
        return binding.getRoot();
    }

    private void loadProfileAdapter() {
        ProfileAdapter profileAdapter = new ProfileAdapter(getContext());
        binding.profileRecyclerView.setAdapter(profileAdapter);
    }

    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}