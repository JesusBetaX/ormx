package ormx;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

public class OrmIterator<T> implements Iterable<T>, Iterator<T>, AutoCloseable {

  final OrmObjectAdapter<T> adapter;
  final ResultSet resultSet;
  
  private boolean next;

  public OrmIterator(OrmObjectAdapter<T> adapter, ResultSet resultSet) {
    this.adapter = adapter;
    this.resultSet = resultSet;
  }

  @Override public Iterator<T> iterator() {
    return this;
  }

  @Override public boolean hasNext() {
    try {
      return next = resultSet.next();
    } catch (SQLException e) {
      close();
      throw new RuntimeException(e.getMessage(), e);
    } finally {
      if (!next) close();
    }
  }

  @Override public T next() {
    try {
      return adapter.resultSetToEntityOrThrow(resultSet);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override public void remove() {
    
  }

  @Override public void close() {
    OrmUtils.close(resultSet);
  }
}
