package com.smartlogix.bff.model;
import lombok.Data;

@Data
public class ProductoDTO {
    private String sku;
    private String nombre;
    private Double precio;
}