package com.example.share.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.share.databinding.ItemContainerAnnouncementsBinding;
import com.example.share.listeners.AnnouncementListener;
import com.example.share.models.Announcement;

import java.util.List;

public class MyAnnouncementsAdapter extends RecyclerView.Adapter<MyAnnouncementsAdapter.MyAnnouncementsViewHolder> {
    private final List<Announcement> announcements;
    private final AnnouncementListener myAnnouncementsListener;

    public MyAnnouncementsAdapter(List<Announcement> announcements, AnnouncementListener myAnnouncementsListener) {
        this.announcements = announcements;
        this.myAnnouncementsListener = myAnnouncementsListener;
    }

    @NonNull
    @Override
    public MyAnnouncementsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerAnnouncementsBinding itemContainerAnnouncementsBinding = ItemContainerAnnouncementsBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false);

        return new MyAnnouncementsViewHolder(itemContainerAnnouncementsBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyAnnouncementsViewHolder holder, int position) {
        holder.setData(announcements.get(position));
    }

    @Override
    public int getItemCount() {
        return announcements.size();
    }

    class MyAnnouncementsViewHolder extends RecyclerView.ViewHolder {
        ItemContainerAnnouncementsBinding binding;

        MyAnnouncementsViewHolder(ItemContainerAnnouncementsBinding itemContainerAnnouncementsBinding) {
            super(itemContainerAnnouncementsBinding.getRoot());
            binding = itemContainerAnnouncementsBinding;
        }

        void setData(Announcement announcement) {
            binding.title.setText(announcement.getTitle());
            binding.numberBedrooms.setText(announcement.getNumberBedrooms()+"");
            binding.numberBathrooms.setText(announcement.getNumberBathrooms()+"");
            binding.numberResidents.setText(announcement.getNumberResidents()+"");
            binding.money.setText("R$ " + announcement.getPrice());
            binding.imgCard.setImageBitmap(getCardImage(announcement.getImage()));
            binding.getRoot().setOnClickListener(v -> myAnnouncementsListener.onCardAnnouncementClicked(announcement));

            if(announcement.getStatus() == 0) {
                binding.txtInactive.setVisibility(View.VISIBLE);
            }
        }
    }

    private Bitmap getCardImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
