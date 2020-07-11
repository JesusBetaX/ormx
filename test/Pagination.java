import java.sql.SQLException;
import models.Producto;
import ormx.OrmIterator;
import ormx.SearchCriteria;

/**
 *
 * @author jesus
 */
public class Pagination {

  public static void main(String... args) throws SQLException {
    SearchCriteria q = new SearchCriteria();
    q.clearParams();
    q.put("fecha_creacion", "2020-02-02");
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
  }
}
