package com.smartlogix.bff.model;

import java.util.List;

import lombok.Data;

@Data
public class PedidoCompletoResponse {
    private Long idPedido;
    private String estado;
    private List<DetalleItem> detalles;
    private Double totalPedido;

    @Data
    public static class DetalleItem {
        private String producto; // Nombre real del producto
        private Integer cantidad;
        private Double precioUnitario;
        private Double subtotal;
    }
}