package com.example.share.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.share.databinding.ActivitySignInBinding;
import com.example.share.models.Announcement;
import com.example.share.models.User;
import com.example.share.utilities.Constants;
import com.example.share.utilities.PreferenceManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

public class SignInActivity extends AppCompatActivity {
    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseAuth mAuth;
    private FirebaseFirestore database;
    private GoogleSignInClient googleSignInClient;
    private String encodedImage;
    private String emailResetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("280607131055-s21eiqbe1t8r9c57ssn4m7foj81i1rhh.apps.googleusercontent.com")
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }

        emailResetPassword = (String) getIntent().getSerializableExtra("emailResetPassword");
        setListeners();
    }

    private void setListeners() {
        if(emailResetPassword != null) {
            binding.inputEmail.setText(emailResetPassword);
        }

        binding.textCreateNewAccount.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));
        binding.textResetPassword.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), ResetPasswordActivity.class)));
        binding.buttonSignIn.setOnClickListener(v -> {
            if (isValidSignInDetails()) {
                signIn();
            }
        });

        TextView txtButtonGoogle = (TextView) binding.buttonGoogle.getChildAt(0);
        txtButtonGoogle.setText("Fazer login com Google");
        binding.buttonGoogle.setOnClickListener(v -> {
            beforeSignIn();
        });
    }

    private void beforeSignIn() {
        Intent intent = googleSignInClient.getSignInIntent();
        openActivity.launch(intent);
    }

    ActivityResultLauncher<Intent> openActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent intent = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(intent);

                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        signWithGoogle(account);
                    } catch (ApiException apiException) {
                        showToast("Nenhum usuário google logado no aparelho");
                        Log.d("Erro: ", apiException.toString());
                    }
                }
            }
    );

    private void signWithGoogle(GoogleSignInAccount account) {
        AuthCredential authCredential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        mAuth.signInWithCredential(authCredential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                showToast("Login com google efetuado com sucesso");

                database.collection(Constants.KEY_COLLECTION_USERS)
                        .whereEqualTo(Constants.KEY_EMAIL, mAuth.getCurrentUser().getEmail())
                        .get()
                        .addOnCompleteListener(response -> {
                            if (response.isSuccessful() && response.getResult() != null && response.getResult().getDocuments().size() > 0) {
                                DocumentSnapshot documentSnapshot = response.getResult().getDocuments().get(0);
                                preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                                preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                                preferenceManager.putString(Constants.KEY_EMAIL, documentSnapshot.getString(Constants.KEY_EMAIL));
                                preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                                preferenceManager.putBoolean(Constants.KEY_IS_ACCESS_GOOGLE, true);

                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                Bitmap imageBitmap = convertUriToFile(account.getPhotoUrl().toString());
                                encodedImage = encodeImage(imageBitmap);

                                User user = new User();
                                user.setName(account.getDisplayName());
                                user.setEmail(account.getEmail());
                                user.setImage(encodedImage);

                                database.collection(Constants.KEY_COLLECTION_USERS)
                                        .add(user)
                                        .addOnSuccessListener(documentReference -> {
                                            preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                                            preferenceManager.putString(Constants.KEY_NAME, user.getName());
                                            preferenceManager.putString(Constants.KEY_EMAIL, user.getEmail());
                                            preferenceManager.putString(Constants.KEY_IMAGE, user.getImage());
                                            preferenceManager.putBoolean(Constants.KEY_IS_ACCESS_GOOGLE, true);

                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                        })
                                        .addOnFailureListener(exception -> {
                                            showToast(exception.getMessage());
                                        });
                            }
                        });
            } else {
                showToast("Erro ao efetuar login com google");
            }
        });
    }

    private Bitmap convertUriToFile(String imageUri) {
       Bitmap bitmap = null;
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            URL src = new URL(imageUri);
            HttpURLConnection connection = (HttpURLConnection) src.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
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

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignIn.setVisibility(View.VISIBLE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private Boolean isValidSignInDetails() {
        if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Informe seu e-mail!");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Informe um e-mail válido!");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Informe sua senha!");
            return false;
        } else {
            return true;
        }
    }

    private void signIn() {
        loading(true);

        String email = binding.inputEmail.getText().toString();
        String password = binding.inputPassword.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    database.collection(Constants.KEY_COLLECTION_USERS)
                            .whereEqualTo(Constants.KEY_EMAIL, mAuth.getCurrentUser().getEmail())
                            .get()
                            .addOnCompleteListener(response -> {
                                if (response.isSuccessful() && response.getResult() != null && response.getResult().getDocuments().size() > 0) {
                                    DocumentSnapshot documentSnapshot = response.getResult().getDocuments().get(0);

                                    if(emailResetPassword != null && !password.equals(documentSnapshot.getString(Constants.KEY_PASSWORD))) {
                                        database.collection(Constants.KEY_COLLECTION_USERS)
                                                .document(documentSnapshot.getId())
                                                .update(Constants.KEY_PASSWORD, password)
                                                .addOnFailureListener(e -> showToast("Unable to change password"));
                                    }

                                    preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                                    preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                                    preferenceManager.putString(Constants.KEY_EMAIL, documentSnapshot.getString(Constants.KEY_EMAIL));
                                    preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));

                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                } else {
                                    loading(false);
                                    showToast("Unable to sign in");
                                }
                            });
                } else {
                    loading(false);
                    showToast("E-mail ou senha incorretos!");
                }
                loading(false);
            }
        });
    }
}