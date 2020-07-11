
import java.sql.SQLException;
import models.Producto;
import ormx.OrmResult;

/**
 *
 * @author jesus
 */
public class SimpleDao {

  public static void main(String... args) throws SQLException {
    Producto p = new Producto();
    p.id = 1000L;
    p.codigo = Long.toHexString(System.currentTimeMillis()).toUpperCase();
    p.nombre = "Producto " + System.currentTimeMillis();
    Producto.dao().insert(p); 
    
    p = Producto.dao().findById(p.id);
    System.out.println(p);
    
    p.nombre = "Papas frias";
    Producto.dao().update(p);
    
    Producto.dao().deleteById(p.id);
    
    OrmResult result = Producto.dao().queryBuilder()
            .order_by("fecha_creacion", "DESC").order_by("nombre", "DESC")
            .limit(25, 7)
            .get();
    
    for (Producto producto : result.it(Producto.class)) {
      System.out.println(producto);
    }
    
    System.out.println(Producto.dao().count());

    Producto.dao().close();
  }

}
