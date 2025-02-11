package com.ioe_enterprice.inventorytoolsmanagment.Domain;

public class ArticuloDomain {
    private int inventariosArtID;
    private int SKU;
    private long UPC;
    private String descripcion;
    private double ctdContada;
    private double stockTotal;
    private int ubicacionID;
    private int usuarioID;
    private String almacenDescripcion;

    public ArticuloDomain(int inventariosArtID, int SKU, long UPC, String descripcion, double ctdContada, double stockTotal, int ubicacionID, int usuarioID, String almacenDescripcion) {
        this.inventariosArtID = inventariosArtID;
        this.SKU = SKU;
        this.UPC = UPC;
        this.descripcion = descripcion;
        this.ctdContada = ctdContada;
        this.stockTotal = stockTotal;
        this.ubicacionID = ubicacionID;
        this.usuarioID = usuarioID;
        this.almacenDescripcion = almacenDescripcion;
    }

    public int getInventariosArtID() { return inventariosArtID; }
    public int getSKU() { return SKU; }
    public long getUPC() { return UPC; }
    public String getDescripcion() { return descripcion; }
    public double getCtdContada() { return ctdContada; }
    public double getStockTotal() { return stockTotal; }
    public int getUbicacionID() { return ubicacionID; }
    public int getUsuarioID() { return usuarioID; }
    public String getAlmacenDescripcion() {return  almacenDescripcion; }

    public void setCtdContada(double ctdContada) { this.ctdContada = ctdContada; }
}