package com.example.examenpm01_p3;

import java.io.Serializable;

public class Entrevista implements Serializable {

    private String key;
    private String idOrden;
    private String descripcion;
    private String periodista;
    private long fecha;
    private String imagenUri;
    private String audioUri;

    public Entrevista() {
        // Constructor vacío requerido por Firebase
    }

    public Entrevista(String idOrden, String descripcion, String periodista,
                      long fecha, String imagenUri, String audioUri) {
        this.idOrden = idOrden;
        this.descripcion = descripcion;
        this.periodista = periodista;
        this.fecha = fecha;
        this.imagenUri = imagenUri;
        this.audioUri = audioUri;
    }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getIdOrden() { return idOrden; }
    public void setIdOrden(String idOrden) { this.idOrden = idOrden; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getPeriodista() { return periodista; }
    public void setPeriodista(String periodista) { this.periodista = periodista; }

    public long getFecha() { return fecha; }
    public void setFecha(long fecha) { this.fecha = fecha; }

    public String getImagenUri() { return imagenUri; }
    public void setImagenUri(String imagenUri) { this.imagenUri = imagenUri; }

    public String getAudioUri() { return audioUri; }
    public void setAudioUri(String audioUri) { this.audioUri = audioUri; }
}
