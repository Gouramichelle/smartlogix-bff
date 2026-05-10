# SmartLogix BFF (Backend for Frontend)

## Descripción
El **Backend for Frontend (BFF)** es un microservicio intermediario que actúa como gateway entre el frontend React y los microservicios backend. Su propósito principal es:

- Unificar las llamadas a múltiples microservicios
- Enriquecer los datos antes de enviarlos al frontend
- Gestionar la lógica de presentación de datos
- Proporcionar endpoints REST simplificados para el frontend

## Tecnologías Utilizadas
- **Java 21**
- **Spring Boot 4.0.6**
- **Spring Web** para REST APIs
- **RestTemplate** para comunicación con otros MS
- **Maven** como gestor de dependencias

## Cómo Ejecutar

### Prerrequisitos
- Java 21 instalado
- Maven instalado
- Los microservicios de Inventario y Pedidos deben estar ejecutándose

### Pasos para Ejecutar
1. Navega al directorio del proyecto:
   ```bash
   cd smartlogix-bff/bff
   ```

2. Ejecuta la aplicación:
   ```bash
   ./mvnw spring-boot:run
   ```

3. La aplicación se ejecutará en: `http://localhost:8088`

### Configuración
- **Puerto**: 8088
- **Base URL**: `/api/bff`
- **Dependencias**:
  - MS-Inventario: `http://localhost:8085`
  - MS-Pedidos: `http://localhost:8086`

## Funcionalidades Específicas del BFF

Este BFF implementa el patrón **Backend for Frontend** de manera clásica, optimizando las respuestas específicamente para el consumo del frontend React. A continuación, se detallan las funcionalidades clave con ejemplos de código:

### 1. **Agregación y Enriquecimiento de Datos**
El BFF combina información de múltiples microservicios para proporcionar respuestas completas y listas para consumir.

**Ejemplo: Generación de Boleta Completa**
```java
public PedidoCompletoResponse obtenerBoletaCompleta(Long idPedido) {
    // 1. Obtiene pedido base del MS-Pedidos
    PedidoDTO pedido = restTemplate.getForObject(pedidosUrl + "/" + idPedido, PedidoDTO.class);
    
    // 2. Obtiene catálogo completo del MS-Inventario
    ProductoDTO[] catalogo = restTemplate.getForObject(inventarioUrl, ProductoDTO[].class);
    
    // 3. Enriquecer: cruzar SKU del pedido con precios del catálogo
    for (PedidoDTO.ItemPedidoDTO item : pedido.getItems()) {
        for (ProductoDTO prod : catalogo) {
            if (prod.getSku().equals(item.getSkuProducto())) {
                detalle.setPrecioUnitario(prod.getPrecio());
                total += prod.getPrecio() * item.getCantidad();
            }
        }
    }
    
    // 4. Retornar respuesta optimizada para frontend
    response.setTotalPedido(total);
    return response;
}
```

### 2. **Cálculo de Lógica de Presentación**
Realiza cálculos que el frontend no debería hacer, como totales y subtotales.

**Ejemplo: Lista de Pedidos con Totales**
```java
public PedidoDTO[] obtenerPedidosBff() {
    PedidoDTO[] pedidos = restTemplate.getForObject(pedidosUrl, PedidoDTO[].class);
    ProductoDTO[] catalogo = restTemplate.getForObject(inventarioUrl, ProductoDTO[].class);
    
    // Crear mapa de productos por SKU para búsqueda rápida
    Map<String, ProductoDTO> mapaProductos = Arrays.stream(catalogo)
        .collect(Collectors.toMap(ProductoDTO::getSku, Function.identity()));
    
    for (PedidoDTO pedido : pedidos) {
        double totalPedido = 0.0;
        if (pedido.getItems() != null) {
            for (PedidoDTO.ItemPedidoDTO item : pedido.getItems()) {
                ProductoDTO producto = mapaProductos.get(item.getSkuProducto());
                if (producto != null) {
                    item.setPrecioUnitario(producto.getPrecio());  // Enriquecer item
                    totalPedido += producto.getPrecio() * item.getCantidad();
                }
            }
        }
        pedido.setTotalPedido(totalPedido);  // Calcular total del pedido
    }
    
    return pedidos;
}
```

### 3. **Simplificación de APIs para el Frontend**
Oculta la complejidad de múltiples llamadas y proporciona endpoints intuitivos.

**Ejemplo: Creación de Pedido Simplificada**
```java
@PostMapping("/pedidos")
public ResponseEntity<?> crearPedido(@RequestBody Object pedidoRequest) {
    // El frontend envía un objeto simple, el BFF lo reenvía al MS-Pedidos
    Object nuevoPedido = bffService.crearPedido(pedidoRequest);
    return ResponseEntity.ok(nuevoPedido);
}
```

### 4. **Gestión de Estados y Respuestas Optimizadas**
Maneja estados de error y proporciona respuestas consistentes al frontend.

**Ejemplo: Actualización de Pedido con Respuesta Enriquecida**
```java
public PedidoCompletoResponse actualizarPedidoBff(Long idPedido, PedidoDTO pedidoNuevosDatos) {
    // 1. Actualizar en MS-Pedidos
    restTemplate.exchange(pedidosUrl + "/" + idPedido, HttpMethod.PUT, 
        new HttpEntity<>(pedidoNuevosDatos, headers), PedidoDTO.class);
    
    // 2. ¡Magia del BFF! Regenerar boleta completa con datos actualizados
    return obtenerBoletaCompleta(idPedido);
}
```

## ¿Por qué es un BFF y no un API Gateway?

- **Optimización para Frontend Específico**: Las respuestas están diseñadas específicamente para React (totales calculados, datos enriquecidos).
- **Lógica de Presentación**: Calcula totales, formatea datos, cruza información de múltiples fuentes.
- **Reducción de Llamadas**: El frontend hace 1 llamada al BFF en lugar de múltiples a diferentes MS.
- **Acoplamiento Controlado**: Conoce las necesidades específicas del frontend, violando intencionalmente la separación pura de microservicios para mejorar UX.

Este BFF reduce significativamente la complejidad del frontend, proporcionando datos listos para renderizar y minimizando la lógica de negocio en el cliente.
