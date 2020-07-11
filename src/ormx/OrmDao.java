package ormx;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author jesus
 */
public class OrmDao<T, ID> implements AutoCloseable {
  final OrmDataBase db; 
  final OrmObjectAdapter<T> adapter;
  final String table;
  final OrmField<ID> key;
  final boolean autoIncrement;
  
  OrmDao(Builder<T, ID> builder) {
    this.db = builder.db;
    this.adapter = builder.adapter;
    this.table = builder.table;
    this.key = builder.key;
    this.autoIncrement = builder.autoIncrement;
  }

  public OrmDataBase db() {
    return db;
  }
  
  public OrmObjectAdapter<T> adapter() {
    return adapter;
  }
  
  public String table() {
    return table;
  }
  
  public QueryBuilder queryBuilder() {
    return db().queryBuilder().select(adapter.fields()).from(table);
  }
  
  public boolean setId(T obj, ID id) {
    if (key == null) 
      return false;
    
    try {
      key.set(obj, id);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
  
  public ID getId(T obj) {
    if (key == null) 
      return null;
    
    try {
      return key.get(obj);
    } catch (Exception e) {
      return null;
    }
  }
  
  public Map<String, Object> vars(T object) {
    final Map<String, Object> vars = adapter.map(object);
    if (autoIncrement) vars.remove(key.name);
    return vars;
  }

  public boolean insert(T obj) throws SQLException {
    return insert(queryBuilder(), obj);
  }
  
  boolean insert(QueryBuilder builder, T obj)  throws SQLException {
    final Map<String, Object> vars = vars(obj);
    
    OrmResult rs = null;
    try {
      rs = builder.executeInsert(vars);
      
      if (autoIncrement) {
        if (rs.next()) {
          ID id = key.type.fromResult(rs, OrmDataBase.GENERATED_KEY, 1);
          setId(obj, id);
        }
      }
      
      return true;

    } finally {
      OrmUtils.close(rs);
    }
  }
  
  public int update(T obj) throws SQLException {
    final ID id = getId(obj);
    final Map<String, Object> vars = vars(obj);
    return queryBuilder().where(key.name, id).update(vars);
  }

  public boolean save(T obj) throws SQLException {
    final ID id = getId(obj);
    
    if (id != null && idExists(id)) {
      return update(obj) > 0;
    } else {
      return insert(obj);
    }
  }
  
  public int delete(T obj) throws SQLException {
    ID id = getId(obj);
    return deleteById(id);
  }
  
  public int deleteById(ID id) throws SQLException {
    return queryBuilder().where(key.name, id).delete();
  }
  
  public int deleteAll() throws SQLException {
   return queryBuilder().delete();
  }
  
  public int deleteIds(ID... ids) throws SQLException {
    return queryBuilder().where_in(key.name, ids).delete();
  }
  
  public long count() throws SQLException {
    return queryBuilder().get_select_count(key.name);
  }
  
  public long count(String whereClause, Object... whereArgs) throws SQLException {
    return db.count(table, key.name, whereClause, whereArgs);
  }
  
  public boolean idExists(ID id) throws SQLException {
    return queryBuilder().where(key.name, id).get_select_count(key.name) > 0;
  }
  
  public T findById(ID id) throws SQLException {
    return findByField(key.name, id);
  }
  
  public T findByField(String column, Object value) throws SQLException {
    return queryBuilder().where(column, value).get().row(adapter);
  }
  
  public List<T> query(QueryBuilder query) throws SQLException {
    return query.get().list(adapter);
  }
  
  public List<T> query(String sql, Object... params) throws SQLException {
    return db.query(sql, params).list(adapter);
  }
  
  public List<T> queryForEq(String column, Object value) throws SQLException {
    return queryBuilder().where(column, value).get().list(adapter);
  }
  
  public List<T> queryForAll() throws SQLException {
    return queryBuilder().get().list(adapter);
  }
 
  public OrmIterator<T> iterator(QueryBuilder query) throws SQLException {
    return query.get().it(adapter);
  }
  
  public OrmIterator<T> iterator(String sql, Object... params) throws SQLException {
    return db.query(sql, params).it(adapter);
  }
  
  public OrmIterator<T> iterator() throws SQLException {
    return queryBuilder().get().it(adapter);
  }
  
  public void truncate() throws SQLException {
    exec("TRUNCATE TABLE " + table);
  }
  
  public boolean exec(String sql, Object... params) throws SQLException{
    return db().execute(sql, params);
  }
  
  public void beginTransaction() throws SQLException {
    db().beginTransaction();
  }
  
  public void commit() throws SQLException {
    db().commit();
  }
  
  public void rollback() throws SQLException {
    db().rollback();
  }
  
  public void endTransaction() throws SQLException {
    db().endTransaction();
  }
  
  @Override public void close() {
    db.close();
  }
  
  public static class Builder<R, ID> 
  {
    final OrmDataBase db;
    Class<R> classOf;
    OrmObjectAdapter<R> adapter;
    String table;
    OrmField<ID> key;
    boolean autoIncrement;
    
    public Builder(OrmDataBase db) {
      this.db = db;
    }

    public Builder setClassOf(Class<R> classOf) {
      this.classOf = classOf;
      return this;
    }
    
    public OrmDao<R, ID> build() {
      adapter = OrmObjectAdapter.of(classOf);
      table = OrmUtils.tableName(classOf);
      key = OrmUtils.primaryKey(adapter);

      if (key != null) {
        autoIncrement = key.info.autoIncrement();
      } 

      return new OrmDao<R, ID>(this);
    }
  }
}