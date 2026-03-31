package com.hei.school.restaurant.repository;

import com.hei.school.restaurant.entity.Dish;
import com.hei.school.restaurant.entity.Ingredient;
import com.hei.school.restaurant.entity.enums.CategoryEnum;
import com.hei.school.restaurant.entity.enums.DishTypeEnum;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DishRepository {

    private final DataSource dataSource;

    public DishRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Dish> findAll() {
        String sql = """
                SELECT dish.id AS dish_id, dish.name AS dish_name,
                       dish.dish_type, dish.selling_price AS dish_price
                FROM dish
                ORDER BY dish.id
                """;
        List<Dish> dishes = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Dish dish = mapRow(rs);
                dish.setIngredients(findIngredientsByDishId(conn, dish.getId()));
                dishes.add(dish);
            }
            return dishes;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Dish findById(Integer id) {
        String sql = """
                SELECT dish.id AS dish_id, dish.name AS dish_name,
                       dish.dish_type, dish.selling_price AS dish_price
                FROM dish
                WHERE dish.id = ?
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Dish dish = mapRow(rs);
                    dish.setIngredients(findIngredientsByDishId(conn, dish.getId()));
                    return dish;
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Dish updateIngredients(Integer dishId, List<Ingredient> ingredients) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            detachAllIngredients(conn, dishId);

            List<Ingredient> existingIngredients = filterExistingIngredients(conn, ingredients);

            attachIngredients(conn, dishId, existingIngredients);

            conn.commit();
            return findById(dishId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void detachAllIngredients(Connection conn, Integer dishId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM dish_ingredient WHERE id_dish = ?")) {
            ps.setInt(1, dishId);
            ps.executeUpdate();
        }
    }

    private List<Ingredient> filterExistingIngredients(Connection conn, List<Ingredient> candidates)
            throws SQLException {
        List<Ingredient> existing = new ArrayList<>();
        String sql = "SELECT id FROM ingredient WHERE id = ?";
        for (Ingredient candidate : candidates) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, candidate.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        existing.add(candidate);
                    }
                }
            }
        }
        return existing;
    }

    private void attachIngredients(Connection conn, Integer dishId, List<Ingredient> ingredients)
            throws SQLException {
        if (ingredients == null || ingredients.isEmpty()) return;
        String sql = """
                INSERT INTO dish_ingredient (id_ingredient, id_dish, required_quantity, unit)
                VALUES (?, ?, NULL, 'PCS'::unit)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Ingredient ingredient : ingredients) {
                ps.setInt(1, ingredient.getId());
                ps.setInt(2, dishId);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private List<Ingredient> findIngredientsByDishId(Connection conn, Integer dishId)
            throws SQLException {
        String sql = """
                SELECT ingredient.id, ingredient.name, ingredient.price, ingredient.category
                FROM ingredient
                JOIN dish_ingredient di ON di.id_ingredient = ingredient.id
                WHERE di.id_dish = ?
                """;
        List<Ingredient> ingredients = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dishId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ingredients.add(new Ingredient(
                            rs.getInt("id"),
                            rs.getString("name"),
                            CategoryEnum.valueOf(rs.getString("category")),
                            rs.getDouble("price")
                    ));
                }
            }
        }
        return ingredients;
    }

    private Dish mapRow(ResultSet rs) throws SQLException {
        Dish dish = new Dish();
        dish.setId(rs.getInt("dish_id"));
        dish.setName(rs.getString("dish_name"));
        dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
        dish.setUnitPrice(rs.getObject("dish_price") == null ? null : rs.getDouble("dish_price"));
        return dish;
    }
}
