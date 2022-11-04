package com.example.share.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.share.databinding.ActivityEditProfileBinding;
import com.example.share.utilities.Constants;
import com.example.share.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class EditProfileActivity extends BaseActivity {
    private ActivityEditProfileBinding binding;
    private PreferenceManager preferenceManager;
    private String encodedImage;
    private FirebaseFirestore database;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        setListerners();
    }

    private void setListerners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        if (preferenceManager.getBoolean(Constants.KEY_IS_ACCESS_GOOGLE)) {
            binding.inputEmail.setEnabled(false);
            binding.textChangePassword.setVisibility(View.GONE);
        }
        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
        binding.inputName.setText(preferenceManager.getString(Constants.KEY_NAME));
        binding.inputEmail.setText(preferenceManager.getString(Constants.KEY_EMAIL));
        binding.imageProfile.setImageBitmap(getUserImage(preferenceManager.getString(Constants.KEY_IMAGE)));

        encodedImage = preferenceManager.getString(Constants.KEY_IMAGE);

        binding.buttonSaveEdit.setOnClickListener(v -> {
            if (isValidEditProfile()) {
                editProfile();
            }
        });
        binding.textChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ChangePasswordActivity.class);
            startActivity(intent);
        });
    }

    private void editProfile() {
        loading(true);

        String name = binding.inputName.getText().toString();
        String email = binding.inputEmail.getText().toString();

        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME, name);
        user.put(Constants.KEY_EMAIL, email);
        user.put(Constants.KEY_IMAGE, encodedImage);

        if (!email.equals(preferenceManager.getString(Constants.KEY_EMAIL))) {
            mAuth.getCurrentUser().updateEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                showToast("E-mail alterado com sucesso!");
                            }
                        }
                    });
        }

        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .update(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    preferenceManager.putString(Constants.KEY_NAME, name);
                    preferenceManager.putString(Constants.KEY_EMAIL, email);
                    preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);

                    showToast("Alterações salvas com sucesso!");
                })
                .addOnFailureListener(exception -> {
                    loading(false);
                    showToast(exception.getMessage());
                });
        ;
    }

    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();

        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageProfile.setImageBitmap(bitmap);
                            encodedImage = encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

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

    private Boolean isValidEditProfile() {
        if (encodedImage == null) {
            showToast("Selecione uma foto de perfil!");
            return false;
        } else if (binding.inputName.getText().toString().trim().isEmpty()) {
            showToast("Nome é obrigatório!");
            return false;
        } else if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("E-mail é obrigatório!");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("E-mail inválido!");
            return false;
        } else {
            return true;
        }
    }
}