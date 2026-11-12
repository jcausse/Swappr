Manejo de errores de service: catchear excepciones en el controller está mal, por qué?
El modo correcto de hacerlo es verificar precondiciones en un form validator, aunque implique otra llamada a service y transaction/conexión a la db?

> RTA: OK catchear en el controller si son excepciones de negocio. El bean validation está más para cosas simples, si hace falta ir al service/DB está OK que falle en el service.

---

Los service que buscan un objeto específico (e.g. getById o getByName) deberían tirar NotFoundException o retornar Optional y que tire la exception el controller?
Ahora tenemos las dos, hay que unificar. A cuál??

> RTA: El service decide si es excepcional o no que falte un valor; depende de la lógica de negocio.
        Si siempre es requerido, mejor que el service tire exception.
        Si depende de quién lo llame, mejor que retorne optional. En el caso que la ruta dependa del objeto está OK tirar exception en el controller.
        NUNCA hay que tirar exception del DAO!! Es lógica de negocio.

---

OK hacer el images create en un loop (`images.forEach(em::persist)`)? O hacemos una query para un multiple insert?

`ListingJpaDao.createAll`

> RTA: Insert en loop no es tan grave como read en loop, no es n+1 porque está dentro de una transaction.

---

OK traducir keys en un controller, si estamos armando un string más complejo que un solo i18n key?
O mejor pasar varios i18n en un objeto y hacerlo en JSP?

(subcategory select en admin panel)

> RTA: Cambiar por selects de category->subcategory como en otras páginas

---

`getListingInfoSummary` (`ProductJpaDao`):
  - Hay una mejor forma de hacerlo sin castear un `Object[]`?

Idem `countByStatus` (`OfferJpaDao`)

> RTA: Se pueden tipar los queries
> NOTA: cambiar native queries por jpql donde sea posible
> NOTA: se puede ordenar por orden del enum (ORDER BY CASE)

---

Tenemos el problema de las `n + 1` queries? Cual es la forma mas facil de identificarlas?

---

Es necesario usar `GenerationType.SEQUENCE`? Con `GenerationType.IDENTITY` es automático y no se necesita 
el `@SequenceGenerator`

> **RTA:** Se puede usar, no pasa nada.

---

Esta bien este `filter` en `web.xml`?

```xml
    <!-- Open EntityManager in View Filter -->
    <!-- TODO: Sacar esto para el final -->
    <filter>
        <filter-name>openEntityManagerInViewFilter</filter-name>
        <filter-class>org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter</filter-class>
    </filter>
    
    <filter-mapping>
    <filter-name>openEntityManagerInViewFilter</filter-name>
    <url-pattern>/*</url-pattern>
    </filter-mapping>
  ```

Esto fue necesario para que el `EntityManager` se mantuviera mientras los JSP estan renderizando, porque acceden a 
propiedades que son _lazy-loaded_ de los modelos, causando `LazyInitializationException`.

> **RTA:** No es un problema. Sacarlo para el final.

---
