package com.example.share.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.share.databinding.ItemContainerAnnouncementsBinding;
import com.example.share.listeners.AnnouncementListener;
import com.example.share.models.Announcement;

import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeViewHolder> {
    private final List<Announcement> announcements;
    private final AnnouncementListener announcementListener;

    public HomeAdapter(List<Announcement> announcements, AnnouncementListener announcementListener) {
        this.announcements = announcements;
        this.announcementListener = announcementListener;
    }

    @NonNull
    @Override
    public HomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerAnnouncementsBinding itemContainerAnnouncementsBinding = ItemContainerAnnouncementsBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false);
        return new HomeViewHolder(itemContainerAnnouncementsBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeViewHolder holder, int position) {
        holder.setData(announcements.get(position));
    }

    @Override
    public int getItemCount() {
        return announcements.size();
    }

    class HomeViewHolder extends RecyclerView.ViewHolder {
        ItemContainerAnnouncementsBinding binding;

        HomeViewHolder(ItemContainerAnnouncementsBinding itemContainerAnnouncementsBinding) {
            super(itemContainerAnnouncementsBinding.getRoot());
            binding = itemContainerAnnouncementsBinding;
        }

        void setData(Announcement announcement) {
            binding.title.setText(announcement.getTitle());
            binding.numberBedrooms.setText(announcement.getNumberBedrooms()+"");
            binding.numberBathrooms.setText(announcement.getNumberBathrooms()+"");
            binding.numberResidents.setText(announcement.getNumberResidents()+"");
            binding.money.setText("R$ " + announcement.getPrice());
            binding.txtMonth.setText("/m??s");
            binding.imgCard.setImageBitmap(getCardImage(announcement.getImage()));
            binding.getRoot().setOnClickListener(v -> announcementListener.onCardAnnouncementClicked(announcement));
        }
    }

    private Bitmap getCardImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
