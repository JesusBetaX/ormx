package models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import ormx.OrmType;

public class DateConverter extends OrmType<Date> {

  @Override
  public void toValues(Map<String, Object> values, String column, Date value) throws Exception {
//    long time = value.getTime();
//    values.put(column.name(), time);
    values.put(column, value);
  }
  
  @Override
  public Date fromResult(ResultSet rs, String column, int index) throws SQLException {
//    long time = rs.getLong(column.name());
//    return new Date(time);
    return rs.getTimestamp(index);
  }
  
}
