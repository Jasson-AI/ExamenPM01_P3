package com.example.examenpm01_p3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListaEntrevistasActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EntrevistaAdapter adapter;
    private List<Entrevista> entrevistas;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_entrevistas);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        entrevistas = new ArrayList<>();
        adapter = new EntrevistaAdapter(entrevistas, this);
        recyclerView.setAdapter(adapter);

        database = FirebaseDatabase.getInstance().getReference("entrevistas");
        cargarEntrevistas();
    }

    private void cargarEntrevistas() {
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                entrevistas.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Entrevista entrevista = snapshot.getValue(Entrevista.class);
                    if (entrevista != null) {
                        entrevista.setIdOrden(snapshot.getKey());
                        entrevistas.add(entrevista);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ListaEntrevistasActivity.this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
