package com.hei.school.restaurant.entity;

import com.hei.school.restaurant.entity.enums.CategoryEnum;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode

public class Ingredient {
    private Integer id;
    private String name;
    private CategoryEnum category;
    private Double price;
}
