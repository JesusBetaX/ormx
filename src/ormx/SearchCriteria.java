package ormx;

import java.io.PrintStream;
import java.sql.SQLException;
import java.util.HashMap;

/**
 *
 * @author jesus
 */
public class SearchCriteria {

  private long mpage = 1;
  private long mlimit = 1000;
  private long mtotal_rows = -1;
  private final HashMap<String, Object> params = new HashMap<String, Object>();

  /**
   * Calcula el número total de páginas. Redondear fracciones hacia arriba
   */
  public long pages() {
    if (mtotal_rows == 0 || mlimit == 0) return 0;
    double dif = (double)mtotal_rows / (double)mlimit;
    return (long) Math.ceil(dif);
  }

  public void setTotalRows(long total_rows) {
    mtotal_rows = total_rows;
  }

  public long getTotalRows() {
    return mtotal_rows;
  }

  public void setLimit(long limit) {
    mlimit = limit;
  }

  public long getLimit() {
    return mlimit;
  }
  
  public void setPage(long value) {
    mpage = value;
  }
  
  public long getPage() {
    return mpage;
  }
  
  public long getIndex() {
    return pageToIndex(mpage);
  }
  
  public long pageToIndex(long page) {
    return mlimit * (page - 1);
  }

  public OrmResult paginate(QueryBuilder query) throws SQLException {
    setTotalRows( query.clone().get_select_count() );
    
    if (query.db().isDebug()) print(System.out);
    
    return query.limit(getIndex(), mlimit).get();
  }
  
  public void print(PrintStream out) {
    StringBuilder sb = new StringBuilder();
    sb.append("#SearchCriteria: ");
    
    long len = pages();
    for (long page = 1; page <= len; page++) {
      if (page > 1) sb.append(" ");
      
      long p_index = pageToIndex(page);
      
      boolean current = mpage == page;
      
      sb.append(current ? '▌' : '[');
      sb.append(page).append("=>").append(p_index);
      //sb.append(":").append(page_index + limit);
      sb.append(current ? '▐' : ']');
    }
  
    sb.append("   ");
    sb.append(" page:").append(mpage);
    sb.append(", index:").append(getIndex());
    sb.append(", limit:").append(mlimit);
    sb.append(", total_rows:").append(mtotal_rows);
    out.println(sb);
  }
  
  public boolean nextPage() {
    long next = mpage + 1;
    if (next <= pages()) {
      mpage = next;
      return true;
    }
    return false;
  }
  
  public boolean previousPage() {
    long next = mpage - 1;
    if (next >= 1) {
      mpage = next;
      return true;
    }
    return false;
  }
  
  public HashMap<String, Object> params() {
    return params;
  }
  
  public boolean containsParam(String key) {
    return params.containsKey(key);
  }
  
  public <V> V get(String key, V defaultValue) {
    V value = (V) params.get(key);
    return value == null ? defaultValue : value;
  }
  
  public <V> V get(String key) {
    return get(key, null);
  }
  
  public <V> V put(String key, V value) {
    return (V) params.put(key, value);
  }
  
  public void clearParams() {
    params.clear();
  }
  
  @Override
  public String toString() {
    return "SearchCriteria{" + "total_rows=" + mtotal_rows + ", limit=" + mlimit + ", page=" + mpage + ", params=" + params + '}';
  }
}
