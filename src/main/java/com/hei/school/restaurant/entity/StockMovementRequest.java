package com.hei.school.restaurant.entity;

import com.hei.school.restaurant.entity.enums.MovementTypeEnum;
import com.hei.school.restaurant.entity.enums.Unit;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString

public class StockMovementRequest {
    private Unit unit;
    private Double value;
    private MovementTypeEnum type;
}