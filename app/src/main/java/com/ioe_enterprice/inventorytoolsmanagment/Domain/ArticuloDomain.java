package com.ioe_enterprice.inventorytoolsmanagment.Domain;

public class ArticuloDomain {
    private int inventariosArtID;
    private int SKU;
    private long UPC;
    private String descripcion;
    private Double ctdContada;
    private double stockTotal;
    private int ubicacionID;
    private int usuarioID;
    private String almacenDescripcion;

    public ArticuloDomain(int inventariosArtID, int SKU, long UPC, String descripcion, Double ctdContada, double stockTotal, int ubicacionID, int usuarioID, String almacenDescripcion) {
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

    // Constructor sin parámetros para creación de objetos
    public ArticuloDomain() {
    }

    public int getInventariosArtID() { return inventariosArtID; }
    public int getSKU() { return SKU; }
    public long getUPC() { return UPC; }
    public String getDescripcion() { return descripcion; }
    public Double getCtdContada() { return ctdContada; }
    public double getStockTotal() { return stockTotal; }
    public int getUbicacionID() { return ubicacionID; }
    public int getUsuarioID() { return usuarioID; }
    public String getAlmacenDescripcion() {return  almacenDescripcion; }

    public void setInventariosArtID(int inventariosArtID) { this.inventariosArtID = inventariosArtID; }
    public void setSKU(int SKU) { this.SKU = SKU; }
    public void setUPC(long UPC) { this.UPC = UPC; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setCtdContada(Double ctdContada) { this.ctdContada = ctdContada; }
    public void setStockTotal(double stockTotal) { this.stockTotal = stockTotal; }
    public void setUbicacionID(int ubicacionID) { this.ubicacionID = ubicacionID; }
    public void setUsuarioID(int usuarioID) { this.usuarioID = usuarioID; }
    public void setAlmacenDescripcion(String almacenDescripcion) { this.almacenDescripcion = almacenDescripcion; }

}