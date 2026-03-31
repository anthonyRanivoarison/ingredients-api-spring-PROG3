package com.hei.school.restaurant.controller;

import com.hei.school.restaurant.entity.Dish;
import com.hei.school.restaurant.entity.Ingredient;
import com.hei.school.restaurant.repository.DishRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dishes")
public class DishController {

    private final DishRepository dishRepository;

    public DishController(DishRepository dishRepository) {
        this.dishRepository = dishRepository;
    }

    @GetMapping
    public List<Dish> getAll() {
        return dishRepository.findAll();
    }

    @PutMapping("/{id}/ingredients")
    public ResponseEntity<?> updateIngredients(@PathVariable Integer id, @RequestBody(required = false) List<Ingredient> ingredients) {
        if (ingredients == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Request body is required and must contain a list of ingredients.");
        }

        Dish dish = dishRepository.findById(id);
        if (dish == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Dish.id=" + id + " is not found");
        }

        Dish updated = dishRepository.updateIngredients(id, ingredients);
        return ResponseEntity.ok(updated);
    }
}
