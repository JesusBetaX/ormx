package ormx;

import java.util.ArrayList;
import java.util.List;

public class ClauseArgs {
  
  public final StringBuilder clause = new StringBuilder();
  public final List<Object> args = new ArrayList<Object>();
  public int count;
  
  @Override public String toString() {
    return clause.toString();
  }
  
  public void set(ClauseArgs clause) {
    this.clause.append(clause.clause);
    this.args.addAll(clause.args);
    this.count = clause.count;
  }
  
  public void reset() {
    OrmUtils.clear(clause);
    args.clear();
    count = 0;
  }
  
  public ClauseArgs group_start() {
    wh(QueryBuilder.AND);
    clause.append(QueryBuilder.STAR_PARENT);
    count = 0;
    return this;
  }
  
  public ClauseArgs or_group_start() {
    wh(QueryBuilder.OR);
    clause.append(QueryBuilder.STAR_PARENT);
    count = 0;
    return this;
  }
  
  public ClauseArgs group_end() {
    clause.append(QueryBuilder.END_PARENT);
    return this;
  }
  
  public void wh(String type) {
    if (count > 0) clause.append(type);
    count++;
  }
 
  public void clause(String type, String column, String op, Object value) {
    wh(type);
    clause.append(column.trim()).append(QueryBuilder.SPACE);
    clause.append(op.trim()).append(QueryBuilder.SPACE);
    clause.append(QueryBuilder.INTERROGATION);
    args.add(value);
  }
  
  public void clauseRaw(String type, String whereClause, Object... whereArgs) {
    if (whereClause != null && !whereClause.isEmpty()) {
      wh(type);
      clause.append(whereClause);
      OrmUtils.fill(args, whereArgs);
    }
  }
  
  public void clause_in(String type, String column, boolean not, Object... values) {
    wh(type);
    clause.append(column).append(not ? " NOT IN" : " IN").append(QueryBuilder.STAR_PARENT);
    
    for (int i = 0; i < values.length; i++) {
      if (i > 0) clause.append(QueryBuilder.COMMA);
      clause.append(QueryBuilder.INTERROGATION);
      args.add(values[i]);
    }
    
    clause.append(QueryBuilder.END_PARENT);
  }

  public ClauseArgs append(String str) {
    clause.append(str);
    return this;
  }
  
  public ClauseArgs append(CharSequence cs) {
    clause.append(cs);
    return this;
  }
  
  public ClauseArgs append(char c) {
    clause.append(c);
    return this;
  }
}
