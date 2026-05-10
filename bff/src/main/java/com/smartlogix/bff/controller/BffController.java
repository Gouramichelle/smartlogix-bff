package com.smartlogix.bff.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartlogix.bff.model.PedidoCompletoResponse;
import com.smartlogix.bff.model.PedidoDTO;
import com.smartlogix.bff.model.ProductoDTO;
import com.smartlogix.bff.service.BffService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bff")
@CrossOrigin(origins = "*") 
@RequiredArgsConstructor
public class BffController {

    private final BffService bffService;

    @GetMapping("/boleta/{idPedido}")
    public ResponseEntity<PedidoCompletoResponse> obtenerResumenPedido(@PathVariable Long idPedido) {
        try {
            return ResponseEntity.ok(bffService.obtenerBoletaCompleta(idPedido));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    @PutMapping("/pedidos/{idPedido}")
    public ResponseEntity<PedidoCompletoResponse> editarPedido(@PathVariable Long idPedido, @RequestBody PedidoDTO pedidoNuevosDatos) {
        try {
            PedidoCompletoResponse boletaActualizada = bffService.actualizarPedidoBff(idPedido, pedidoNuevosDatos);
            return ResponseEntity.ok(boletaActualizada);
        } catch (Exception e) {
            // Si el MS-Pedidos lanza error (ej. falta de stock o pedido no existe)
            return ResponseEntity.badRequest().build(); 
        }
    }

    // Endpoint para que React elimine un pedido
    @DeleteMapping("/pedidos/{idPedido}")
    public ResponseEntity<Void> borrarPedido(@PathVariable Long idPedido) {
        try {
            bffService.eliminarPedidoBff(idPedido);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/productos")
    public ResponseEntity<ProductoDTO[]> listarProductos() {
        return ResponseEntity.ok(bffService.obtenerProductosBff());
    }

    @PostMapping("/productos")
    public ResponseEntity<ProductoDTO> crearProducto(@RequestBody ProductoDTO producto) {
        try {
            return ResponseEntity.ok(bffService.crearProductoBff(producto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/productos/{id}")
    public ResponseEntity<Void> actualizarProducto(@PathVariable String id, @RequestBody ProductoDTO producto) {
        bffService.actualizarProductoBff(id, producto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/productos/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable String id) {
        bffService.eliminarProductoBff(id);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/pedidos")
    public ResponseEntity<PedidoDTO[]> listarPedidos() {
        return ResponseEntity.ok(bffService.obtenerPedidosBff());
    }
    @PostMapping("/pedidos")
    public ResponseEntity<?> crearPedido(@RequestBody Object pedidoRequest) {
        try {
            // Llama a tu servicio para que procese y envíe el pedido al MS-Pedidos
            Object nuevoPedido = bffService.crearPedido(pedidoRequest);
            return ResponseEntity.ok(nuevoPedido);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error procesando el pedido: " + e.getMessage());
        }
    }
}