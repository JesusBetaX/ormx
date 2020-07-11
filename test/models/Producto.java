package models;

import java.sql.SQLException;
import java.util.Date;
import ormx.OrmDao;
import ormx.OrmIterator;
import ormx.OrmResult;
import ormx.SearchCriteria;
import ormx.annot.ColumnInfo;
import ormx.annot.TableInfo;

@TableInfo(name = "producto")
public class Producto
{
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
  
  public static OrmDao<Producto, Long> dao() {
    return DB.punto_venta.dao(Producto.class);
  }
  
  public static OrmIterator<Producto> search(SearchCriteria q) throws SQLException {
    OrmResult result = Producto.dao().queryBuilder()
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
            //.having("nombre", "=", "'Papas frias'")
            //.limit(7, 25)
            .paginate(q);
    
    return result.it(Producto.class);
  }
}
