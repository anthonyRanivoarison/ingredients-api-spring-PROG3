# Ingredients API – Spring Boot REST Service

This project is a **refactoring** of the previous JDBC-based console application [1] into a fully functional **Spring Boot REST API**. The main goals are:

- Apply the **Single Responsibility Principle (SRP)** by decomposing monolithic classes into dedicated layers
- Migrate from a console app to an **HTTP-accessible web service**
- Expose clean REST endpoints for `Ingredient`, `Dish`, and `StockMovement` resources

[1]: https://github.com/anthonyRanivoarison/ingredients-PROG3