package com.example.share.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.share.databinding.ActivityAddNewAnnouncementBinding;
import com.example.share.models.Announcement;
import com.example.share.utilities.Constants;
import com.example.share.utilities.PreferenceManager;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class AddNewAnnouncementActivity extends BaseActivity {
    private ActivityAddNewAnnouncementBinding binding;
    private String encodedImage;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Double latitude, longitude;
    private LocationRequest locationRequest;
    private PreferenceManager preferenceManager;
    private Announcement editAnnouncement;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddNewAnnouncementBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setContentView(binding.getRoot());
        database = FirebaseFirestore.getInstance();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(500);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        setStartLocationAction();
        editAnnouncement = (Announcement) getIntent().getSerializableExtra(Constants.KEY_EDIT_ANNOUNCEMENT);
        verifyStatus();
        setListeners();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1000 && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkSettingsAndStartLocationUpdates();
            } else {
                Toast.makeText(this, "Permissão de localização recusada!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setStartLocationAction() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    (this),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1000
            );
        } else {
            checkSettingsAndStartLocationUpdates();
        }
    }

    private void getLastLocation() {
        @SuppressLint("MissingPermission") Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();

        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    Log.d("location", location.toString());
                    Log.d("latitude", "" + location.getLatitude());
                    Log.d("longitude", "" + location.getLongitude());

                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                } else {
                    Log.e("location error", "location was null...");
                }
            }
        });

        locationTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("location error", e.getLocalizedMessage());
            }
        });
    }

    private void checkSettingsAndStartLocationUpdates() {
        LocationSettingsRequest request = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest).build();

        SettingsClient client = LocationServices.getSettingsClient((this));

        Task<LocationSettingsResponse> locationSettingsResponseTask = client.checkLocationSettings(request);
        locationSettingsResponseTask.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                getLastLocation();
            }
        });
        locationSettingsResponseTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException apiException = (ResolvableApiException) e;
                    try {
                        apiException.startResolutionForResult(AddNewAnnouncementActivity.this, 1001);
                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    private void setListeners() {
        if (editAnnouncement != null) {
            binding.txtTitle.setText("Editar anúncio");
            binding.imageAnnouncement.setImageBitmap(getAnnouncementImage(editAnnouncement.getImage()));
            binding.inputTitle.setText(editAnnouncement.getTitle());
            binding.inputDescription.setText(editAnnouncement.getDescription());

            binding.inputNumberBedrooms.setText(editAnnouncement.getNumberBedrooms() + "");
            binding.inputNumberBathrooms.setText(editAnnouncement.getNumberBathrooms() + "");
            binding.inputNumberResidents.setText(editAnnouncement.getNumberResidents() + "");
            binding.inputPrice.setText(editAnnouncement.getPrice() + "");

            encodedImage = editAnnouncement.getImage();

            for (int i = 0; i < 2; i++) {
                int itemRadioGroup = binding.rdGroup.getChildAt(i).getId();
                RadioButton rbTypeSelected = findViewById(itemRadioGroup);

                if (editAnnouncement.getType().equals(rbTypeSelected.getText().toString())) {
                    binding.rdGroup.check(itemRadioGroup);
                }
            }

            binding.imageDelete.setOnClickListener(v -> {
                disableAnnouncement();
            });
            binding.imageReactivate.setOnClickListener(v -> {
                reactivateAnnouncement();
            });
        }

        binding.imageBack.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MyAnnouncementsActivity.class);
            startActivity(intent);
            finish();
        });
        binding.layoutImage.setOnClickListener(v -> {
            new AlertDialog.Builder(AddNewAnnouncementActivity.this)
                    .setTitle("Origem da imagem")
                    .setMessage("Por favor, selecione a origem da imagem: ")
                    .setPositiveButton("Câmera", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            pickCameraImage.launch(intent);
                        }
                    })
                    .setNegativeButton("Galeria", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            pickImage.launch(intent);
                        }
                    })
                    .show();
        });
        binding.buttonContinueRegister.setOnClickListener(v -> {
            if (isValidForm()) {
                nextStepFormAnnouncement();
            }
        });
    }

    private void disableAnnouncement() {
        if (editAnnouncement != null) {
            new AlertDialog.Builder(AddNewAnnouncementActivity.this)
                    .setMessage("Deseja desativar este anúncio?")
                    .setCancelable(false)
                    .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            database.collection(Constants.KEY_COLLECTION_ANNOUNCEMENTS)
                                    .document(editAnnouncement.getId())
                                    .update(Constants.KEY_ANNOUNCEMENT_STATUS, 0)
                                    .addOnSuccessListener(v -> {
                                        database.collection("bookmarks")
                                                .whereEqualTo("announcementId", editAnnouncement.getId())
                                                .get()
                                                .addOnCompleteListener(task -> {
                                                    if (task.isSuccessful() && task.getResult() != null) {
                                                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                                            database.collection("bookmarks")
                                                                    .document(documentSnapshot.getId())
                                                                    .update("status", 0)
                                                                    .addOnFailureListener(e -> showToast("Unable to disable announcement"));
                                                        }
                                                    }
                                                });

                                        Intent intent = new Intent(getApplicationContext(), MyAnnouncementsActivity.class);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> showToast("Unable to disable announcement"));

                        }
                    })
                    .setNegativeButton("Não", null)
                    .show();
        }
    }

    private void reactivateAnnouncement() {
        if (editAnnouncement != null) {
            new AlertDialog.Builder(AddNewAnnouncementActivity.this)
                    .setMessage("Deseja reativar este anúncio?")
                    .setCancelable(false)
                    .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            database.collection(Constants.KEY_COLLECTION_ANNOUNCEMENTS)
                                    .document(editAnnouncement.getId())
                                    .update(Constants.KEY_ANNOUNCEMENT_STATUS, 1)
                                    .addOnSuccessListener(v -> {
                                        database.collection("bookmarks")
                                                .whereEqualTo("announcementId", editAnnouncement.getId())
                                                .get()
                                                .addOnCompleteListener(task -> {
                                                    if (task.isSuccessful() && task.getResult() != null) {

                                                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                                            database.collection("bookmarks")
                                                                    .document(documentSnapshot.getId())
                                                                    .update("status", 1)
                                                                    .addOnFailureListener(e -> showToast("Unable to reactivate announcement"));
                                                        }
                                                    }
                                                });

                                        Intent intent = new Intent(getApplicationContext(), MyAnnouncementsActivity.class);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> showToast("Unable to reactivate announcement"));

                        }
                    })
                    .setNegativeButton("Não", null)
                    .show();
        }
    }

    private void verifyStatus() {
        if (editAnnouncement != null) {
            if (editAnnouncement.getStatus() == 1) {
                binding.imageDelete.setVisibility(View.VISIBLE);
                binding.imageReactivate.setVisibility(View.GONE);
            } else if (editAnnouncement.getStatus() == 0) {
                binding.imageDelete.setVisibility(View.GONE);
                binding.imageReactivate.setVisibility(View.VISIBLE);
            }
        } else {
            binding.imageDelete.setVisibility(View.GONE);
            binding.imageReactivate.setVisibility(View.GONE);
        }
    }

    private void nextStepFormAnnouncement() {
        int itemRadioGroup = binding.rdGroup.getCheckedRadioButtonId();
        RadioButton rbTypeSelected = findViewById(itemRadioGroup);

        String image = encodedImage;
        String title = binding.inputTitle.getText().toString();
        String description = binding.inputDescription.getText().toString();
        String type = rbTypeSelected.getText().toString();
        String numberBathrooms = binding.inputNumberBathrooms.getText().toString();
        String numberBedrooms = binding.inputNumberBedrooms.getText().toString();
        String numberResidents = binding.inputNumberResidents.getText().toString();
        String price = binding.inputPrice.getText().toString();

        Announcement announcement = new Announcement();
        announcement.setUserId(preferenceManager.getString(Constants.KEY_USER_ID));
        announcement.setImage(image);
        announcement.setTitle(title);
        announcement.setDescription(description);
        announcement.setType(type);
        announcement.setNumberBathrooms(Integer.valueOf(numberBathrooms));
        announcement.setNumberBedrooms(Integer.valueOf(numberBedrooms));
        announcement.setNumberResidents(Integer.valueOf(numberResidents));
        announcement.setPrice(Double.valueOf(price));
        announcement.setStatus(1);

        if (latitude == null && longitude == null) {
            // Centro da cidade Quixadá
            announcement.setLatPoint(-4.96998);
            announcement.setLngPoint(-39.01586);
        } else {
            announcement.setLatPoint(latitude);
            announcement.setLngPoint(longitude);
        }

        if (editAnnouncement != null) {
            announcement.setId(editAnnouncement.getId());
            announcement.setLatPoint(editAnnouncement.getLatPoint());
            announcement.setLngPoint(editAnnouncement.getLngPoint());
        }

        Intent intent = new Intent(this, AddLocationNewAnnouncementActivity.class);
        intent.putExtra(Constants.KEY_ANNOUNCEMENT, announcement);
        startActivity(intent);
    }

    private String encodeImage(Bitmap bitmap) {
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, 256, 256, true);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
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
                            binding.imageAnnouncement.setImageBitmap(bitmap);
                            binding.textAddImage.setVisibility(View.GONE);
                            binding.iconAddImage.setVisibility(View.GONE);
                            encodedImage = encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> pickCameraImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                        try {
                            binding.imageAnnouncement.setImageBitmap(bitmap);
                            binding.textAddImage.setVisibility(View.GONE);
                            binding.iconAddImage.setVisibility(View.GONE);
                            encodedImage = encodeImage(bitmap);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private Boolean isValidForm() {
        if (encodedImage == null) {
            showToast("Selecione uma foto do imóvel!");
            return false;
        } else if (binding.inputTitle.getText().toString().trim().isEmpty()) {
            showToast("Informe o título do anúncio!");
            return false;
        } else if (binding.inputDescription.getText().toString().trim().isEmpty()) {
            showToast("Informe uma descrição para o anúncio!");
            return false;
        } else if (binding.rdGroup.getCheckedRadioButtonId() == -1) {
            showToast("Selecione o tipo de imóvel!");
            return false;
        } else if (binding.inputNumberBedrooms.getText().toString().trim().isEmpty()) {
            showToast("Informe a quantidade de quartos!");
            return false;
        } else if (binding.inputNumberBathrooms.getText().toString().trim().isEmpty()) {
            showToast("Informe a quantidade de banheiros!");
            return false;
        } else if (binding.inputNumberResidents.getText().toString().trim().isEmpty()) {
            showToast("Informe a quantidade de residentes!");
            return false;
        } else if (binding.inputPrice.getText().toString().trim().isEmpty()) {
            showToast("Informe o preço a ser dividido");
            return false;
        } else {
            return true;
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private Bitmap getAnnouncementImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}