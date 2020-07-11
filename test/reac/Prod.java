package reac;

import java.sql.SQLException;
import java.util.Date;
import models.DB;
import ormx.annot.ColumnInfo;
import ormx.annot.TableInfo;

@TableInfo(name="producto")
public class Prod {

  @ColumnInfo(primaryKey=true, autoIncrement=true)
  private int id;
  
  @ColumnInfo
  private String codigo;
  
  @ColumnInfo
  private String nombre;
  
  @ColumnInfo
  private Date fecha_creacion = new Date();

  public Prod() {
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }
  
  public void setId(String id) {
    this.id = Integer.parseInt(id);
  }

  public String getCodigo() {
    return codigo;
  }

  public void setCodigo(String codigo) {
    this.codigo = codigo;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public Date getFecha_creacion() {
    return fecha_creacion;
  }

  public void setFecha_creacion(Date fecha_creacion) {
    this.fecha_creacion = fecha_creacion;
  }
  
  

  @Override public String toString() {
    return "Prod{" + "id=" + id + ", codigo=" + codigo + ", nombre=" + nombre + ", fecha_creacion=" + fecha_creacion + '}';
  }
  
  public static void main(String... args) throws SQLException {
    Prod prod = new Prod();
    prod.id = 0;
    prod.codigo = Long.toHexString(System.currentTimeMillis()).toUpperCase();
    prod.nombre = "Test";
    DB.punto_venta.queryBuilder()
            .insert(prod);
    
    System.out.println(prod);
    
    prod = DB.punto_venta.queryBuilder()
            .where("id", prod.id)
            .get_first(Prod.class);
    
    System.out.println(prod);
    
    DB.punto_venta.queryBuilder()
            .where("id", prod.id)
            .delete(Prod.class);
  }
}
