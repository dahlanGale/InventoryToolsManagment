package com.ioe_enterprice.inventorytoolsmanagment.Domain;

/**
 * Clase de dominio para representar almacenes
 */
public class AlmacenDomain {
    private int almacenID;
    private String almacenDescripcion;
    private int ubicacionID;
    private int sucursalID;
    private boolean activo;

    // Constructor completo
    public AlmacenDomain(int almacenID, String almacenDescripcion, int ubicacionID, int sucursalID, boolean activo) {
        this.almacenID = almacenID;
        this.almacenDescripcion = almacenDescripcion;
        this.ubicacionID = ubicacionID;
        this.sucursalID = sucursalID;
        this.activo = activo;
    }

    // Constructor mínimo necesario para el spinner
    public AlmacenDomain(int almacenID, String almacenDescripcion, int ubicacionID) {
        this.almacenID = almacenID;
        this.almacenDescripcion = almacenDescripcion;
        this.ubicacionID = ubicacionID;
        this.activo = true;
    }

    // Getters y setters
    public int getAlmacenID() {
        return almacenID;
    }

    public void setAlmacenID(int almacenID) {
        this.almacenID = almacenID;
    }

    public String getAlmacenDescripcion() {
        return almacenDescripcion;
    }

    public void setAlmacenDescripcion(String almacenDescripcion) {
        this.almacenDescripcion = almacenDescripcion;
    }

    public int getUbicacionID() {
        return ubicacionID;
    }

    public void setUbicacionID(int ubicacionID) {
        this.ubicacionID = ubicacionID;
    }

    public int getSucursalID() {
        return sucursalID;
    }

    public void setSucursalID(int sucursalID) {
        this.sucursalID = sucursalID;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    // Para mostrar el nombre del almacén en el Spinner
    @Override
    public String toString() {
        return almacenDescripcion;
    }
}
