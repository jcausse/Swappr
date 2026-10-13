Es necesario usar `GenerationType.SEQUENCE`? Con `GenerationType.IDENTITY` es automático y no se necesita 
el `@SequenceGenerator`

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

---

Tenemos el problema de las `n + 1` queries? Cual es la forma mas facil de identificarlas?
