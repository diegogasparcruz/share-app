package com.example.share.activities;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.share.databinding.ActivityChangePasswordBinding;
import com.example.share.utilities.Constants;
import com.example.share.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChangePasswordActivity extends BaseActivity {
    private ActivityChangePasswordBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());

        setListeners();
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.buttonSaveEdit.setOnClickListener(v -> {
            if (isValidChangePassword()) {
                changePassword();
            }
        });
    }

    private void changePassword() {
        loading(true);
        String currentPassword = binding.inputCurrentPassword.getText().toString();
        String newPassword = binding.inputNewPassword.getText().toString();

        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .get()
                .addOnCompleteListener(task -> {
                    Log.d(TAG, "" + task.getResult().get(Constants.KEY_PASSWORD));
                    if (task.isSuccessful() && task.getResult() != null) {
                        if (!currentPassword.equals(task.getResult().get(Constants.KEY_PASSWORD))) {
                            loading(false);
                            showToast("A senha atual está errada!");
                        } else if (task.getResult().get(Constants.KEY_PASSWORD).equals(newPassword)) {
                            loading(false);
                            showToast("A nova senha não pode ser igual a senha atual!");
                        } else {
                            mAuth.getCurrentUser().updatePassword(newPassword)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                database.collection(Constants.KEY_COLLECTION_USERS)
                                                        .document(preferenceManager.getString(Constants.KEY_USER_ID))
                                                        .update(Constants.KEY_PASSWORD, newPassword);
                                                showToast("Senha alterada com sucesso!");

                                                Intent intent = new Intent(getApplicationContext(), EditProfileActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }
                                    })
                                    .addOnFailureListener(v -> {
                                        loading(false);
                                        showToast(task.getException().getMessage());
                                    });
                        }
                        loading(false);
                    }

                })
                .addOnFailureListener(exception -> {
                    loading(false);
                    showToast(exception.getMessage());
                });
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonSaveEdit.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSaveEdit.setVisibility(View.VISIBLE);
        }
    }

    private Boolean isValidChangePassword() {
        if (binding.inputCurrentPassword.getText().toString().trim().isEmpty()) {
            showToast("Senha atual é obrigatória!");
            return false;
        }
        if (binding.inputNewPassword.getText().toString().trim().isEmpty()) {
            showToast("Informe a sua nova senha!");
            return false;
        }
        if (binding.inputConfirmNewPassword.getText().toString().trim().isEmpty()) {
            showToast("Confirme sua nova senha!");
            return false;
        } else if (!binding.inputNewPassword.getText().toString().equals(binding.inputConfirmNewPassword.getText().toString())) {
            showToast("A nova senha deve estar correta em ambos os campos!");
            return false;
        } else {
            return true;
        }
    }
}