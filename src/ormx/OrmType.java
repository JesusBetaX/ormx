package ormx;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class OrmType<T> {
  public static final HashMap<Class<?>, OrmType> types = new HashMap<Class<?>, OrmType>(17);
  public static final HashMap<Class<?>, OrmType> others = new HashMap<Class<?>, OrmType>();
 
  static {
    types.put(String.class, new StringType());

    BoolType boolAdapter = new BoolType();
    types.put(boolean.class, boolAdapter);
    types.put(Boolean.class, boolAdapter);

    ShortType shortAdapter = new ShortType();
    types.put(short.class, shortAdapter);
    types.put(Short.class, shortAdapter);

    IntType intAdapter = new IntType();
    types.put(int.class, intAdapter);
    //adapters.put(byte.class, intAdapter);
    types.put(Integer.class, intAdapter);

    LongType longAdapter = new LongType();
    types.put(long.class, longAdapter);
    types.put(Long.class, longAdapter);

    FloatType floatAdapter = new FloatType();
    types.put(float.class, floatAdapter);
    types.put(Float.class, floatAdapter);

    DoubleType doubleAdapter = new DoubleType();
    types.put(double.class, doubleAdapter);
    types.put(Double.class, doubleAdapter);

    types.put(byte[].class, new BytesType());

    types.put(InputStream.class, new StreamType());

    types.put(Date.class, new DateType());
    
    types.put(Object.class, new ObjectType());
  }
  
  public void toValues(Map<String, Object> values, String column, T value) throws Exception {
    values.put(column, value);
  }

  public abstract T fromResult(ResultSet rs, String column, int index) throws SQLException;
  
  public static OrmType of(Class<?> type) {
    OrmType typeAdapter = types.get(type);
    //return typeAdapter == null ? DEFAULT_TYPE_ADAPTER : typeAdapter;
    if (typeAdapter == null) {
      throw new RuntimeException(
              String.format("No se ha definido un adaptador para: '%s'", type)
      );
    }
    return typeAdapter;
  }
  
  public static <V> void put(Class<V> type, OrmType<V> typeAdapter) {
    types.put(type, typeAdapter);
  }
  
  public static <V> OrmType<V> other(Class<V> type) throws Exception {
    OrmType typeAdapter = others.get(type);
    
    if (typeAdapter == null) {
      Constructor<OrmType<V>> constructor = 
              (Constructor<OrmType<V>>) type.getConstructor();
      
      typeAdapter = constructor.newInstance();
      others.put(type, typeAdapter);
    }
    
    return typeAdapter;
  }
  
  /*******************************************************************/
  
  public static class ObjectType extends OrmType<Object> {
    @Override
    public Object fromResult(ResultSet rs, String column, int index) throws SQLException {
      return rs.getObject(index);
    }
  }
  
  public static class StringType extends OrmType<String> {
    @Override
    public String fromResult(ResultSet rs, String column, int index) throws SQLException {
      return rs.getString(index);
    }
  }
  
  public static class BoolType extends OrmType<Boolean> {
    @Override
    public Boolean fromResult(ResultSet rs, String column, int index) throws SQLException {
      return rs.getBoolean(index);
    }
  }
  
  public static class ShortType extends OrmType<Short> {
    @Override
    public Short fromResult(ResultSet rs, String column, int index) throws SQLException {
      return rs.getShort(index);
    }
  }
  
  public static class IntType extends OrmType<Integer> {
    @Override
    public Integer fromResult(ResultSet rs, String column, int index) throws SQLException {
      return rs.getInt(index);
    }
  }
  
  public static class LongType extends OrmType<Long> {
    @Override
    public Long fromResult(ResultSet rs, String column, int index) throws SQLException {
      return rs.getLong(index);
    }
  }
  
  public static class FloatType extends OrmType<Float> {
    @Override
    public Float fromResult(ResultSet rs, String column, int index) throws SQLException {
      return rs.getFloat(index);
    }
  }
  
  public static class DoubleType extends OrmType<Double> {
    @Override
    public Double fromResult(ResultSet rs, String column, int index) throws SQLException {
      return rs.getDouble(index);
    }
  }
  
  public static class BytesType extends OrmType<byte[]> {
    @Override
    public byte[] fromResult(ResultSet rs, String column, int index) throws SQLException {
      return rs.getBytes(index);
    }
  }
  
  public static class StreamType extends OrmType<InputStream> {
    @Override
    public InputStream fromResult(ResultSet rs, String column, int index) throws SQLException {
      return rs.getBinaryStream(index);
    }
  }
  
  public static class DateType extends OrmType<Date> {
    @Override
    public Date fromResult(ResultSet rs, String column, int index) throws SQLException {
      return rs.getTimestamp(index);
    }
  }
}
