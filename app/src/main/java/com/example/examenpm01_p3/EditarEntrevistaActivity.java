package com.example.examenpm01_p3;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class EditarEntrevistaActivity extends AppCompatActivity {

    private EditText etIdOrden, etDescripcion, etPeriodista, etFecha;
    private ImageView ivImagen;
    private Button btnActualizar, btnEliminar, btnCancelar, btnCambiarAudio;

    private String entrevistaKey;
    private Entrevista entrevista;
    private DatabaseReference database;
    private StorageReference storage;

    private Calendar fechaSeleccionada;

    private static final int PICK_IMAGE_GALLERY_EDIT = 10;
    private static final int TAKE_PHOTO_EDIT = 11;
    private static final int PICK_AUDIO_EDIT = 12;

    private Uri nuevaImagenUri;
    private Uri nuevoAudioUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_entrevista);

        etIdOrden = findViewById(R.id.etIdOrden);
        etDescripcion = findViewById(R.id.etDescripcion);
        etPeriodista = findViewById(R.id.etPeriodista);
        etFecha = findViewById(R.id.etFecha);
        ivImagen = findViewById(R.id.ivImagen);
        btnActualizar = findViewById(R.id.btnActualizar);
        btnEliminar = findViewById(R.id.btnEliminar);
        btnCancelar = findViewById(R.id.btnCancelar);
        btnCambiarAudio = findViewById(R.id.btnCambiarAudio);

        database = FirebaseDatabase.getInstance().getReference("entrevistas");
        storage = FirebaseStorage.getInstance().getReference("entrevistas");

        fechaSeleccionada = Calendar.getInstance();

        Intent intent = getIntent();
        entrevistaKey = intent.getStringExtra("key");
        entrevista = (Entrevista) intent.getSerializableExtra("entrevista");

        if (entrevista != null) {
            llenarFormulario();
        }

        etFecha.setOnClickListener(v -> mostrarDatePicker());

        ivImagen.setOnClickListener(v -> mostrarDialogoImagenEditar());

        btnCambiarAudio.setOnClickListener(v -> seleccionarAudioEditar());

        btnActualizar.setOnClickListener(v -> actualizarEntrevista());
        btnEliminar.setOnClickListener(v -> eliminarEntrevista());
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void llenarFormulario() {
        etIdOrden.setText(entrevista.getIdOrden());
        etDescripcion.setText(entrevista.getDescripcion());
        etPeriodista.setText(entrevista.getPeriodista());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        etFecha.setText(sdf.format(new Date(entrevista.getFecha())));

        Glide.with(this).load(entrevista.getImagenUri()).into(ivImagen);
    }

    private void mostrarDatePicker() {
        fechaSeleccionada.setTimeInMillis(entrevista.getFecha());

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    fechaSeleccionada.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    etFecha.setText(sdf.format(fechaSeleccionada.getTime()));
                    entrevista.setFecha(fechaSeleccionada.getTimeInMillis());
                },
                fechaSeleccionada.get(Calendar.YEAR),
                fechaSeleccionada.get(Calendar.MONTH),
                fechaSeleccionada.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void mostrarDialogoImagenEditar() {
        String[] opciones = {"Tomar nueva foto", "Seleccionar de galería"};

        new AlertDialog.Builder(this)
                .setTitle("Cambiar imagen")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        tomarFotoEditar();
                    } else {
                        seleccionarImagenGaleriaEditar();
                    }
                })
                .show();
    }

    private void seleccionarImagenGaleriaEditar() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_GALLERY_EDIT);
    }

    private void tomarFotoEditar() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, TAKE_PHOTO_EDIT);
        } else {
            Toast.makeText(this, "No hay cámara disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private void seleccionarAudioEditar() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_AUDIO_EDIT);
    }

    private void actualizarEntrevista() {
        String idOrden = etIdOrden.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String periodista = etPeriodista.getText().toString().trim();

        if (idOrden.isEmpty() || descripcion.isEmpty() || periodista.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        entrevista.setIdOrden(idOrden);
        entrevista.setDescripcion(descripcion);
        entrevista.setPeriodista(periodista);
        // fecha ya se actualizó cuando seleccionaste en el DatePicker

        // Ahora decidir si hay que subir nueva imagen/audio
        if (nuevaImagenUri != null || nuevoAudioUri != null) {
            subirCambiosMultimedia();
        } else {
            guardarCambiosEnBD();
        }
    }

    private void subirCambiosMultimedia() {
        // Vamos encadenando subidas según lo que cambió
        if (nuevaImagenUri != null) {
            String imagenId = UUID.randomUUID().toString();
            StorageReference imagenRef = storage.child("imagenes/" + imagenId);

            imagenRef.putFile(nuevaImagenUri).addOnSuccessListener(task ->
                    imagenRef.getDownloadUrl().addOnSuccessListener(url -> {
                        entrevista.setImagenUri(url.toString());
                        // Luego ver si también hay audio nuevo
                        if (nuevoAudioUri != null) {
                            subirNuevoAudio();
                        } else {
                            guardarCambiosEnBD();
                        }
                    })
            ).addOnFailureListener(e ->
                    Toast.makeText(this, "Error al subir nueva imagen", Toast.LENGTH_SHORT).show()
            );
        } else if (nuevoAudioUri != null) {
            // Solo cambió el audio
            subirNuevoAudio();
        }
    }

    private void subirNuevoAudio() {
        String audioId = UUID.randomUUID().toString();
        StorageReference audioRef = storage.child("audios/" + audioId);

        audioRef.putFile(nuevoAudioUri).addOnSuccessListener(task ->
                audioRef.getDownloadUrl().addOnSuccessListener(url -> {
                    entrevista.setAudioUri(url.toString());
                    guardarCambiosEnBD();
                })
        ).addOnFailureListener(e ->
                Toast.makeText(this, "Error al subir nuevo audio", Toast.LENGTH_SHORT).show()
        );
    }

    private void guardarCambiosEnBD() {
        database.child(entrevistaKey).setValue(entrevista).addOnSuccessListener(aVoid -> {
            Toast.makeText(EditarEntrevistaActivity.this, "Entrevista actualizada", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e ->
                Toast.makeText(EditarEntrevistaActivity.this, "Error al actualizar", Toast.LENGTH_SHORT).show()
        );
    }

    private void eliminarEntrevista() {
        database.child(entrevistaKey).removeValue().addOnSuccessListener(aVoid -> {
            Toast.makeText(EditarEntrevistaActivity.this, "Entrevista eliminada", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e ->
                Toast.makeText(EditarEntrevistaActivity.this, "Error al eliminar", Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == PICK_IMAGE_GALLERY_EDIT) {
                nuevaImagenUri = data.getData();
                Glide.with(this).load(nuevaImagenUri).into(ivImagen);

            } else if (requestCode == TAKE_PHOTO_EDIT) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    if (imageBitmap != null) {
                        ivImagen.setImageBitmap(imageBitmap);

                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                        String path = MediaStore.Images.Media.insertImage(
                                getContentResolver(), imageBitmap, "FotoEntrevistadoEdit", null);
                        nuevaImagenUri = Uri.parse(path);
                    }
                }

            } else if (requestCode == PICK_AUDIO_EDIT) {
                nuevoAudioUri = data.getData();
                Toast.makeText(this, "Nuevo audio seleccionado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}