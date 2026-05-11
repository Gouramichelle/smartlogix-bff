package com.smartlogix.bff;

import com.smartlogix.bff.model.PedidoCompletoResponse;
import com.smartlogix.bff.model.PedidoDTO;
import com.smartlogix.bff.model.ProductoDTO;
import com.smartlogix.bff.service.BffService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

// Patrón BFF (Backend For Frontend): agrega datos de MS-Inventario y MS-Pedidos
// en una sola respuesta adaptada para el cliente React.
// ReflectionTestUtils inyecta los @Value sin levantar Spring Context.
@ExtendWith(MockitoExtension.class)
class BffServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private BffService bffService;

    private static final String INVENTARIO_URL = "http://localhost:8081/api/inventario/productos";
    private static final String PEDIDOS_URL = "http://localhost:8086/api/pedidos";

    private ProductoDTO productoDTO;
    private PedidoDTO pedidoDTO;

    @BeforeEach
    void setUp() {
        // Inyectamos los @Value manualmente (sin levantar Spring Context)
        ReflectionTestUtils.setField(bffService, "inventarioUrl", INVENTARIO_URL);
        ReflectionTestUtils.setField(bffService, "pedidosUrl", PEDIDOS_URL);

        productoDTO = new ProductoDTO();
        productoDTO.setSku("SKU-001");
        productoDTO.setNombre("Camiseta");
        productoDTO.setPrecio(9990.0);
        productoDTO.setStock(50);

        PedidoDTO.ItemPedidoDTO item = new PedidoDTO.ItemPedidoDTO();
        item.setSkuProducto("SKU-001");
        item.setCantidad(2);

        pedidoDTO = new PedidoDTO();
        pedidoDTO.setId(1L);
        pedidoDTO.setCliente("Cliente Web");
        pedidoDTO.setEstado("APROBADO");
        pedidoDTO.setItems(List.of(item));
    }

    @Test
    @DisplayName("obtenerBoletaCompleta() cruza pedido con inventario y calcula total")
    void obtenerBoletaCompleta_calculaTotalCorrectamente() {

        when(restTemplate.getForObject(PEDIDOS_URL + "/1", PedidoDTO.class))
                .thenReturn(pedidoDTO);
        when(restTemplate.getForObject(INVENTARIO_URL, ProductoDTO[].class))
                .thenReturn(new ProductoDTO[] { productoDTO });

        // el BFF agrega ambas fuentes (patrón agregación)
        PedidoCompletoResponse boleta = bffService.obtenerBoletaCompleta(1L);

        assertThat(boleta.getIdPedido()).isEqualTo(1L);
        assertThat(boleta.getCliente()).isEqualTo("Cliente Web");
        // 2 unidades × $9.990 = $19.980
        assertThat(boleta.getTotalPedido()).isEqualTo(19980.0);
        assertThat(boleta.getDetalles()).hasSize(1);
        assertThat(boleta.getDetalles().get(0).getProducto()).isEqualTo("Camiseta");
    }

    @Test
    @DisplayName("obtenerBoletaCompleta() lanza excepción si MS-Pedidos retorna null")
    void obtenerBoletaCompleta_pedidoNull_lanzaExcepcion() {

        when(restTemplate.getForObject(PEDIDOS_URL + "/99", PedidoDTO.class))
                .thenReturn(null);

        // el BFF debe lanzar error claro, no NullPointerException silencioso
        assertThatThrownBy(() -> bffService.obtenerBoletaCompleta(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no encontrado");
    }

    @Test
    @DisplayName("obtenerProductosBff() retorna array de productos desde MS-Inventario")
    void obtenerProductosBff_retornaProductos() {

        when(restTemplate.getForObject(INVENTARIO_URL, ProductoDTO[].class))
                .thenReturn(new ProductoDTO[] { productoDTO });

        var resultado = bffService.obtenerProductosBff();

        assertThat(resultado).hasSize(1);
        assertThat(resultado[0].getSku()).isEqualTo("SKU-001");
    }

    @Test
    @DisplayName("obtenerProductosBff() retorna null si MS-Inventario responde null")
    void obtenerProductosBff_respuestaNula_retornaNull() {
        // simula microservicio caído o sin productos
        when(restTemplate.getForObject(INVENTARIO_URL, ProductoDTO[].class))
                .thenReturn(null);

        var resultado = bffService.obtenerProductosBff();

        // el método retorna directamente lo que recibe del microservicio
        assertThat(resultado).isNull();
    }
}
