
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import models.DB;
import models.DateConverter;
import ormx.OrmDao;
import ormx.annot.ColumnInfo;
import ormx.annot.PersisterClass;
import ormx.annot.TableInfo;

public class SimpleSearch {
  
  public static void main(String... args) throws SQLException {
    new SimpleSearch().init();
  }
  
  @TableInfo(name = "producto")
  public static class Producto {
    @ColumnInfo(primaryKey = true, autoIncrement = true)
    public long id;
    
    @ColumnInfo
    public String codigo;
    
    @ColumnInfo 
    public String nombre;
    
    @ColumnInfo
    @PersisterClass(DateConverter.class)
    public Date fecha_creacion = new Date();

    @Override public String toString() {
      return "Producto{" + "id=" + id + ", codigo=" + codigo + ", nombre=" + nombre + ", fecha_creacion=" + fecha_creacion + '}';
    }
  }
  
  void init() throws SQLException {
    OrmDao<Producto, Long> dao = DB.punto_venta.dao(Producto.class);
    
    List<Producto> result = dao.queryBuilder()
            .where("id", "=", 2)
            .get_list(Producto.class);
    
    System.out.println(result);
            
    dao.close();
  }
}
