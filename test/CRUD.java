
import java.sql.SQLException;
import java.util.List;
import models.DB;
import models.Producto;
import ormx.OrmDataBase;

public class CRUD {

  public static void main(String... args) throws SQLException {
    OrmDataBase db = DB.punto_venta;
    
     // INSERT ...
    Producto p = new Producto();
    p.codigo = "COD-001";
    p.nombre = "Test";
    db.queryBuilder().insert(p);
    
    // SELECT * FROM ... WHERE id = ?
    p = db.queryBuilder()
            .where("id", "=", p.id)
            .get_first(Producto.class); 
    
    System.out.println(p);
    
     // UPDATE ... WHERE id = ?
    p.nombre = "001";
    db.queryBuilder()
            .where("id", "=", p.id)
            .update(p);
    
    // DELETE ... WHERE id = ?
    db.queryBuilder()
            .where("id", "=", p.id)
            .delete(Producto.class);
    
    // SELECT * FROM ... LIMIT 25, 1
    List<Producto> result = db.queryBuilder()
            .limit(25, 7)
            .get_list(Producto.class); 
    
    for (Producto it : result) {
      System.out.println(it);
    }
    
    db.close();
  }
}
