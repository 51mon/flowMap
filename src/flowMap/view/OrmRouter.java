package flowMap.view;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import flowMap.Handler;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class OrmRouter extends Router {
  public OrmRouter(Handler handler, String fullUrl, String url, Map<String, Object> attributes, String contentType, String accept, String body, String tableName) {
    super(handler, fullUrl, url, attributes, contentType, accept, body, tableName);
  }

  @Override
  public String show(String id) {
    return null;
  }

  @Override
  public String list() { // Oracle doesn't manage "natively" offset, and so do ormlite ...
    int page = (attributes.containsKey("page")) ? Integer.parseInt((String)attributes.get("page")) : 1;
    attributes.remove("page");
    QueryBuilder qb = dao.queryBuilder();
    Where where = qb.where();
    try {
      for (Map.Entry<String,Object> entry : attributes.entrySet()) {
        if (entry.getKey().equals("AND")) {
          where = where.and();
        } else if (entry.getKey().equals("OR")) {
          where = where.or();
        } else {
          Map<String,String> criteria = (Map<String,String>) entry.getValue();
          if (criteria.get("op").equals("EQ")) {
            where = where.eq(entry.getKey(), criteria.get("value"));
          }
        }
      }
      List results = attributes.size() == 0 ? qb.query() : where.query();

      model.put("results", results);
      String view = handler.render(tableName+"/list.jade", model);
      model.put("body", view);
      return handler.render("layout.jade", model);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String create() {
    return null;
  }

  @Override
  public String delete(String id) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public String update() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }
}
