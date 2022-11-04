package com.example.share.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.share.R;
import com.example.share.databinding.ActivityResetPasswordBinding;
import com.example.share.utilities.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ResetPasswordActivity extends AppCompatActivity {
    private ActivityResetPasswordBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        setListeners();
    }

    private void setListeners() {
        binding.textBack.setOnClickListener(v -> onBackPressed());
        binding.buttonSendEmail.setOnClickListener(v -> {
            validateEmailAccount();
        });
    }

    private void validateEmailAccount() {
        String email = binding.inputEmail.getText().toString();

        loading(true);
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);

                        if (documentSnapshot.getString(Constants.KEY_PASSWORD) == null) {
                            showToast("Não é possível alterar a senha deste usuário!");
                        } else {
                            sendEmailForResetPassword(documentSnapshot.getString(Constants.KEY_EMAIL));
                        }
                        loading(false);
                    } else {
                        loading(false);
                        showToast("Esse e-mail não está associado a nenhum usuário!");
                    }
                });
    }


    private void sendEmailForResetPassword(String email) {
        loading(true);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        loading(false);
                        showAlertDialog(email);
                    } else {
                        loading(false);
                        showToast("Falha ao enviar e-mail");
                    }
                })
                .addOnFailureListener(e -> {
                    loading(false);
                    showToast(e.getMessage());
                });
    }

    @SuppressLint("ResourceType")
    private void showAlertDialog(String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ResetPasswordActivity.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(ResetPasswordActivity.this).inflate(
                R.layout.layout_dialog_reset_password, null
        );
        builder.setView(view);
        ((TextView) view.findViewById(R.id.textTitle)).setText("Instruções enviadas");
        ((TextView) view.findViewById(R.id.textMessage)).setText("Enviamos as intruções de mudança de senha para " + email + ". Verifique sua caixa de entrada e de spam.");

        final AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);

        view.findViewById(R.id.buttonOk).setOnClickListener(v -> {
            alertDialog.dismiss();
            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
            intent.putExtra("emailResetPassword", email);
            startActivity(intent);
            finish();
        });

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        alertDialog.show();
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonSendEmail.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSendEmail.setVisibility(View.VISIBLE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}