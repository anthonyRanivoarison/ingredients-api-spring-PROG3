package com.hei.school.restaurant.repository;

import com.hei.school.restaurant.entity.StockMovement;
import com.hei.school.restaurant.entity.StockMovementRequest;
import com.hei.school.restaurant.entity.StockValue;
import com.hei.school.restaurant.entity.enums.MovementTypeEnum;
import com.hei.school.restaurant.entity.enums.Unit;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Repository
public class StockMovementRepository {

    private final DataSource dataSource;

    public StockMovementRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public StockValue getStockAt(Integer ingredientId, Instant at, Unit unit) {
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
                    return new StockValue(
                            rs.getDouble("quantity"),
                            Unit.valueOf(rs.getString("unit"))
                    );
                }
                return new StockValue(0.0, unit);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<StockMovement> findByIngredientIdBetween(Integer ingredientId, Instant from, Instant to) {
        String sql = """
                SELECT id, creation_datetime, unit, quantity, type
                FROM stock_movement
                WHERE id_ingredient = ?
                  AND creation_datetime >= ?
                  AND creation_datetime <= ?
                ORDER BY creation_datetime
                """;
        List<StockMovement> movements = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ingredientId);
            ps.setTimestamp(2, Timestamp.from(from));
            ps.setTimestamp(3, Timestamp.from(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    movements.add(new StockMovement(
                            rs.getInt("id"),
                            MovementTypeEnum.valueOf(rs.getString("type")),
                            rs.getTimestamp("creation_datetime").toInstant(),
                            new StockValue(rs.getDouble("quantity"), Unit.valueOf(rs.getString("unit")))
                    ));
                }
            }
            return movements;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<StockMovement> saveAll(Integer ingredientId, List<StockMovementRequest> requests) {
        String sql = """
                INSERT INTO stock_movement (id_ingredient, quantity, unit, type, creation_datetime)
                VALUES (?, ?, ?::unit, ?::movement_type, ?)
                RETURNING id, creation_datetime, unit, quantity, type
                """;
        List<StockMovement> created = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            Instant now = Instant.now();
            for (StockMovementRequest req : requests) {
                ps.setInt(1, ingredientId);
                ps.setDouble(2, req.getValue());
                ps.setString(3, req.getUnit().name());
                ps.setString(4, req.getType().name());
                ps.setTimestamp(5, Timestamp.from(now));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        created.add(new StockMovement(
                                rs.getInt("id"),
                                MovementTypeEnum.valueOf(rs.getString("type")),
                                rs.getTimestamp("creation_datetime").toInstant(),
                                new StockValue(rs.getDouble("quantity"), Unit.valueOf(rs.getString("unit")))
                        ));
                    }
                }
            }
            return created;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
