package com.example.examenpm01_p3;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private EditText etIdOrden, etDescripcion, etPeriodista, etFecha;
    private Button btnSeleccionarImagen, btnSeleccionarAudio, btnGuardar, btnVerLista;
    private ImageView ivImagen;

    private Uri imagenUri;
    private Uri audioUri;
    private Calendar fechaSeleccionada;

    private DatabaseReference database;
    private StorageReference storage;

    private static final int PICK_IMAGE_GALLERY = 1;
    private static final int PICK_AUDIO = 2;
    private static final int TAKE_PHOTO = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = FirebaseDatabase.getInstance().getReference("entrevistas");
        storage = FirebaseStorage.getInstance().getReference("entrevistas");

        etIdOrden = findViewById(R.id.etIdOrden);
        etDescripcion = findViewById(R.id.etDescripcion);
        etPeriodista = findViewById(R.id.etPeriodista);
        etFecha = findViewById(R.id.etFecha);
        btnSeleccionarImagen = findViewById(R.id.btnSeleccionarImagen);
        btnSeleccionarAudio = findViewById(R.id.btnSeleccionarAudio);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnVerLista = findViewById(R.id.btnVerLista);
        ivImagen = findViewById(R.id.ivImagen);

        fechaSeleccionada = Calendar.getInstance();

        btnSeleccionarImagen.setOnClickListener(v -> mostrarDialogoImagen());
        btnSeleccionarAudio.setOnClickListener(v -> seleccionarAudio());
        etFecha.setOnClickListener(v -> mostrarDatePicker());
        btnGuardar.setOnClickListener(v -> guardarEntrevista());
        btnVerLista.setOnClickListener(v -> irALista());
    }

    private void mostrarDialogoImagen() {
        String[] opciones = {"Tomar foto", "Seleccionar de galería"};

        new AlertDialog.Builder(this)
                .setTitle("Imagen del entrevistado")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        tomarFoto();
                    } else {
                        seleccionarImagenGaleria();
                    }
                })
                .show();
    }

    private void seleccionarImagenGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_GALLERY);
    }

    private void tomarFoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, TAKE_PHOTO);
        } else {
            Toast.makeText(this, "No hay cámara disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private void seleccionarAudio() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_AUDIO);
    }

    private void mostrarDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    fechaSeleccionada.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    etFecha.setText(sdf.format(fechaSeleccionada.getTime()));
                },
                fechaSeleccionada.get(Calendar.YEAR),
                fechaSeleccionada.get(Calendar.MONTH),
                fechaSeleccionada.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void guardarEntrevista() {
        String idOrden = etIdOrden.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String periodista = etPeriodista.getText().toString().trim();
        String fechaStr = etFecha.getText().toString().trim();

        if (idOrden.isEmpty() || descripcion.isEmpty() || periodista.isEmpty() || fechaStr.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imagenUri == null || audioUri == null) {
            Toast.makeText(this, "Selecciona imagen y audio", Toast.LENGTH_SHORT).show();
            return;
        }

        subirArchivos(idOrden, descripcion, periodista, fechaSeleccionada.getTimeInMillis());
    }

    private void subirArchivos(String idOrden, String descripcion, String periodista, long fecha) {
        String imagenId = UUID.randomUUID().toString();
        String audioId = UUID.randomUUID().toString();

        StorageReference imagenRef = storage.child("imagenes/" + imagenId);
        StorageReference audioRef = storage.child("audios/" + audioId);

        imagenRef.putFile(imagenUri).addOnSuccessListener(task -> {
            imagenRef.getDownloadUrl().addOnSuccessListener(imagenUrl -> {
                audioRef.putFile(audioUri).addOnSuccessListener(task2 -> {
                    audioRef.getDownloadUrl().addOnSuccessListener(audioUrl -> {
                        guardarEnBaseDatos(idOrden, descripcion, periodista, fecha,
                                imagenUrl.toString(), audioUrl.toString());
                    });
                });
            });
        }).addOnFailureListener(e ->
                Toast.makeText(MainActivity.this, "Error al subir imagen", Toast.LENGTH_SHORT).show()
        );
    }

    private void guardarEnBaseDatos(String idOrden, String descripcion, String periodista,
                                    long fecha, String imagenUri, String audioUri) {

        String key = database.push().getKey();
        Entrevista entrevista = new Entrevista(idOrden, descripcion, periodista, fecha, imagenUri, audioUri);

        if (key == null) {
            Toast.makeText(this, "Error al generar clave", Toast.LENGTH_SHORT).show();
            return;
        }

        database.child(key).setValue(entrevista).addOnSuccessListener(aVoid -> {
            Toast.makeText(MainActivity.this, "Entrevista guardada", Toast.LENGTH_SHORT).show();
            limpiarFormulario();
        }).addOnFailureListener(e ->
                Toast.makeText(MainActivity.this, "Error al guardar", Toast.LENGTH_SHORT).show()
        );
    }

    private void limpiarFormulario() {
        etIdOrden.setText("");
        etDescripcion.setText("");
        etPeriodista.setText("");
        etFecha.setText("");
        ivImagen.setImageResource(android.R.drawable.ic_menu_gallery);
        imagenUri = null;
        audioUri = null;
    }

    private void irALista() {
        startActivity(new Intent(MainActivity.this, ListaEntrevistasActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == PICK_IMAGE_GALLERY) {
                imagenUri = data.getData();
                Glide.with(this).load(imagenUri).into(ivImagen);

            } else if (requestCode == TAKE_PHOTO) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    if (imageBitmap != null) {
                        ivImagen.setImageBitmap(imageBitmap);

                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                        String path = MediaStore.Images.Media.insertImage(
                                getContentResolver(), imageBitmap, "FotoEntrevistado", null);
                        imagenUri = Uri.parse(path);
                    }
                }

            } else if (requestCode == PICK_AUDIO) {
                audioUri = data.getData();
                Toast.makeText(this, "Audio seleccionado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}