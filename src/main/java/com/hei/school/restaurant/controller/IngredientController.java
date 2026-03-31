package com.hei.school.restaurant.controller;

import com.hei.school.restaurant.entity.Ingredient;
import com.hei.school.restaurant.entity.StockMovement;
import com.hei.school.restaurant.entity.StockMovementRequest;
import com.hei.school.restaurant.entity.StockValue;
import com.hei.school.restaurant.entity.enums.Unit;
import com.hei.school.restaurant.repository.IngredientRepository;
import com.hei.school.restaurant.repository.StockMovementRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/ingredients")
public class IngredientController {

    private final IngredientRepository ingredientRepository;
    private final StockMovementRepository stockMovementRepository;

    public IngredientController(IngredientRepository ingredientRepository, StockMovementRepository stockMovementRepository) {
        this.ingredientRepository = ingredientRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @GetMapping
    public List<Ingredient> getAll() {
        return ingredientRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable int id) {
        Ingredient ingredient = ingredientRepository.findById(id);
        if (ingredient == null) {
            return ResponseEntity.status(404)
                    .body("Ingredient.id=" + id + " is not found");
        }
        return ResponseEntity.ok(ingredient);
    }

    @GetMapping("/{id}/stock")
    public ResponseEntity<?> getStock(@PathVariable Integer id, @RequestParam(required = false) String at, @RequestParam(required = false) String unit) {
        if (at == null || unit == null) {
            return ResponseEntity.status(400)
                    .body("Either query parameter `at` or `unit` is not provided.");
        }

        Ingredient ingredient = ingredientRepository.findById(id);
        if (ingredient == null) {
            return ResponseEntity.status(404)
                    .body("Ingredient.id=" + id + " is not found");
        }

        StockValue stock = stockMovementRepository.getStockAt(id, Instant.parse(at), Unit.valueOf(unit.toUpperCase()));
        return ResponseEntity.ok(stock);
    }

    @GetMapping("/{id}/stockMovements")
    public ResponseEntity<?> getStockMovements(@PathVariable Integer id, @RequestParam String from, @RequestParam String to) {
        Ingredient ingredient = ingredientRepository.findById(id);
        if (ingredient == null) {
            return ResponseEntity.status(404)
                    .body("Ingredient.id=" + id + " is not found");
        }

        List<StockMovement> movements = stockMovementRepository.findByIngredientIdBetween(id, Instant.parse(from), Instant.parse(to));
        return ResponseEntity.ok(movements);
    }

    @PostMapping("/{id}/stockMovements")
    public ResponseEntity<?> createStockMovements(@PathVariable Integer id, @RequestBody(required = false) List<StockMovementRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return ResponseEntity.status(400)
                    .body("Request body is required and must contain a list of stock movements.");
        }
        Ingredient ingredient = ingredientRepository.findById(id);
        if (ingredient == null) {
            return ResponseEntity.status(404)
                    .body("Ingredient.id=" + id + " is not found");
        }

        List<StockMovement> created = stockMovementRepository.saveAll(id, requests);
        return ResponseEntity.ok(created);
    }
}
