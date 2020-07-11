package ormx;

import java.lang.reflect.Constructor;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jesus
 */
public class OrmObjectAdapter<T> {
  final static HashMap<Class<?>, OrmObjectAdapter<?>> cache = new HashMap<Class<?>, OrmObjectAdapter<?>>();
 
  final Class<T> classOf;
  final Constructor<T> constructor;
  final OrmField fields[];
  
  private OrmObjectAdapter(Class<T> classOf) throws Exception {
    this.classOf = classOf;
    this.constructor = classOf.getConstructor();
    this.fields = OrmField.fields(classOf);
  }
  
  public static <V> OrmObjectAdapter of(Class<V> classOf) {
    try {
      OrmObjectAdapter<V> adapter = (OrmObjectAdapter<V>) cache.get(classOf);
      if (adapter == null) {
        adapter = new OrmObjectAdapter<V>(classOf);
        cache.put(classOf, adapter);
      }
      return adapter;
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
  
  public T newInstance() throws Exception {
    return constructor.newInstance();
  }

  public void fill(T src, Map<String, Object> dest) throws Exception {
    for (int i = 0; i < fields.length; i++) {
      fields[i].toValues(dest, src);
    }
  }
  
  public void fill(ResultSet src, T dest, int[] indexs) throws Exception {
    for (int i = 0; i < fields.length; i++) {
      fields[i].set(dest, src, indexs[i]);
    }
  }
  
  public void fill(ResultSet src, List<T> dest) throws Exception {
    final int[] indexs = indexs(src);
    while (src.next()) {
      dest.add(resultSetToEntityOrThrow(src, indexs));
    }
  }
  
  public Map<String, Object> map(T obj) {
    try {
      final HashMap<String, Object> result = new HashMap<String, Object>(fields.length);
      fill(obj, result);
      return result;
    } catch(Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
  
  public T resultSetToEntityOrThrow(ResultSet rs, int[] indexs) throws Exception {
    T result = newInstance();
    fill(rs, result, indexs);
    return result;
  }
  
  public T resultSetToEntityOrThrow(ResultSet rs) throws Exception {
    final int[] indexs = indexs(rs);
    return resultSetToEntityOrThrow(rs, indexs);
  }
  
  public T entity(ResultSet rs) throws SQLException {
    try {
      return rs.next() ? resultSetToEntityOrThrow(rs) : null;
    } catch(Exception e) {
      throw new SQLException(e.getMessage(), e);
    }
  }
  
  public List<T> list(ResultSet rs) throws SQLException {
    try {
      final List<T> result = new ArrayList<T>();
      fill(rs, result);
      return result;
    } catch(Exception e) {
      throw new SQLException(e.getMessage(), e);
    }
  }
  
  public OrmIterator<T> it(ResultSet rs) {
    return new OrmIterator<T>(this, rs);
  }
  
  public Class<T> getClassOf() {
    return classOf;
  }

  public OrmField[] fields() {
    return fields;
  }
  
  public <V> OrmField<V> fieldByName(CharSequence columnName) {
    if (OrmUtils.isEmpty(columnName)) 
      return null;

    int pos = Arrays.binarySearch(fields, columnName);
    if (pos > -1) {
      return fields[pos];
    }
    
    return null;
  }

  public int[] indexs(ResultSet rs) throws SQLException {
    final int[] result = new int[fields.length];
    final ResultSetMetaData meta = rs.getMetaData();
    
    int first = 1;
    int last = meta.getColumnCount();
    
    for (int i = 0; i < fields.length; i++) {
      result[i] = find(meta, fields[i].name, first, last);
    }
    
    return result;
  }

  private int find(ResultSetMetaData meta, String name, int first, int len) throws SQLException {
    int i = first;
    
    while (i <= len) {
      if (name.equals(meta.getColumnLabel(i))) {
        return i;
      }
      i++;
    }

    return -1;
  }
}
