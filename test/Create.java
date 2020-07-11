
import java.sql.SQLException;
import models.DB;
import models.Producto;
import ormx.OrmDao;

public class Create {

  public static void main(String... args) throws SQLException{
    OrmDao<Producto, Long> dao = DB.punto_venta.dao(Producto.class);
     
    dao.truncate();
    
    try {
      dao.beginTransaction();
      
      for (int i = 0; i < 100; i++) {
        long time = System.currentTimeMillis();
        long nano = System.nanoTime();
        Producto p = new Producto();
        p.codigo = Long.toHexString(time).toUpperCase() + "-" + Long.toHexString(nano).toUpperCase();
        p.nombre = "Papas frias-" + p.codigo;
        dao.insert(p);
      }
      
      dao.commit();
      
    } catch(Exception e)  {
      dao.rollback();
    
    } finally {
      dao.endTransaction();
    }
    
    dao.close();
  }
}
