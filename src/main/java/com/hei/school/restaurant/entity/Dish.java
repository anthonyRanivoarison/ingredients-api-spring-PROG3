package com.hei.school.restaurant.entity;

import com.hei.school.restaurant.entity.enums.DishTypeEnum;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode

public class Dish {
    private Integer id;
    private String name;
    private Double unitPrice;
    private DishTypeEnum dishType;
    private List<Ingredient> ingredients;
}
