package com.ioe_enterprice.inventorytoolsmanagment.Domain;

public class InventariosCabecera {
    private int inventarioDocID;
    private String inventarioFolio;
    private String tipoInventario;
    private double cantidadContada;
    private double costoTotal;

    public InventariosCabecera(int inventarioDocID, String inventarioFolio, String tipoInventario, double cantidadContada, double costoTotal){
        this.inventarioDocID = inventarioDocID;
        this.inventarioFolio = inventarioFolio;
        this.tipoInventario = tipoInventario;
        this.cantidadContada = cantidadContada;
        this.costoTotal = costoTotal;
    }

    public int getInventarioDocID() { return inventarioDocID; }
    public String getInventarioFolio() { return inventarioFolio; }
    public String getTipoInventario() { return tipoInventario; }
    public double getCantidadContada() { return cantidadContada; }
    public double getCostoTotal() { return costoTotal; }
}