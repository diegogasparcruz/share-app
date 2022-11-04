package com.example.share.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.share.databinding.ActivityTermsOfUseBinding;

public class TermsOfUseActivity extends AppCompatActivity {
    private ActivityTermsOfUseBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTermsOfUseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.pdfViewer.fromAsset("termo.pdf");
        binding.pdfViewer.isZoomEnabled();
        binding.pdfViewer.show();
    }
}