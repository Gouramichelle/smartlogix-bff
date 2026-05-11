package com.smartlogix.bff.model;

import java.util.List;

import lombok.Data;

@Data
public class PedidoDTO {
    private Long id;
    private String estado;
    private String cliente;
    private List<ItemPedidoDTO> items;
    private Double totalPedido;

    @Data
    public static class ItemPedidoDTO {
        private String skuProducto;
        private Integer cantidad;
        private Double precioUnitario;
    }
}