package com.smartlogix.bff.service;


import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.smartlogix.bff.model.PedidoCompletoResponse;
import com.smartlogix.bff.model.PedidoDTO;
import com.smartlogix.bff.model.ProductoDTO;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class BffService {

    private final RestTemplate restTemplate;

   

    // Inyectamos las URLs desde el application.properties
    @Value("${url.ms.pedidos}")
    private String pedidosUrl;

    @Value("${url.ms.inventario}")
    private String inventarioUrl;

    public PedidoCompletoResponse obtenerBoletaCompleta(Long idPedido) {
        // 1. Llamamos al MS-Pedidos para obtener el pedido base
        PedidoDTO pedido = restTemplate.getForObject(pedidosUrl + "/" + idPedido, PedidoDTO.class);
        
        if (pedido == null) throw new RuntimeException("Pedido no encontrado");

        // 2. Traemos todo el catálogo del MS-Inventario
        ProductoDTO[] catalogo = restTemplate.getForObject(inventarioUrl, ProductoDTO[].class);

        // 3. Armamos la respuesta enriquecida para el Frontend
        PedidoCompletoResponse response = new PedidoCompletoResponse();
        response.setIdPedido(pedido.getId());
        response.setEstado(pedido.getEstado());
        
        List<PedidoCompletoResponse.DetalleItem> detalles = new ArrayList<>();
        double total = 0.0;

        // 4. Cruzamos los datos (SKU del pedido vs SKU del catálogo)
        for (PedidoDTO.ItemPedidoDTO item : pedido.getItems()) {
            PedidoCompletoResponse.DetalleItem detalle = new PedidoCompletoResponse.DetalleItem();
            detalle.setCantidad(item.getCantidad());
            
            // Buscamos el nombre y precio real en el catálogo
            if (catalogo != null) {
                for (ProductoDTO prod : catalogo) {
                    if (prod.getSku().equals(item.getSkuProducto())) {
                        detalle.setProducto(prod.getNombre());
                        detalle.setPrecioUnitario(prod.getPrecio());
                        detalle.setSubtotal(prod.getPrecio() * item.getCantidad());
                        total += detalle.getSubtotal();
                        break;
                    }
                }
            }
            detalles.add(detalle);
        }

        response.setDetalles(detalles);
        response.setTotalPedido(total);

        return response;
    }
    // Método para modificar un pedido a través del BFF
    public PedidoCompletoResponse actualizarPedidoBff(Long idPedido, PedidoDTO pedidoNuevosDatos) {
        // 1. Preparamos la petición HTTP para enviar el JSON al MS-Pedidos
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PedidoDTO> requestEntity = new HttpEntity<>(pedidoNuevosDatos, headers);

        // 2. Hacemos el PUT al MS-Pedidos (Puerto 8086)
        restTemplate.exchange(
                pedidosUrl + "/" + idPedido,
                HttpMethod.PUT,
                requestEntity,
                PedidoDTO.class
        );

        // 3. ¡La magia del BFF! En lugar de devolver un simple "OK", 
        // volvemos a generar la boleta completa con los datos recién actualizados
        return obtenerBoletaCompleta(idPedido);
    }

    // Método para eliminar un pedido a través del BFF
    public void eliminarPedidoBff(Long idPedido) {
        // Simplemente le pasamos la orden de eliminar al MS-Pedidos
        restTemplate.delete(pedidosUrl + "/" + idPedido);
    }
    // --- MÉTODOS PROXY PARA INVENTARIO ---

    public ProductoDTO[] obtenerProductosBff() {
        // Pide los productos al 8081 y los devuelve
        return restTemplate.getForObject(inventarioUrl, ProductoDTO[].class);
    }

    public ProductoDTO crearProductoBff(ProductoDTO producto) {
        // Envía el POST al 8081
        return restTemplate.postForObject(inventarioUrl, producto, ProductoDTO.class);
    }

    public void actualizarProductoBff(String id, ProductoDTO producto) {
        // Envía el PUT al 8081
        restTemplate.put(inventarioUrl + "/" + id, producto);
    }

    public void eliminarProductoBff(String id) {
        // Envía el DELETE al 8081
        restTemplate.delete(inventarioUrl + "/" + id);
    }
    public Object crearPedido(Object pedidoRequest) {
        try {
            // postForObject envía el body (pedidoRequest) a la URL y devuelve la respuesta
            Object pedidoCreado = restTemplate.postForObject(pedidosUrl, pedidoRequest, Object.class);
            return pedidoCreado;
        } catch (Exception e) {
            throw new RuntimeException("Error al comunicarse con MS-Pedidos: " + e.getMessage());
        }
    }
    // Listar todos los pedidos para la tabla de gestión
    public PedidoDTO[] obtenerPedidosBff() {
        return restTemplate.getForObject(pedidosUrl, PedidoDTO[].class);
    }

    // Actualizar un pedido existente
    public void actualizarPedidoBff(String id, PedidoDTO pedido) {
        restTemplate.put(pedidosUrl + "/" + id, pedido);
    }

    // Eliminar un pedido (opcional pero útil)
    public void eliminarPedidoBff(String id) {
        restTemplate.delete(pedidosUrl + "/" + id);
    }
}