package com.example.share.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.share.activities.AnnouncementDetailsActivity;
import com.example.share.databinding.ItemContainerAnnouncementsBinding;
import com.example.share.models.Announcement;
import com.example.share.utilities.Constants;

import java.util.List;

public class BookmarksAdapter extends RecyclerView.Adapter<BookmarksAdapter.BookmarkViewHolder> {
    private final List<Announcement> bookmarks;
    private final Context context;

    public BookmarksAdapter(List<Announcement> bookmarks, Context context) {
        this.bookmarks = bookmarks;
        this.context = context;
    }

    @NonNull
    @Override
    public BookmarkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerAnnouncementsBinding itemContainerAnnouncementsBinding = ItemContainerAnnouncementsBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );

        return new BookmarkViewHolder(itemContainerAnnouncementsBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull BookmarkViewHolder holder, int position) {
        holder.setData(bookmarks.get(position));
    }

    @Override
    public int getItemCount() {
        return bookmarks.size();
    }

    class BookmarkViewHolder extends RecyclerView.ViewHolder {
        ItemContainerAnnouncementsBinding binding;

        public BookmarkViewHolder(ItemContainerAnnouncementsBinding itemContainerAnnouncementsBinding) {
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
            binding.cardAnnouncement.setOnClickListener(v -> {
                Intent intent = new Intent(context, AnnouncementDetailsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Constants.KEY_ANNOUNCEMENT, announcement);
                context.startActivity(intent);
            });
        }
    }

    private Bitmap getCardImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
