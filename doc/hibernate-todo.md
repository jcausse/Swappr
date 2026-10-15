1. Hacer que la relacion entre Listings e Images no use `@JoinColumn`.
   Modelar la tabla intermedia y hacer que se fetchee `LAZY` la `Image` en `ListingImage`.

2. Poner en scope `test` todas las dependencias de JDBC.

3. Arreglar el `populateTransientFields` para usar correctamente JPQL.

4. Revisar los `ON DELETE` y `ON INSERT`.
