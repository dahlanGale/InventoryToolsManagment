package com.ioe_enterprice.inventorytoolsmanagment.Domain;

public class UserDomain {
    private int usuarioID;
    private String nombre;
    private String rol;
    private String email;

    public UserDomain() {
    }

    public UserDomain(int usuarioID, String nombre, String rol, String email) {
        this.usuarioID = usuarioID;
        this.nombre = nombre;
        this.rol = rol;
        this.email = email;
    }

    public int getUsuarioID() {
        return usuarioID;
    }

    public void setUsuarioID(int usuarioID) {
        this.usuarioID = usuarioID;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
