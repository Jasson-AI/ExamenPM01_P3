package com.example.examenpm01_p3;

import android.content.Intent;
import android.os.Bundle;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditarEntrevistaActivity extends AppCompatActivity {

    private EditText etIdOrden, etDescripcion, etPeriodista, etFecha;
    private ImageView ivImagen;
    private Button btnActualizar, btnEliminar, btnCancelar;

    private String entrevistaKey;
    private Entrevista entrevista;
    private DatabaseReference database;
    private StorageReference storage;

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

        database = FirebaseDatabase.getInstance().getReference("entrevistas");
        storage = FirebaseStorage.getInstance().getReference("entrevistas");

        Intent intent = getIntent();
        entrevistaKey = intent.getStringExtra("key");
        entrevista = (Entrevista) intent.getSerializableExtra("entrevista");

        if (entrevista != null) {
            llenarFormulario();
        }

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

        database.child(entrevistaKey).setValue(entrevista).addOnSuccessListener(aVoid -> {
            Toast.makeText(EditarEntrevistaActivity.this, "Entrevista actualizada", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> Toast.makeText(EditarEntrevistaActivity.this, "Error al actualizar", Toast.LENGTH_SHORT).show());
    }

    private void eliminarEntrevista() {
        database.child(entrevistaKey).removeValue().addOnSuccessListener(aVoid -> {
            Toast.makeText(EditarEntrevistaActivity.this, "Entrevista eliminada", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> Toast.makeText(EditarEntrevistaActivity.this, "Error al eliminar", Toast.LENGTH_SHORT).show());
    }
}