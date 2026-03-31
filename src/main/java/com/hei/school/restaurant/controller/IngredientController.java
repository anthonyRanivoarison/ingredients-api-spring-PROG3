package com.hei.school.restaurant.controller;

import com.hei.school.restaurant.entity.Ingredient;
import com.hei.school.restaurant.entity.StockMovement;
import com.hei.school.restaurant.entity.enums.Unit;
import com.hei.school.restaurant.repository.IngredientRepository;
import com.hei.school.restaurant.repository.StockMovementRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

        Instant instant = Instant.parse(at);
        Unit stockUnit = Unit.valueOf(unit.toUpperCase());

        StockMovement stock = stockMovementRepository.getStockAt(id, instant, stockUnit);
        return ResponseEntity.ok(stock);
    }
}
