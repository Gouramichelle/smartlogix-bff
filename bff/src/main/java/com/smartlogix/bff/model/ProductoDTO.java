package com.smartlogix.bff.model;
import lombok.Data;

@Data
public class ProductoDTO {
   private String id; 
    private String sku;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Integer stock;
}