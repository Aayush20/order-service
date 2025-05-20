package org.example.orderservice.clients;

import org.example.orderservice.dtos.RollbackStockRequestDto;
import org.example.orderservice.dtos.RollbackStockRequestDto.ProductRollbackEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.mockito.Mockito.*;

class InventoryClientTest {

    @Mock private RestTemplate restTemplate;
    @InjectMocks private InventoryClient inventoryClient;

    private final String url = "http://prod-cat-service/internal/rollback-stock";

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        inventoryClient = new InventoryClient(restTemplate);
    }

    @Test
    void shouldCallRollbackStockSuccessfully() {
        RollbackStockRequestDto dto = new RollbackStockRequestDto(
                List.of(new ProductRollbackEntry(1L, 3)),
                "unit test"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RollbackStockRequestDto> requestEntity = new HttpEntity<>(dto, headers);

        when(restTemplate.postForEntity(eq(url), eq(requestEntity), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        inventoryClient.rollbackStock(dto);

        verify(restTemplate).postForEntity(eq(url), eq(requestEntity), eq(Void.class));
    }

    @Test
    void shouldThrowExceptionOnHttpFailure() {
        RollbackStockRequestDto dto = new RollbackStockRequestDto(
                List.of(new ProductRollbackEntry(2L, 5)),
                "simulate failure"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RollbackStockRequestDto> requestEntity = new HttpEntity<>(dto, headers);

        HttpStatusCodeException ex = mock(HttpStatusCodeException.class);
        when(ex.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(ex.getResponseBodyAsString()).thenReturn("Invalid input");

        when(restTemplate.postForEntity(eq(url), eq(requestEntity), eq(Void.class)))
                .thenThrow(ex);

        try {
            inventoryClient.rollbackStock(dto);
        } catch (Exception e) {
            verify(restTemplate).postForEntity(eq(url), eq(requestEntity), eq(Void.class));
        }
    }
}
