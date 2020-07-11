package models;

import ormx.OrmDataBase;

public class DB {

  public static final OrmDataBase punto_venta = new OrmDataBase()
          .setDriverClassName("com.mysql.jdbc.Driver")
          .setUrl("jdbc:mysql://localhost:3306/punto_venta")
          .setUsername(/*"usuario"*/"root")
          .setPassword(/*"password"*/"")
          .setDebug(true);
}
