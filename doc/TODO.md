# ToDo List

## Code Review Feedback

- [ ] Hacer que la relacion entre Listings e Images no use `@JoinColumn`. Modelar la tabla intermedia y hacer que se fetchee `LAZY` la `Image` en `ListingImage`.

- [ ] Poner en scope `test` todas las dependencias de JDBC.

- [ ] Arreglar el `populateTransientFields` para usar correctamente JPQL.

- [ ] Revisar los `ON DELETE` y `ON INSERT`.

## Fixes

- [ ] `Bad Request` con formato numérico en checkout. Sotuyo probó hacer una oferta de menor valor utilizando el mismo formato numérico que se muestra en el precio y le dio un `400`.

## Minor Changes

- [ ] Hacer que los _forms_ tengan un `.todto()` para evitar instanciar todo el tiempo los DTOs para pasar a los servicios.
