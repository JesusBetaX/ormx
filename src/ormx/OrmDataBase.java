package ormx;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author Jesus
 */
public class OrmDataBase implements AutoCloseable {
  static final String GENERATED_KEY = "GENERATED_KEY";
  
  private final LinkedList<QueryBuilder> querys = new LinkedList<QueryBuilder>();
  private final HashMap<Class<?>, OrmDao<?,?>> daos = new HashMap<Class<?>, OrmDao<?,?>>();
  
  /**
   * Driver de coneccion. 
   * MySQL      : com.mysql.jdbc.Driver 
   * Oracle     : oracle.jdbc.driver.OracleDriver 
   * PostgreSQL : org.postgresql.Driver
   * SQLServer  : com.microsoft.sqlserver.jdbc.SQLServerDriver
   */
  private String driverClassName = "com.mysql.jdbc.Driver";
  
  private String url;
  private String username;
  private String password;
  protected Connection conn;
 
  private int validTimeout = 8;
  private boolean debug;
  
// Costructor
  
  public OrmDataBase() {
  }

// Funciones  
  
  protected Connection newConnection() throws SQLException {
    try {
      if (debug) OrmUtils.debug(OrmDataBase.class, "LOAD driver ", driverClassName);  
      Class.forName(driverClassName);
    } catch (ClassNotFoundException ex) {
      throw new SQLException(ex);
    }
    return DriverManager.getConnection(url, username, password);
  }
  
  /**
   * @return @true si la base de datos esta cerrada.
   *
   * @throws SQLException
   */
  public synchronized boolean isClosed() throws SQLException {
    return conn == null || conn.isClosed();
  }

  @Override public synchronized void close() {
    OrmUtils.close(conn);
    
    if (conn != null && debug)
      OrmUtils.debug(OrmDataBase.class, "CLOSE ", url, ";user=", username);
    
    conn = null;
  }
  
  /**
   * Establece la coneccion con la base de datos.
   *
   * @return la coneccion
   *
   * @throws SQLException
   */
  public synchronized Connection getConnection() throws SQLException {
    if (isClosed()) {
      conn = newConnection();
      
      if (debug)
        OrmUtils.debug(OrmDataBase.class, "OPEN ", url, ";user=", username);
    
    } else if (!conn.isValid(validTimeout)) {
      close();
      conn = newConnection();
      
      if (debug)
        OrmUtils.debug(OrmDataBase.class, "RE-OPEN ", url, ";user=", username);
    }
    
    return conn;
  }
  
  /**
   * Prepara sentencias sql.
   *
   * @param sql instruccion a preparar
   * @param flags [opcional] flags de configuracion
   *
   * @return PreparedStatement setencia preparada
   *
   * @throws SQLException
   */
  public PreparedStatement prepareStatement(String sql, int... flags) 
  throws SQLException {
    if (OrmUtils.isEmpty(flags))
      return getConnection().prepareStatement(sql);
    else
      return getConnection().prepareStatement(sql, flags);
  }
  
  public Statement createStatement() throws SQLException {
    return getConnection().createStatement();
  }
  
  /**
   * Ejecuta consultas a la base de datos.
   *
   * @param sql query a ejecutar
   * @param params [opcional] parametros del query
   *
   * @return ResultSet con el resultado obtenido
   *
   * @throws SQLException
   */
  public OrmResult query(String sql, Object... params) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = prepareStatement(sql);
      OrmUtils.bindArgs(ps, params);
      
      if (debug) 
        OrmUtils.debugStmt(OrmDataBase.class, ps, sql, params);
      
      return OrmResult.executeQuery(ps);
    
    } catch(SQLException e) {
      OrmUtils.close(ps);
      throw e;
    
    } finally {
      //ps.closeOnCompletion();
    }
  }

  /**
   * Ejecuta sentencias a la base de datos.
   *
   * @param sql sentencia a ejecutar
   * @param params [opcional] parametros del query
   *
   * @return @true resultado obtenido
   *
   * @throws SQLException
   */
  public boolean execute(String sql, Object... params) throws SQLException {
    return executeUpdate(sql, params) == 1;
  }
  
  /**
   * Ejecuta un sentenci sql y obtiene la ultima llave generada.
   * 
   * @param sql sentencia insert
   * @param params [opcional] parametros de la sentencia
   * @return GENERATED_KEY
   * @throws SQLException 
   */
  public OrmResult executeAndReturnKeys(String sql, Object... params) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      OrmUtils.bindArgs(ps, params);
      
      if (debug) 
        OrmUtils.debugStmt(OrmDataBase.class, ps, sql, params);
      
      ps.executeUpdate();
      //obtengo la ultima llave generada
      ResultSet rs = ps.getGeneratedKeys();
      return new OrmResult(ps, rs);

    } catch (SQLException e) {
      OrmUtils.close(ps);
      throw e;
    }
  }
  
  /**
   * Ejecuta sentencias insert y obtiene el id del registro insertado.
   * 
   * @param sql sentencia insert
   * @param params [opcional] parametros de la sentencia
   * 
   * @return el ID de la fila recién insertada, o -1 si se produjo un error
   * 
   * @throws SQLException 
   */
  public long executeInsert(String sql, Object... params) throws SQLException {
    ResultSet rs = null;
    try {
      //obtengo la ultima llave generada
      rs = executeAndReturnKeys(sql, params);
      return rs.next() ? rs.getLong(1) : 0;
    } finally {
      OrmUtils.close(rs);
    }
  }
 
  public int executeUpdate(String sql, Object... params) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = prepareStatement(sql);
      OrmUtils.bindArgs(ps, params);
      
      if (debug)
        OrmUtils.debugStmt(OrmDataBase.class, ps, sql, params);
      
      return ps.executeUpdate();
    } finally {
      OrmUtils.close(ps);
    }
  }
    
  /**
   * Inserta un registro en la base de datos.
   *
   * @param tabla donde se va a insertar la fila
   * @param datos mapa contiene los valores de columna iniciales para la fila.
   *      Las claves deben ser los nombres de las columnas 
   *      y los valores valores de la columna
   *
   * @return el ID de la fila recién insertada, o -1 si se produjo un error
   *
   * @throws SQLException
   */
  public long insert(String tabla, Map<String, Object> datos) throws SQLException {
    return queryBuilder().from(tabla).insert(datos);
  }

//  public <V> boolean insert(V obj) throws SQLException {
//    Class<V> classOf = (Class<V>) obj.getClass();
//    return dao(classOf).insert(obj);
//  }
  
  /**
   * Actualiza una registro en la base de datos.
   *
   * @param tabla donde se va a actualizar la fila.
   * @param datos mapa contiene los valores de columna iniciales para la fila. 
   *      Las claves deben ser los nombres de las columnas y los valores valores 
   *      de la columna.
   * @param whereClause [opcional] cláusula WHERE para aplicar al actualizar.
   *      Pasar null actualizará todas las filas.
   * @param whereArgs [opcional] Puede incluirse en la cláusula WHERE, que
   *      será reemplazado por los valores de whereArgs. Los valores
   *      se enlazará como cadenas.
   *
   * @return el número de filas afectadas.
   *
   * @throws SQLException
   */
  public int update(String tabla, Map<String, Object> datos, String whereClause, Object... whereArgs)
  throws SQLException {
     return queryBuilder().from(tabla).whereRaw(whereClause, whereArgs).update(datos);
  }
  
//  public <V> int update(V obj) throws SQLException {
//    Class<V> classOf = (Class<V>) obj.getClass();
//    return dao(classOf).update(obj);
//  }
  
  /**
   * Elimina un registro de la base de datos.
   * 
   * @param tabla donde se eliminara
   * @param whereClause [opcional] cláusula WHERE para aplicar la eliminación.
   *      Pasar null elimina todas las filas.
   * @param whereArgs [opcional] Puede incluirse en la cláusula WHERE, que
   *      será reemplazado por los valores de whereArgs. Los valores
   *      se enlazará como cadenas.
   * 
   * @return el número de filas afectadas.
   * 
   * @throws SQLException 
   */
  public int delete(String tabla, String whereClause, Object... whereArgs) 
  throws SQLException {
    return queryBuilder().from(tabla).whereRaw(whereClause, whereArgs).delete();
  }
  
//  public <V> int delete(V obj) throws SQLException {
//    Class<V> classOf = (Class<V>) obj.getClass();
//    return dao(classOf).delete(obj);
//  }
  
  /**
   * Obtiene el numero de filas.
   *
   * @param tabla donde se buscaran las existencias
   * @param selection campo ha seleccionar
   * @param whereClause condicion
   * @param whereArgs [opcional] parametros del whereClause
   *
   * @return numero de existencia
   *
   * @throws SQLException
   */
  public long count(String tabla, String selection, String whereClause, Object... whereArgs) 
  throws SQLException {
    return queryBuilder().from(tabla).whereRaw(whereClause, whereArgs)
            .get_select_count(selection);
  }

  /**
   * Begins a transaction in EXCLUSIVE mode. <p> Transactions can be nested.
   * When the outer transaction is ended all of the work done in that
   * transaction and all of the nested transactions will be committed or rolled
   * back. The changes will be rolled back if any transaction is ended without
   * being marked as clean (by calling setTransactionSuccessful). Otherwise they
   * will be committed. </p> <p>Here is the standard idiom for transactions:
   *
   * <pre>
   *   try {
   *     db.beginTransaction();
   *     ...
   *     db.commit();
   *   } catch(Exception e) {
   *     db.rollback();
   *   } finally {
   *     db.endTransaction();
   *   }
   * </pre>
   */
  public void beginTransaction() throws SQLException {
    getConnection().setAutoCommit(false);
  }
  
  public void commit() throws SQLException {
    getConnection().commit();
  }
  
  public void rollback() throws SQLException {
    getConnection().rollback();
  }
  
  public void endTransaction() throws SQLException {
    getConnection().setAutoCommit(true);
  }
  
  @Override protected void finalize() throws Throwable {
    try {
      close();
    } finally {
      super.finalize();
    }
  }
  
  public synchronized QueryBuilder queryBuilder() {
    QueryBuilder query = querys.poll();
    if (query == null) query = new QueryBuilder(this);
    return query;
  }

  public synchronized void recycler(QueryBuilder query) {
    querys.add(query.reset());
  }
  
  public synchronized <V, ID> OrmDao<V, ID> dao(Class<V> classOf) {
    OrmDao<V, ID> dao = (OrmDao<V, ID>) daos.get(classOf);
    if (dao == null) {
      dao = new OrmDao.Builder<V, ID>(this)
              .setClassOf(classOf)
              .build();
      
      putCache(classOf, dao);
    }
    return dao; 
  }
  
  public synchronized <V, ID> void putCache(Class<V> classOf, OrmDao<V, ID> dao) {
    daos.put(classOf, dao);
  }
  
  public String getDriverClassName() {
    return driverClassName;
  }

  public OrmDataBase setDriverClassName(String driverClassName) {
    this.driverClassName = driverClassName;
    return this;
  }

  public String getUrl() {
    return url;
  }

  public OrmDataBase setUrl(String url) {
    this.url = url;
    return this;
  }

  public String getUsername() {
    return username;
  }

  public OrmDataBase setUsername(String username) {
    this.username = username;
    return this;
  }

  public String getPassword() {
    return password;
  }

  public OrmDataBase setPassword(String password) {
    this.password = password;
    return this;
  }

  public int getValidTimeout() {
    return validTimeout;
  }

  public OrmDataBase setValidTimeout(int validTimeout) {
    this.validTimeout = validTimeout;
    return this;
  }
  
  public boolean isDebug() {
    return debug;
  }

  public OrmDataBase setDebug(boolean debug) {
    this.debug = debug;
    return this;
  }
}
