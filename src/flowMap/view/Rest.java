package flowMap.view;

import com.google.gson.stream.JsonWriter;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.table.TableInfo;
import com.wm.net.HttpHeader;
import flowMap.API;
import flowMap.model.*;
import flowMap.model.flow.*;
import org.apache.commons.lang3.StringUtils;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Rest {
  API api;
  Map<String,TableInfo> tablesInfos;
  // rest routes based on http://guides.rubyonrails.org/routing.html
  Pattern pattern = Pattern.compile("^/rest/([^/]*)(/(\\d+)(?:/(edit))?|new)?(?:[?](.*))?");
  Connection conn;
  long maxRows = 25;
  public Rest(API api) {
    this.api = api;
    try {
      Class.forName(api.driver);
      conn = DriverManager.getConnection(api.dbURL, api.dbuser, api.dbpass);
      tablesInfos = new HashMap<String, TableInfo>();
      for (Class c : API.models) {
        tablesInfos.put(c.getSimpleName(), ((BaseDaoImpl)api.getDao(c)).getTableInfo());
      }
      //tablesInfos.put("", ((BaseDaoImpl<, Integer>)api.getDao()).getTableInfo());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  public String render(int verb, String url, String body, OutputStream os) {
    String html = "not set";
    Matcher m = pattern.matcher(url);
    m.matches();
    String newOrUseless = m.group(2);
    String id = m.group(3);
    String edit = m.group(4); // edit or null
    String qs = m.group(5);
    try {
      String className = "flowMap.model." + m.group(1);
      Class klass = Class.forName(className);
      TableInfo tableInfo = tablesInfos.get(m.group(1));
      String tableName = tableInfo.getTableName();
      switch (verb) {
        case HttpHeader.GET: // get or search
          if ("new".equals(newOrUseless)) {
            // TODO NEW
          } else if (id == null) { // list / query
            Map <String,String> attributes = new HashMap<String,String>();
            String where = "";
            if (qs != null) {
              String[] splitQs = qs.split("&");
              for (int i = 0; i < qs.length(); i++) {
                String[] attr = splitQs[i].split("=");
                FieldType type = tableInfo.getFieldTypeByColumnName(attr[0]);
                if (type == null) {
                  return error("Unknown field "+attr[0]+" in class "+m.group(1));
                }
                splitQs[i] = attr[0] + " = ";
                if (type.getSqlType() == SqlType.INTEGER || type.getSqlType() == SqlType.FLOAT
                    || type.getSqlType() == SqlType.BOOLEAN) {
                  splitQs[i] += attr[1];
                } else if (type.getSqlType() == SqlType.STRING) {
                  splitQs[i] += "'"+attr[1]+"'";
                } else if (type.getSqlType() == SqlType.BOOLEAN) {
                  return error("Unmanaged type "+type.getSqlType());
                }
              }
              where = "where "+ StringUtils.join(splitQs, " and ");
            }
            int page = 1;
            if (attributes.containsKey("page"))
              page = Integer.parseInt(attributes.get("page"));
            PreparedStatement statement = conn.prepareStatement(
                "SELECT * FROM (\n" +
                " SELECT a.*, rownum r__ FROM (\n" +
                "  SELECT * FROM " + tableName + " " + where +
                " ) a WHERE rownum < ((" + page + " * " + maxRows + ") + 1 )\n" +
                ")\n" +
                "WHERE r__ >= (((" + page + "-1) * " + maxRows + ") + 1)");
            ResultSet results = statement.executeQuery();
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.beginArray();
            while(results.next()) {
              writer.beginObject();
              // loop rs.getResultSetMetadata columns
              for(int idx=1; idx<=results.getMetaData().getColumnCount()-1; idx++) {
                writer.name(results.getMetaData().getColumnLabel(idx)); // write key:value pairs
                writer.value(results.getString(idx));
              }
              writer.endObject();
            }
            writer.endArray();
            writer.close();
            os.flush();
          } else if ("edit".equals(edit)) {
            // TODO EDIT ID
          } else {
            // TODO GET ID
          }
          break;
        case HttpHeader.DELETE: break;
        case HttpHeader.POST: break;
        case HttpHeader.PUT: break;
      }
      //className.getFields()
    } catch (ClassNotFoundException e) {
      return error("Class does not exist");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return html;
  }
  private String error(String msg) {
    return "{\"type\": \"error\", \"message\": \""+msg+"\"}";
  }
}
