package com.smartlogix.bff.model;

import java.util.List;

import lombok.Data;

@Data
public class PedidoDTO {
    private Long id;
    private String estado;
    private List<ItemPedidoDTO> items;

    @Data
    public static class ItemPedidoDTO {
        private String skuProducto;
        private Integer cantidad;
    }
}