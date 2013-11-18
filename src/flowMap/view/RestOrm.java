package flowMap.view;

import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RestOrm {
  Pattern pattern = Pattern.compile("^/rest/([^/]*)(?:/([^/]*)(?:/(.*))?)?(?:[?](.*))?");
  JdbcPooledConnectionSource connectionSource;
  long maxRows = 25;
  public RestOrm(JdbcPooledConnectionSource connectionSource) {
    this.connectionSource = connectionSource;

  }
  public String render(String url) {
    String html = "not set";
    Matcher m = pattern.matcher(url);
    m.matches();
    Map<String,String> attributes = new HashMap<String,String>();
    if (m.groupCount() >= 7) {
      String qs = m.group(7);
      if (qs != null) {
        String[] splitQs = qs.split("&");
        for (String queryPart : splitQs) {
          String[] attr = queryPart.split("=");
          attributes.put(attr[0], attr[1]);
        }
      }
    }
    int page = 0;
    if (attributes.containsKey("page"))
      page = Integer.parseInt(attributes.get("page"));
    try {
      String className = "flowMap.model." + m.group(1);
      Class klass = Class.forName(className);
      //Util.log(className);
      QueryBuilder builder = DaoManager.createDao(connectionSource, klass).queryBuilder();
      builder.limit(maxRows);
      builder.offset(maxRows*page);
      if (attributes.size() != 0) {
        Where where = builder.where();
        Iterator<Map.Entry<String,String>> it = attributes.entrySet().iterator();
        Map.Entry<String,String> entry = it.next();
        where = where.eq(entry.getKey(), entry.getValue());
        while (it.hasNext()) {
          entry = it.next();
          where = where.and().eq(entry.getKey(), entry.getValue());
        }
      }
      List list = builder.query();
      System.out.println(list);
      //className.getFields()
    } catch (ClassNotFoundException e) {
      html = "{\"type\": \"error\", \"message\": \"Class does not exist\"}";
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return html;
  }
}
