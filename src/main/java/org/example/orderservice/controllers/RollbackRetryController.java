package org.example.orderservice.controllers;

import org.example.orderservice.dtos.TokenIntrospectionResponseDTO;
import org.example.orderservice.services.InventoryRollbackService;
import org.example.orderservice.services.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order/internal")
public class RollbackRetryController {

    private final InventoryRollbackService rollbackService;
    private final TokenService tokenService;

    public RollbackRetryController(InventoryRollbackService rollbackService, TokenService tokenService) {
        this.rollbackService = rollbackService;
        this.tokenService = tokenService;
    }

    @PostMapping("/rollback-retry")
    public ResponseEntity<String> retryRollbackTasks(@RequestHeader("Authorization") String authHeader) {
        TokenIntrospectionResponseDTO token = tokenService.introspect(authHeader);
        if (!token.getScopes().contains("internal")) {
            return ResponseEntity.status(403).body("Forbidden");
        }

        rollbackService.retryFailedTasks(); // ✅ This exists
        return ResponseEntity.ok("Retry triggered");
    }
}
