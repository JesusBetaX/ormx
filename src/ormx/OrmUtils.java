package ormx;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import ormx.annot.ColumnInfo;
import ormx.annot.PersisterClass;
import ormx.annot.TableInfo;

/**
 *
 * @author jesus
 */
public final class OrmUtils {

  private OrmUtils() {
  }
  
  public static void close(AutoCloseable ac) {
    try {
      if (ac != null) 
        ac.close();
    
    } catch (Exception e) {
      // TODO Auto-generated catch block
    }
  }
  
  public static void clear(StringBuilder sb) {
    sb.delete(0, sb.capacity());
    //sb.setLength(0);
  }
  
  public static boolean isNull(Object obj) {
    return obj == null; 
  }

  public static boolean isEmpty(CharSequence str) {
    return (isNull(str) || str.length() == 0) ? true : false;
  }
  
  public static <V> boolean isEmpty(V... array) {
    return (isNull(array) || array.length == 0) ? true : false;
  }
  
  public static String trim(String str) {
    return isEmpty(str) ? "" : str.trim();
  }
  
  
  public static String tableName(Class<?> classOf) {
    TableInfo info = (TableInfo) classOf.getAnnotation(TableInfo.class);
    return ( isEmpty(info.name()) ) ? classOf.getSimpleName() : info.name();
  }

  public static String columnName(ColumnInfo info, Field field) {
    String name = info.name();
    return isEmpty(name) ? field.getName() : name;
  }
  
  public static <V> OrmField<V> primaryKey(OrmField[] fields) {
    for (OrmField field : fields) 
      if (field.info.primaryKey()) 
        return field;
      
    return null;
  }
  
  public static <V> OrmField<V> primaryKey(OrmObjectAdapter adapter) {
    return primaryKey(adapter.fields);
  }

  public static OrmType type(Field field) throws Exception {
    PersisterClass persister = field.getAnnotation(PersisterClass.class);
    if (persister != null) {
      return OrmType.other(persister.value());
    }
    return OrmType.of(field.getType());
  }
  
  public static String capitalize(String str) {
    char[] data = str.toCharArray();
    data[0] = Character.toUpperCase(data[0]);
    return new String(data);
  }
  
  public static Method getMethod(Field f, Method[] m) throws Exception {
    String name = OrmUtils.capitalize(f.getName());
    String methodnameA = "get" + name;
    String methodnameB = "is" + name;
    
    Class[] parameterTypes;
    
    for (Method method : m) {
      parameterTypes = method.getParameterTypes();
      
      if (parameterTypes.length > 0) {
        continue;
      }
      
      if (method.getName().equals(methodnameA)) {
        return method;
      } else if (method.getName().equals(methodnameB)) {
        return method;
      }
    }
    
    return null;
  }

  public static Method setMethod(Field f, Method[] m) throws Exception {
    String name = OrmUtils.capitalize(f.getName());
    String methodname = "set" + name;
    
    Class[] parameterTypes;
    
    for (Method method : m) {
      if (method.getName().equals(methodname)) {
        parameterTypes = method.getParameterTypes();
        
        if (parameterTypes.length == 0) {
          continue;
        }
        
        if (parameterTypes[0] == f.getType()) {
          return method;
        }
      } 
    }
    
    return null;
  }
  
  public static void appendClause(StringBuilder s, CharSequence name, CharSequence clause) {
    if (!OrmUtils.isEmpty(clause)) {
      s.append(name);
      s.append(clause);
    }
  }
  
  /**
   * Add the names that are non-null in columns to s, separating them with
   * commas.
   */
  public static void appendColumns(StringBuilder s, CharSequence... columns) {
    for (int i = 0; i < columns.length; i++) {
      CharSequence column = columns[i];
      if (column != null) {
        if (i > 0) s.append(QueryBuilder.COMMA);
        s.append(column);
      }
    }
    s.append(QueryBuilder.SPACE);
  }

  public static void appendJoin(StringBuilder query, StringBuilder joins) {
    if (!OrmUtils.isEmpty(joins)) query.append(joins);
  }
  
  public static void appendLimit(StringBuilder query, long index, long limit) {
    if (limit > 0) {
      query.append(QueryBuilder.LIMIT);
      if (index > -1) query.append(index).append(QueryBuilder.COMMA);
      query.append(limit);
    }
  }
  
  public static void bindArgs(PreparedStatement ps, Object... params) throws SQLException {
    if (!isEmpty(params)) {
      for (int i = 0; i < params.length; i++) {
        ps.setObject(i + 1, params[i]);
      }
    }
  }
  
  public static Object[] toArray(List<Object> list) {
    final Object[] result = new Object[list.size()];
    for (int i = 0; i < list.size(); i++) {
      result[i] = list.get(i);
    }
    return result;
  }
  
  public static Object[] concatToArray(List<Object>... lists) {
    int size = 0;
    for (List<Object> list : lists) {
      size = size + list.size();
    }
    
    final Object[] result = new Object[size];
    int len = 0;
    int i;
    
    for (List<Object> list : lists) {
      for (i = 0; i < list.size(); i++, len++) {
        result[len] = list.get(i);
      }
    }
    
    return result;
  }
  
  public static void fill(List<Object> original, Object[] val) {
    for (int i = 0; i < val.length; i++) {
      original.add(val[i]);
    }
  }
  
  public static void debug(Class<?> classOf, CharSequence cs) {
    String className = classOf.getSimpleName();
    
    StringBuilder builder = new StringBuilder(className.length() + cs.length() + 3)
            .append('#')
            .append(className)
            .append(':')
            .append(' ')
            .append(cs);
    
    System.out.println(builder.toString());
  }
  
  public static void debug(Class<?> classOf, Object...params) {
    StringBuilder builder = new StringBuilder();
    
    for (int i = 0; i < params.length; i++) {
      //if (i > 0) builder.append(' ');
      builder.append(params[i]);
    }
    
    debug(classOf, builder);
  }
  
  public static void debugStmt(Class<?> classOf, PreparedStatement ps, String sql, Object... params) {
    String[] split = ps.toString().split(": ");
//    if (1 < split.length) {
//      debug(classOf, trim(split[1]));
//    } else {
      StringBuilder builder = new StringBuilder().append(sql);
      
      if (!isEmpty(params)) {
        builder.append("; [");
        for (int i = 0; i < params.length; i++) {
          if (i > 0) builder.append(", ");
          builder.append(params[i]);
        }
        builder.append("]");
      }
      
      debug(classOf, builder);
    }
  //}

}
