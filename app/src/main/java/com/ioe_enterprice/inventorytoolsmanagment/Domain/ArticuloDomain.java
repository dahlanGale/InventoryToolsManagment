package com.ioe_enterprice.inventorytoolsmanagment.Domain;

public class ArticuloDomain {
    private int SKU;
    private long UPC;
    private String descripcion;
    private double ctdContada;

    public ArticuloDomain(int SKU, long UPC, String descripcion, double ctdContada) {
        this.SKU = SKU;
        this.UPC = UPC;
        this.descripcion = descripcion;
        this.ctdContada = ctdContada;
    }

    public int getSKU() { return SKU; }
    public long getUPC() { return UPC; }
    public String getDescripcion() { return descripcion; }
    public double getCtdContada() { return ctdContada; }
    public void setCtdContada(double ctdContada) { this.ctdContada = ctdContada; }
}