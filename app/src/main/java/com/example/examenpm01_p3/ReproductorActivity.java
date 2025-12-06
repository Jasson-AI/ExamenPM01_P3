package com.example.examenpm01_p3;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReproductorActivity extends AppCompatActivity {

    private ImageView ivImagen;
    private TextView tvPeriodista, tvDescripcion, tvFecha;
    private Button btnReproducir, btnPausar, btnDetener;
    private SeekBar seekBar;
    private TextView tvTiempoActual, tvDuracion;

    private MediaPlayer mediaPlayer;
    private Entrevista entrevista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reproductor);

        ivImagen = findViewById(R.id.ivImagen);
        tvPeriodista = findViewById(R.id.tvPeriodista);
        tvDescripcion = findViewById(R.id.tvDescripcion);
        tvFecha = findViewById(R.id.tvFecha);
        btnReproducir = findViewById(R.id.btnReproducir);
        btnPausar = findViewById(R.id.btnPausar);
        btnDetener = findViewById(R.id.btnDetener);
        seekBar = findViewById(R.id.seekBar);
        tvTiempoActual = findViewById(R.id.tvTiempoActual);
        tvDuracion = findViewById(R.id.tvDuracion);

        Intent intent = getIntent();
        entrevista = (Entrevista) intent.getSerializableExtra("entrevista");

        if (entrevista != null) {
            mostrarDetalles();
            inicializarMediaPlayer();
        }

        btnReproducir.setOnClickListener(v -> reproducir());
        btnPausar.setOnClickListener(v -> pausar());
        btnDetener.setOnClickListener(v -> detener());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void mostrarDetalles() {
        tvPeriodista.setText("Periodista: " + entrevista.getPeriodista());
        tvDescripcion.setText("Descripción: " + entrevista.getDescripcion());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvFecha.setText("Fecha: " + sdf.format(new Date(entrevista.getFecha())));

        Glide.with(this).load(entrevista.getImagenUri()).into(ivImagen);
    }

    private void inicializarMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(entrevista.getAudioUri());
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                seekBar.setMax(mediaPlayer.getDuration());
                tvDuracion.setText(formatoTiempo(mediaPlayer.getDuration()));
            });
            mediaPlayer.setOnCompletionListener(mp -> detener());
        } catch (Exception e) {
            Toast.makeText(this, "Error al cargar audio", Toast.LENGTH_SHORT).show();
        }
    }

    private void reproducir() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            btnReproducir.setEnabled(false);
            btnPausar.setEnabled(true);
            actualizarProgreso();
        }
    }

    private void pausar() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            btnReproducir.setEnabled(true);
            btnPausar.setEnabled(false);
        }
    }

    private void detener() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            seekBar.setProgress(0);
            tvTiempoActual.setText("00:00");
            btnReproducir.setEnabled(true);
            btnPausar.setEnabled(false);
            inicializarMediaPlayer();
        }
    }

    private void actualizarProgreso() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            tvTiempoActual.setText(formatoTiempo(mediaPlayer.getCurrentPosition()));
            seekBar.postDelayed(this::actualizarProgreso, 500);
        }
    }

    private String formatoTiempo(int milisegundos) {
        int segundos = milisegundos / 1000;
        int minutos = segundos / 60;
        segundos = segundos % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutos, segundos);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}