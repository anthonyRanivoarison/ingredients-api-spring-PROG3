package com.hei.school.restaurant.repository;

import com.hei.school.restaurant.entity.StockMovement;
import com.hei.school.restaurant.entity.enums.Unit;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;

@Repository
public class StockMovementRepository {

    private final DataSource dataSource;

    public StockMovementRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public StockMovement getStockAt(Integer ingredientId, Instant at, Unit unit) {
        String sql = """
                SELECT stock_movement.unit,
                       SUM(
                           CASE
                               WHEN stock_movement.type = 'IN'  THEN stock_movement.quantity
                               WHEN stock_movement.type = 'OUT' THEN -stock_movement.quantity
                               ELSE 0
                           END
                       ) AS actual_quantity
                FROM stock_movement
                JOIN ingredient ON stock_movement.id_ingredient = ingredient.id
                WHERE ingredient.id = ?
                  AND creation_datetime <= ?
                  AND stock_movement.unit = ?::unit
                GROUP BY stock_movement.unit
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ingredientId);
            ps.setTimestamp(2, Timestamp.from(at));
            ps.setString(3, unit.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new StockMovement(
                            Unit.valueOf(rs.getString("unit")),
                            rs.getDouble("actual_quantity")
                    );
                }
                return new StockMovement(unit, 0.0);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
