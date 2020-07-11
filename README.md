## ORMX

Convertir datos entre el sistema de tipos utilizado en un lenguaje de programación orientado a objetos

## Ejemplos

Simple CRUD

```java
OrmDataBase db = DB.punto_venta;
    
Producto p = new Producto();
p.codigo = "COD-001";
p.nombre = "Test";
db.insert(p); // INSERT ...
    
p = db.queryBuilder()
        .where("id", "=", p.id)
        .get_first(Producto.class); // SELECT * FROM ... WHERE id = ?
    
System.out.println(p);
    
p.nombre = "001";
db.update(p); // UPDATE ...
    
db.delete(p); // DELETE ...
    
List<Producto> result = db.queryBuilder()
        .limit(25, 7)
        .get_list(Producto.class); // SELECT * FROM ... LIMIT 25, 1
    
for (Producto it : result) {
    System.out.println(it);
}
    
db.close();
```

Consola:
```markdown
#OrmDataBase: LOAD driver com.mysql.jdbc.Driver
#OrmDataBase: OPEN jdbc:mysql://localhost:3306/punto_venta;user=root
#OrmDataBase: INSERT INTO producto(codigo, nombre, fecha_creacion) VALUES(?, ?, ?); [COD-001, Test, Sun Jan 19 17:53:10 CST 2020]
#OrmDataBase: SELECT * FROM producto WHERE id = ?; [110]
Producto{id=110, codigo=COD-001, nombre=Test, fecha_creacion=2020-01-19 17:53:10.0, none=null}
#OrmDataBase: UPDATE producto SET codigo=?, nombre=?, fecha_creacion=? WHERE id = ?; [COD-001, 001, 2020-01-19 17:53:10.0, 110]
#OrmDataBase: DELETE FROM producto WHERE id = ?; [110]
#OrmDataBase: SELECT * FROM producto LIMIT 25, 7
Producto{id=1, codigo=E18-001, nombre=E-18-001, fecha_creacion=2020-01-19 17:00:10.0, none=null}
Producto{id=2, codigo=E18-002, nombre=E-18-002, fecha_creacion=2020-01-19 17:00:10.0, none=null}
#OrmDataBase: CLOSE jdbc:mysql://localhost:3306/punto_venta;user=root
```

### Config database

```java
public class DB {

  public static final OrmDataBase punto_venta = new OrmDataBase()
        .setDriverClassName("com.mysql.jdbc.Driver")
        .setUrl("jdbc:mysql://localhost:3306/punto_venta")
        .setUsername(/*"usuario"*/"root")
        .setPassword(/*"password"*/"")
        .setDebug(true);
}
```

### Define model

```java
@TableInfo(name = "producto")
public class Producto {

  @ColumnInfo(primaryKey = true, autoIncrement = true)
  public long id;
  
  @ColumnInfo
  public String codigo;
  
  @ColumnInfo
  public String nombre;
  
  @ColumnInfo
  public Date fecha_creacion = new Date();

  public String none;

  @Override public String toString() {
    return "Producto{" + "id=" + id + ", codigo=" + codigo + ", nombre=" + nombre + ", fecha_creacion=" + fecha_creacion + ", none=" + none + '}';
  }
}
```

### Simple DAO

```java
Producto p = new Producto();
p.id = 1000L;
p.codigo = Long.toHexString(System.currentTimeMillis()).toUpperCase();
p.nombre = "Producto " + System.currentTimeMillis();
Producto.dao().insert(p); // INSERT
    
p = Producto.dao().findById(p.id);
System.out.println(p); // SELECT ... FROM ... WHERE id = ?
    
p.nombre = "Papas frias";
Producto.dao().update(p); // UPDATE
    
Producto.dao().delete(p); // DELETE
    
OrmResultSet result = Producto.dao().queryBuilder()
        .order_by("fecha_creacion", "DESC").order_by("nombre", "DESC")
        .limit(25, 7)
        .get(); // SELECT [columns] FROM [table] ORDER BY fecha_creacion DESC, nombre DESC LIMIT 25, 7
    
for (Producto producto : result.it(Producto.class)) {
  System.out.println(producto);
}
    
System.out.println(Producto.dao().count()); // SELECT COUNT(id) AS total_rows FROM producto

Producto.dao().close();
```

Consola:
```markdown
#OrmDataBase: LOAD driver com.mysql.jdbc.Driver
#OrmDataBase: OPEN jdbc:mysql://localhost:3306/punto_venta;user=root
#OrmDataBase: INSERT INTO producto(codigo, nombre, fecha_creacion) VALUES(?, ?, ?); [16FC046E139, Producto 1579478868281, Sun Jan 19 18:07:48 CST 2020]
#OrmDataBase: SELECT id, codigo, nombre, fecha_creacion FROM producto WHERE id = ?; [111]
Producto{id=111, codigo=16FC046E139, nombre=Producto 1579478868281, fecha_creacion=2020-01-19 18:07:48.0, none=null}
#OrmDataBase: UPDATE producto SET codigo=?, nombre=?, fecha_creacion=? WHERE id = ?; [16FC046E139, Papas frias, 2020-01-19 18:07:48.0, 111]
#OrmDataBase: DELETE FROM producto WHERE id = ?; [111]
#OrmDataBase: SELECT id, codigo, nombre, fecha_creacion FROM producto ORDER BY fecha_creacion DESC, nombre DESC LIMIT 25, 7
#OrmDataBase: SELECT COUNT(id) AS total_rows FROM producto
0
#OrmDataBase: CLOSE jdbc:mysql://localhost:3306/punto_venta;user=root
```

### Define DAO

```java
@TableInfo(name = "producto")
public class Producto {
  ...
  ... 
  ...
  public static OrmDao<Producto, Long> dao() {
    return DB.punto_venta.dao(Producto.class);
  }
}
```

### PAGINATION
```java
SearchCriteria q = new SearchCriteria();
q.clearParams();
q.put("fecha_creacion", "2020-01-19");
q.put("nombre", "Papas frias");
q.setLimit(10);
    
do {
  OrmIterator<Producto> it = Producto.search(q);
  for (Producto producto : it) {
    System.out.println(producto);
  }
  System.out.println(" - - -");
      
} while (q.nextPage());
        
Producto.dao().close();
```

Consola:
```markdown
#OrmDataBase: LOAD driver com.mysql.jdbc.Driver
#OrmDataBase: OPEN jdbc:mysql://localhost:3306/punto_venta;user=root
#OrmDataBase: SELECT COUNT(*) AS total_rows FROM producto WHERE CAST(fecha_creacion AS DATE) = ? AND nombre LIKE ? ORDER BY fecha_creacion DESC, id DESC; [2020-01-19, %Papas frias%]
#SearchCriteria: ▌1=>0▐ [2=>10] [3=>20] [4=>30] [5=>40] [6=>50] [7=>60] [8=>70] [9=>80] [10=>90]    page:1, index:0, limit:10, total_rows:100
#OrmDataBase: SELECT id, codigo, nombre, fecha_creacion FROM producto WHERE CAST(fecha_creacion AS DATE) = ? AND nombre LIKE ? ORDER BY fecha_creacion DESC, id DESC LIMIT 0, 10; [2020-01-19, %Papas frias%]
Producto{id=100, codigo=16FC04C9C72-3EE42CAD4DB, nombre=Papas frias-16FC04C9C72-3EE42CAD4DB, fecha_creacion=2020-01-19 18:14:03.0, none=null}
Producto{id=99, codigo=16FC04C9C6F-3EE429888F7, nombre=Papas frias-16FC04C9C6F-3EE429888F7, fecha_creacion=2020-01-19 18:14:03.0, none=null}
Producto{id=98, codigo=16FC04C9C66-3EE4216BF27, nombre=Papas frias-16FC04C9C66-3EE4216BF27, fecha_creacion=2020-01-19 18:14:03.0, none=null}
Producto{id=97, codigo=16FC04C9C63-3EE41E45275, nombre=Papas frias-16FC04C9C63-3EE41E45275, fecha_creacion=2020-01-19 18:14:03.0, none=null}
Producto{id=96, codigo=16FC04C9C5F-3EE41A0F26A, nombre=Papas frias-16FC04C9C5F-3EE41A0F26A, fecha_creacion=2020-01-19 18:14:03.0, none=null}
Producto{id=95, codigo=16FC04C9C5B-3EE4164E5B7, nombre=Papas frias-16FC04C9C5B-3EE4164E5B7, fecha_creacion=2020-01-19 18:14:03.0, none=null}
Producto{id=94, codigo=16FC04C9C57-3EE412E62E9, nombre=Papas frias-16FC04C9C57-3EE412E62E9, fecha_creacion=2020-01-19 18:14:03.0, none=null}
Producto{id=93, codigo=16FC04C9C53-3EE40EA282A, nombre=Papas frias-16FC04C9C53-3EE40EA282A, fecha_creacion=2020-01-19 18:14:03.0, none=null}
Producto{id=92, codigo=16FC04C9C4C-3EE4085F49F, nombre=Papas frias-16FC04C9C4C-3EE4085F49F, fecha_creacion=2020-01-19 18:14:03.0, none=null}
Producto{id=91, codigo=16FC04C9C0E-3EE3CCDA972, nombre=Papas frias-16FC04C9C0E-3EE3CCDA972, fecha_creacion=2020-01-19 18:14:03.0, none=null}
 - - -
```

### Search method

```java
@TableInfo(name = "producto")
public class Producto {
  ...
  ... 
  ...
  public static OrmIterator<Producto> search(SearchCriteria q) throws SQLException {
    OrmResultSet result = Producto.dao().queryBuilder()
            //.select("*")
            //.distinct()
            //.from("producto")
            .where("CAST(fecha_creacion AS DATE)", "=", q.get("fecha_creacion"))
            .like("nombre", q.get("nombre"))
            //.where_in("id", 50, 60, 30)
            //.or_where_in("id", 22, 44)
            //.where("id", ">", 14)
            //.where_not_in("id", 50, 60, 30)
            //.or_where_not_in("id", 22, 44)
            //.not_like("nombre", "jet")
            //.join("producto b", "producto.id = b.id")
            //.join("producto c", "producto.id = c.id", "LEFT")
            .order_by("fecha_creacion", "DESC").order_by("id", "DESC")
            //.group_by("producto.id")
            //.group_by("producto.nombre")
            //.having("nombre = 'Papas frias'")
            //.limit(7, 25)
            .paginate(q);
    
    return result.it(Producto.class);
  }
}
```

License
=======

    Copyright 2020 JesusBetaX, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
