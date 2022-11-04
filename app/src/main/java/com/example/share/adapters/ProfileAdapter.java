package com.example.share.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.share.R;
import com.example.share.activities.BookmarksActivity;
import com.example.share.activities.EditProfileActivity;
import com.example.share.activities.MyAnnouncementsActivity;
import com.example.share.activities.SignInActivity;
import com.example.share.databinding.ItemContainerProfileBinding;
import com.example.share.utilities.Constants;
import com.example.share.utilities.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {
    private FirebaseAuth mAuth;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private final Context context;
    private final ArrayList<Integer> icons;
    private final ArrayList<String> menuTitles;
    private final ArrayList<String> menuSubtitles;

    public ProfileAdapter(Context context) {
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(context);

        icons = new ArrayList<Integer>();
        icons.add(R.drawable.ic_person);
        icons.add(R.drawable.ic_apartment);
        icons.add(R.drawable.ic_bookmarks);
        icons.add(R.drawable.ic_logout);

        menuTitles = new ArrayList<String>();
        menuTitles.add("Conta");
        menuTitles.add("Anúncios");
        menuTitles.add("Anúncios salvos");
        menuTitles.add("Sair");

        menuSubtitles = new ArrayList<String>();
        menuSubtitles.add("Dados pessoais");
        menuSubtitles.add("Meus anúncios");
        menuSubtitles.add("Meus anúncios salvos");
        menuSubtitles.add("Logout");
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerProfileBinding itemContainerProfileBinding = ItemContainerProfileBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ProfileViewHolder(itemContainerProfileBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        holder.setProfileViewData(
                menuTitles.get(position),
                menuSubtitles.get(position),
                icons.get(position),
                position
        );
    }

    @Override
    public int getItemCount() {
        return menuSubtitles.size();
    }

    class ProfileViewHolder extends RecyclerView.ViewHolder {
        ItemContainerProfileBinding binding;

        ProfileViewHolder(ItemContainerProfileBinding itemContainerProfileBinding) {
            super(itemContainerProfileBinding.getRoot());
            binding = itemContainerProfileBinding;
        }

        void setProfileViewData(String title, String subtitle, int icon, int position) {
            binding.imageIconStart.setImageResource(icon);
            binding.textTitle.setText(title);
            binding.textSubtitle.setText(subtitle);
            binding.imageIconEnd.setImageResource(R.drawable.ic_next);

            binding.imageIconStart.setColorFilter(context.getResources().getColor(R.color.secondary_text));

            if (position == 3) { //POSIÇÃO DO LOGOUT
                binding.imageIconStart.setColorFilter(context.getResources().getColor(R.color.error));
                binding.textTitle.setTextColor(context.getResources().getColor(R.color.error));
                binding.textSubtitle.setTextColor(context.getResources().getColor(R.color.error));
            }

            binding.parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (position) {
                        case 0: { // MEUS DADOS
                            Intent intent = new Intent(context, EditProfileActivity.class);
                            context.startActivity(intent);
                            break;
                        }
                        case 1: { // MEUS ANÚNCIOS
                            Intent intent = new Intent(context, MyAnnouncementsActivity.class);
                            context.startActivity(intent);
                            break;
                        }
                        case 2: { // ITENS SALVOS
                            Intent intent = new Intent(context, BookmarksActivity.class);
                            context.startActivity(intent);
                            break;
                        }
                        case 3: {
                            new AlertDialog.Builder(context)
                                    .setMessage("Deseja fazer logout?")
                                    .setCancelable(false)
                                    .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            Toast.makeText(context, "Deslogando...", Toast.LENGTH_SHORT).show();

                                            DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
                                                    preferenceManager.getString(Constants.KEY_USER_ID)
                                            );
                                            HashMap<String, Object> updates = new HashMap<>();
                                            updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
                                            documentReference.update(updates)
                                                    .addOnSuccessListener(unused -> {
                                                        preferenceManager.clear();
                                                        mAuth.signOut();
                                                        Intent intent = new Intent(context, SignInActivity.class);
                                                        context.startActivity(intent);
                                                    })
                                                    .addOnFailureListener(e -> Log.d("ERRO_LOGOUT", "Unable to sign out"));
                                        }
                                    })
                                    .setNegativeButton("Não", null)
                                    .show();
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                }
            });
        }
    }
}
