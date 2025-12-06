package com.example.examenpm01_p3;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EntrevistaAdapter extends RecyclerView.Adapter<EntrevistaAdapter.ViewHolder> {

    private List<Entrevista> entrevistas;
    private Context context;

    public EntrevistaAdapter(List<Entrevista> entrevistas, Context context) {
        this.entrevistas = entrevistas;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_entrevista, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Entrevista entrevista = entrevistas.get(position);

        holder.tvIdOrden.setText("ID: " + entrevista.getIdOrden());
        holder.tvPeriodista.setText("Periodista: " + entrevista.getPeriodista());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        holder.tvFecha.setText("Fecha: " + sdf.format(new Date(entrevista.getFecha())));

        Glide.with(context).load(entrevista.getImagenUri()).into(holder.ivImagen);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReproductorActivity.class);
            intent.putExtra("entrevista", entrevista);
            context.startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            Intent intent = new Intent(context, EditarEntrevistaActivity.class);
            intent.putExtra("entrevista", entrevista);
            intent.putExtra("key", entrevista.getIdOrden());
            context.startActivity(intent);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return entrevistas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImagen;
        TextView tvIdOrden, tvPeriodista, tvFecha;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImagen = itemView.findViewById(R.id.ivImagen);
            tvIdOrden = itemView.findViewById(R.id.tvIdOrden);
            tvPeriodista = itemView.findViewById(R.id.tvPeriodista);
            tvFecha = itemView.findViewById(R.id.tvFecha);
        }
    }
}
