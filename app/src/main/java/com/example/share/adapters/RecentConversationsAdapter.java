package com.example.share.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.share.databinding.ItemContainerRecentConversationBinding;
import com.example.share.listeners.ConversationListener;
import com.example.share.models.ChatMessage;
import com.example.share.models.User;

import java.util.List;

public class RecentConversationsAdapter extends RecyclerView.Adapter<RecentConversationsAdapter.ConversationViweHolder> {
    private final List<ChatMessage> chatMessages;
    private final ConversationListener conversationListener;

    public RecentConversationsAdapter(List<ChatMessage> chatMessages, ConversationListener conversationListener) {
        this.chatMessages = chatMessages;
        this.conversationListener = conversationListener;
    }

    @NonNull
    @Override
    public ConversationViweHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversationViweHolder(
                ItemContainerRecentConversationBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViweHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ConversationViweHolder extends RecyclerView.ViewHolder {
        ItemContainerRecentConversationBinding binding;

        ConversationViweHolder(ItemContainerRecentConversationBinding itemContainerRecentConversationBinding) {
            super(itemContainerRecentConversationBinding.getRoot());
            binding = itemContainerRecentConversationBinding;
        }

        void setData(ChatMessage chatMessage) {
            binding.imageProfile.setImageBitmap(getConversationImage(chatMessage.getConversationImage()));
            binding.textName.setText(chatMessage.getConversationName());
            binding.textRecentMessage.setText(chatMessage.getMessage());
            binding.getRoot().setOnClickListener(v -> {
                User user = new User();
                user.setId(chatMessage.getConversationId());
                user.setName(chatMessage.getConversationName());
                user.setImage(chatMessage.getConversationImage());
                conversationListener.onConversionClicked(user);
            });
        }
    }

    private Bitmap getConversationImage(String encodeImage) {
        byte[] bytes = Base64.decode(encodeImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
