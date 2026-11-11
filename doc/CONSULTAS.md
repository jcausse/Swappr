Manejo de errores de service: catchear excepciones en el controller está mal, por qué?
El modo correcto de hacerlo es verificar precondiciones en un form validator, aunque implique otra llamada a service y transaction/conexión a la db?

---

Los service que buscan un objeto específico (e.g. getById o getByName) deberían tirar NotFoundException o retornar Optional y que tire la exception el controller?
Ahora tenemos las dos, hay que unificar. A cuál??

---

OK hacer el images create en un loop (`images.forEach(em::persist)`)? O hacemos una query para un multiple insert?

`ListingJpaDao.createAll`

---

OK traducir keys en un controller, si estamos armando un string más complejo que un solo i18n key?
O mejor pasar varios i18n en un objeto y hacerlo en JSP?

(subcategory select en admin panel)

---

`getListingInfoSummary` (`ProductJpaDao`):
  - Hay una mejor forma de hacerlo sin castear un `Object[]`?

Idem `countByStatus` (`OfferJpaDao`)

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
