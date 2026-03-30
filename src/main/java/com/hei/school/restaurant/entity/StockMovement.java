package com.hei.school.restaurant.entity;

import com.hei.school.restaurant.entity.enums.Unit;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode

public class StockMovement {
    private Unit unit;
    private Double value;
}
