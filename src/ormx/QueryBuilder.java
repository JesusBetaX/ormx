package ormx;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class QueryBuilder implements Cloneable {
  public static final String INSERT_INTO = "INSERT INTO ";
  public static final String VALUES = " VALUES";
  public static final String UPDATE = "UPDATE ";
  public static final String DELETE = "DELETE ";
  public static final String SELECT = "SELECT ";
  public static final String DISTINCT = "DISTINCT ";
  public static final String ALL = "* ";
  public static final String FROM = "FROM ";
  public static final String WHERE = " WHERE ";
  public static final String AND = " AND ";
  public static final String OR = " OR ";
  public static final String GROUP_BY = " GROUP BY ";
  public static final String HAVING = " HAVING ";
  public static final String ORDER_BY = " ORDER BY ";
  public static final String LIMIT = " LIMIT ";
  public static final String SET = " SET ";
  
  public static final String COMMA = ", ";
  public static final char STAR_PARENT = '(';
  public static final char END_PARENT = ')';
  public static final char SPACE = ' ';
  public static final char INTERROGATION = '?';
  public static final String EQ_INTERROGATION = "=?";
  
  private boolean distinct;
  private CharSequence[] columns;
  private CharSequence table;
  private final ClauseArgs where = new ClauseArgs();
  private final StringBuilder joins = new StringBuilder();
  private final StringBuilder groupBy = new StringBuilder();
  private final ClauseArgs having = new ClauseArgs();
  private final StringBuilder orderBy = new StringBuilder();
  private long index = -1;
  private long limit = 0;
  
  private final OrmDataBase db;
  private StringBuilder sb;
  
  public QueryBuilder(OrmDataBase db) {
    this.db = db;
  }

  public QueryBuilder set(QueryBuilder query) {
    distinct = query.distinct;
    columns = query.columns;
    table = query.table;
    where.set(query.where);
    joins.append(query.joins);
    groupBy.append(query.groupBy);
    having.set(query.having);
    orderBy.append(query.orderBy);
    index = query.index;
    limit = query.limit;
    return this;
  }
  
  public QueryBuilder reset() {
    distinct = false;
    columns = null;
    table = null;
    where.reset();
    OrmUtils.clear(joins);
    OrmUtils.clear(groupBy);
    having.reset();
    OrmUtils.clear(orderBy);
    index = -1;
    limit = 0;
    return this;
  }

  @Override public QueryBuilder clone() {
    return db.queryBuilder().set(this);
  }
  
  public OrmDataBase db() {
    return db;
  }
  
  public QueryBuilder distinct() {
    distinct = true;
    return this;
  }
  
  public QueryBuilder select(CharSequence... select) {
    columns = select;
    return this;
  }
  
  public QueryBuilder from(CharSequence from) {
    table = from;
    return this;
  }
  public QueryBuilder fromIsEmpty(CharSequence from) {
    if (OrmUtils.isEmpty(table)) from(from);
    return this;
  }
  public QueryBuilder from(Class<?> classOf) {
    from(OrmUtils.tableName(classOf));
    if (OrmUtils.isEmpty(columns)) {
      select(OrmObjectAdapter.of(classOf).fields);
    }
    return this;
  }
  public QueryBuilder fromIsEmpty(Class<?> classOf) {
    if (OrmUtils.isEmpty(table)) from(classOf);
    return this;
  }
  
  public QueryBuilder group_start() {
    where.group_start();
    return this;
  }
  public QueryBuilder or_group_start() {
    where.or_group_start();
    return this;
  }
  public QueryBuilder group_end() {
    where.group_end();
    return this;
  }
  private QueryBuilder _where(String type, String column, String op, Object value) {
    where.clause(type, column, op, value);
    return this;
  }
  private QueryBuilder _whereRaw(String type, String whereClause, Object... whereArgs) {
    where.clauseRaw(type, whereClause, whereArgs);
    return this;
  }
  public QueryBuilder whereRaw(String whereClause, Object... whereArgs) {
    return _whereRaw(AND, whereClause, whereArgs);
  }
  public QueryBuilder orWhereRaw(String whereClause, Object... whereArgs) {
    return _whereRaw(OR, whereClause, whereArgs);
  }
  public QueryBuilder where(String column, String op, Object value) {
    return _where(AND, column, op, value);
  }
  public QueryBuilder where(String column, Object value) {
     return where(column, "=", value);
  }
  
  public QueryBuilder like(String column, Object value) {
    return where(column, "LIKE", "%"+value+"%");
  }
  public QueryBuilder not_like(String column, Object value) {
    return where(column, "NOT LIKE", "%"+value+"%");
  }
  
  public QueryBuilder or_where(String column, String op, Object value) {
    return _where(OR, column, op, value);
  }
  public QueryBuilder or_where(String column, Object value) {
     return or_where(column, "=", value);
  }
  public QueryBuilder or_like(String column, Object value) {
    return or_where(column, "LIKE", "%"+value+"%");
  }
  public QueryBuilder or_not_like(String column, Object value) {
    return or_where(column, "NOT LIKE", "%"+value+"%");
  }
  
  
  private QueryBuilder _where_in(String wh, String column, boolean not, Object... values) {
    where.clause_in(wh, column, not, values);
    return this;
  }
  public QueryBuilder where_in(String column, Object... values) {
    return _where_in(AND, column, false, values);
  }
  public QueryBuilder where_not_in(String column, Object... values) {
    return _where_in(AND, column, true, values);
  }
  public QueryBuilder or_where_in(String column, Object... values) {
    return _where_in(OR, column, false, values);
  } 
  public QueryBuilder or_where_not_in(String column, Object... values) {
    return _where_in(OR, column, true, values);
  }
  
  public QueryBuilder join(String table, String clause, String type) {
    if (!OrmUtils.isEmpty(type)) joins.append(SPACE).append(type);
    joins.append(" JOIN ").append(table.trim());
    joins.append(" ON ").append(clause.trim());
    return this;
  }
  public QueryBuilder join(String table, String clause) {
    return join(table, clause, null);
  }
  
  public QueryBuilder group_by(String group_by) {
    if (groupBy.length() > 0) groupBy.append(COMMA);
    groupBy.append(group_by);
    return this;
  }
  
  private QueryBuilder _having(String type, String column, String op, Object value) {
    having.clause(type, column, op, value);
    return this;
  }
  public QueryBuilder having(String column, String op, Object value) {
    return _having(AND, column, op, value);
  }
  public QueryBuilder or_having(String column, String op, Object value) {
    return _having(OR, column, op, value);
  }
  
  public QueryBuilder order_by(String order_by, String direction) {
    if (orderBy.length() > 0) orderBy.append(COMMA);
    orderBy.append(order_by).append(SPACE).append(direction);
    return this;
  }
  
  public QueryBuilder limit(long value) {
    limit = value;
    return this;
  }
  public QueryBuilder limit(long index, long limit) {
    this.index = index;
    return limit(limit);
  }
 
  private StringBuilder stringBuilder() {
    if (sb == null) sb = new StringBuilder(120);
    else OrmUtils.clear(sb);
    return sb;
  }
  
  public Object[] compileSelect(StringBuilder sql) {
    if (OrmUtils.isEmpty(groupBy) && !OrmUtils.isEmpty(having.clause)) {
      throw new IllegalArgumentException(
              "HAVING clauses are only permitted when using a groupBy clause");
    }

    sql.append(SELECT);
    if (distinct) sql.append(DISTINCT);

    if (!OrmUtils.isEmpty(columns)) OrmUtils.appendColumns(sql, columns);
    else sql.append(ALL);
    
    sql.append(FROM).append(table);
    OrmUtils.appendJoin(sql, joins);
    OrmUtils.appendClause(sql, WHERE, where.clause);
    OrmUtils.appendClause(sql, GROUP_BY, groupBy);
    OrmUtils.appendClause(sql, HAVING, having.clause);
    OrmUtils.appendClause(sql, ORDER_BY, orderBy);
    OrmUtils.appendLimit(sql, index, limit);
    
    return OrmUtils.concatToArray(where.args, having.args);
  }
  
  public OrmResult get() throws SQLException {
    try {
      StringBuilder sql = stringBuilder();
      Object[] args = compileSelect(sql);
      return db.query(sql.toString(), args);
    } finally {
      recycler();
    }
  }
  public OrmResult paginate(SearchCriteria q) throws SQLException {
    return q.paginate(this);
  }
  
  public <V> List<V> get_list(Class<V> classOf) throws SQLException {
    return fromIsEmpty(classOf).get().list(classOf);
  }
  public <V> OrmIterator<V> get_it(Class<V> classOf) throws SQLException {
    return fromIsEmpty(classOf).get().it(classOf);
  }
  public <V> V get_first(Class<V> classOf) throws SQLException {
    return fromIsEmpty(classOf).get().row(classOf);
  }

  public long get_select_count(String selection) throws SQLException {
    OrmResult result = null;
    try {
      result = select("COUNT("+selection+") AS total_rows").get();
      return result.next() ? result.getLong(/*"total_rows"*/1) : -1;
    } finally {
      OrmUtils.close(result);
    }
  }
  public long get_select_count() throws SQLException {
    return get_select_count("*");
  }
  
//  public OrmResultSet findById(Object value) throws SQLException {
//    return findByColumn(OrmUtils.isEmpty(key) ? "id" : key, value);
//  }
  
  public Object[] compileDelete(StringBuilder sql) {
    sql.append(DELETE).append(FROM).append(table);
    OrmUtils.appendClause(sql, WHERE, where.clause);
    OrmUtils.appendLimit(sql, index, limit);
    return OrmUtils.toArray(where.args);
  }
  
  public int delete() throws SQLException {
    try {
      StringBuilder sql = stringBuilder();
      Object[] args = compileDelete(sql);
      return db.executeUpdate(sql.toString(), args);
    } finally {
      recycler();
    }
  }
  
  public int delete(Class<?> classOf) throws SQLException {
    return from(classOf).delete();
  }
  
  public int delete(String table) throws SQLException {
    return from(table).delete();
  }
 
  public Object[] compileUpdate(final StringBuilder sql, final Map<String, Object> values) {
    sql.append(UPDATE).append(table).append(SET);

    int setValuesSize = values.size();
    int bindArgsSize = (where.args.isEmpty()) ? setValuesSize : (setValuesSize + where.args.size());
    final Object[] bindArgs = new Object[bindArgsSize];
    
    int i = 0;
    for (String colName : values.keySet()) {
      if (i > 0) sql.append(COMMA);
      sql.append(colName);
      bindArgs[i++] = values.get(colName);
      sql.append(EQ_INTERROGATION);
    }
    
    if (!where.args.isEmpty()) {
      for (i = setValuesSize; i < bindArgsSize; i++) {
        bindArgs[i] = where.args.get(i - setValuesSize);
      }
    }
    
    OrmUtils.appendClause(sql, WHERE, where.clause);
    OrmUtils.appendClause(sql, ORDER_BY, orderBy);
    OrmUtils.appendLimit(sql, index, limit);
    
    return bindArgs;
  }
  
  public int update(final Map<String, Object> values) throws SQLException {
    try {
      StringBuilder sql = stringBuilder();
      Object[] args = compileUpdate(sql, values);
      return db.executeUpdate(sql.toString(), args);
    } finally {
      recycler();
    }
  }
  
  public <V> int update(final V obj) throws SQLException {
    OrmDao<V, ?> dao = db.dao((Class<V>) obj.getClass());
    return fromIsEmpty(dao.table).update(dao.vars(obj));
  }
  
  public Object[] compileInsert(final StringBuilder sql, final Map<String, Object> values) {
    sql.append(INSERT_INTO).append(table).append(STAR_PARENT);

    int size = (values != null && values.size() > 0) ? values.size() : 0;
    final Object[] bindArgs = new Object[size];
    int i = 0;
    for (String colName : values.keySet()) {
      if (i > 0) sql.append(COMMA);
      sql.append(colName);
      bindArgs[i++] = values.get(colName);
    }

    sql.append(END_PARENT).append(VALUES).append(STAR_PARENT);
    for (i = 0; i < size; i++) {
      if (i > 0) sql.append(COMMA);
      sql.append(INTERROGATION);
    }
    sql.append(END_PARENT);
    
    return bindArgs;
  }
  
  public long insert(final Map<String, Object> values) throws SQLException {
    try {
      StringBuilder sql = stringBuilder();
      Object[] args = compileInsert(sql, values);
      return db.executeInsert(sql.toString(), args);
    } finally {
      recycler();
    }
  }
  
  public <V> boolean insert(final V obj) throws SQLException {
    OrmDao<V, ?> dao = db.dao((Class<V>) obj.getClass());
    return dao.insert(fromIsEmpty(dao.table), obj);
  }
  
  public OrmResult executeInsert(final Map<String, Object> values) throws SQLException {
    try {
      StringBuilder sql = stringBuilder();
      Object[] args = compileInsert(sql, values);
      return db.executeAndReturnKeys(sql.toString(), args);
    } finally {
      recycler();
    }
  }
  
  public void recycler() {
    db.recycler(this);
  }
   
  
//  public String escape_value(Object value) {
//    if (value == null) return "NULL";
//    
//    String str = value.toString();
//    
//    if (value instanceof Number) return str;
//    
//    return new StringBuilder(str.length())
//            .append("'")
//            .append(str.replace("'", "\\'"))
//            .append("'")
//            .toString();
//  }
}
