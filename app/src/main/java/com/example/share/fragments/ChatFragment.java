package com.example.share.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.share.R;
import com.example.share.activities.ChatActivity;
import com.example.share.adapters.RecentConversationsAdapter;
import com.example.share.databinding.FragmentChatBinding;
import com.example.share.listeners.ConversationListener;
import com.example.share.models.ChatMessage;
import com.example.share.models.User;
import com.example.share.utilities.Constants;
import com.example.share.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatFragment extends Fragment implements ConversationListener {
    private FragmentChatBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private List<ChatMessage> conversations;
    private RecentConversationsAdapter conversationsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        preferenceManager = new PreferenceManager(getContext());

        init();
        listenConversations();
        return binding.getRoot();
    }

    private void init() {
        conversations = new ArrayList<>();
        conversationsAdapter = new RecentConversationsAdapter(conversations, this);
        binding.conversationsRecyclerView.setAdapter(conversationsAdapter);
        database = FirebaseFirestore.getInstance();

    }

    private void listenConversations() {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if(error != null) {
            return;
        }

        if(value != null){
            loading(true);
            for(DocumentChange documentChange : value.getDocumentChanges()) {
                if(documentChange.getType() == DocumentChange.Type.ADDED) {
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setSenderId(senderId);
                    chatMessage.setReceiverId(receiverId);

                    if(preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)) {
                        chatMessage.setConversationImage(documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE));
                        chatMessage.setConversationName(documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME));
                        chatMessage.setConversationId(documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID));
                    } else {
                        chatMessage.setConversationImage(documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE));
                        chatMessage.setConversationName(documentChange.getDocument().getString(Constants.KEY_SENDER_NAME));
                        chatMessage.setConversationId(documentChange.getDocument().getString(Constants.KEY_SENDER_ID));
                    }
                    chatMessage.setMessage(documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE));
                    chatMessage.setDateObject(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    conversations.add(chatMessage);
                } else if(documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for(int i = 0; i < conversations.size(); i++) {
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);

                        if(conversations.get(i).getSenderId().equals(senderId) && conversations.get(i).getReceiverId().equals(receiverId)){
                            conversations.get(i).setMessage(documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE));
                            conversations.get(i).setDateObject(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                            break;
                        }
                    }
                }
            }

            if(conversations.size() == 0) {
                loading(false);
                showErrorMessage();
            }

            Collections.sort(conversations, (obj1, obj2) -> obj2.getDateObject().compareTo(obj1.getDateObject()));
            conversationsAdapter.notifyDataSetChanged();
            binding.conversationsRecyclerView.smoothScrollToPosition(0);
            binding.conversationsRecyclerView.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
            loading(false);
        }
    };

    private void showErrorMessage() {
        binding.textErrorMessage.setText(String.format("%s", "Nenhuma conversa encontrada!"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onConversionClicked(User user) {
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }
}