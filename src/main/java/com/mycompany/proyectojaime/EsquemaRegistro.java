/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyectojaime;

import java.util.List;

/**
 * Esquema de un registro: nombre de entidad y lista de campos en orden
 * @author xaime
 */
public class EsquemaRegistro {
    
    private String nombreEntidad;
    private List<CampoDefinicion> campos;
    
    public EsquemaRegistro(){}

    public EsquemaRegistro(String nombreEntidad, List<CampoDefinicion> campos) {
        this.nombreEntidad = nombreEntidad;
        this.campos = campos;
    }

    public String getNombreEntidad() {
        return nombreEntidad;
    }

    public List<CampoDefinicion> getCampos() {
        return campos;
    }
    
    /**
     * Devuelve una definición de campo por nombre ignorando mayúsculas y minúsculas
     * @param nombre
     * @return definición
     */
    public CampoDefinicion obtenerCampoPorNombre(String nombre) {
        
        for(CampoDefinicion c : campos) {
            
            if(c.getNombre().equalsIgnoreCase(nombre)) {
                
                return c;
                
            }
            
        }
        
        return null;
        
    }
    
    public int tamanoRegistroFijo() {
        
        int tam = 0;
        
        for(CampoDefinicion c : campos) {
            
            if (c.getTipoCampo() == TipoCampo.ENTERO) {
                
                tam += 4;
                
            } else if (c.getTipoCampo() == TipoCampo.CADENA) {
                
                tam += c.getLongitud();
                
            }
            
        }
        
        return tam;
        
    }
    
}
