/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyectojaime;

/**
 * Describe un campo del registro
 * @author xaime
 */
class CampoDefinicion {
    
    private String nombre;
    private TipoCampo tipoCampo;
    private int longitud; // Solamente aplica a cadena

    public CampoDefinicion(String nombre, TipoCampo tipoCampo, int longitud) {
        this.nombre = nombre;
        this.tipoCampo = tipoCampo;
        this.longitud = longitud;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoCampo getTipoCampo() {
        return tipoCampo;
    }

    public int getLongitud() {
        return longitud;
    }
    
}
