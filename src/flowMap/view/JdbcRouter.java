package flowMap.view;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.TableInfo;
import flowMap.API;
import flowMap.Handler;
import flowMap.server.Util;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

public class JdbcRouter extends Router {
  Map<Class,String> classesToTables = new HashMap<Class,String>();
  Map<Class,Map<String,String>> foreignColNames = new HashMap<Class, Map<String,String>>();
  Map<Class,Map<String,String>> foreignTableNames = new HashMap<Class, Map<String,String>>();
  private void buildForeignCols(Class klass) {
    foreignColNames.put(klass, new HashMap<String,String>());
    foreignTableNames.put(klass, new HashMap<String,String>());

    Map<String,String> foreignFieldNames = new HashMap<String,String>();
    for (Field f1 : klass.getDeclaredFields()) {
      for (Annotation anno : f1.getDeclaredAnnotations()) {
        if (anno instanceof ForeignCollectionField) {
          ForeignCollectionField foreignColl = (ForeignCollectionField) anno;
          if (foreignColl.foreignFieldName() != null && !foreignColl.foreignFieldName().equals("")) {
            foreignFieldNames.put(f1.getName(), foreignColl.foreignFieldName());
            //System.out.println(f1.getName()+" "+ foreignColl.foreignFieldName());
          }
        }
      }
    }

    TableInfo tableInfo = handler.api.getDao(klass).getTableInfo();
    for (FieldType fieldType : tableInfo.getForeignCollections()) {
      if (fieldType.getField().getGenericType() instanceof ParameterizedType) {
        Field field = fieldType.getField();
        Type[] genericArguments = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
        if (genericArguments.length != 1)
          throw new RuntimeException("Wrong number of genericArguments in "+field.getName()+" of "+klass.getName());
        Class foreignClass = (Class) genericArguments[0];
        String foreignTableName = ((DatabaseTable)foreignClass.getAnnotation(DatabaseTable.class)).tableName();
        foreignTableNames.get(klass).put(field.getName(), foreignTableName);

        TableInfo foreignInfo = handler.api.getDao(foreignClass).getTableInfo();
        String foreignFieldName = foreignFieldNames.get(field.getName());
        //System.out.println("foreign name: "+foreignFieldName+" "+fieldType.getForeignIdField());
        if (foreignFieldName != null) { // Get it by name
          for (FieldType foreignFieldType : foreignInfo.getFieldTypes()) {
            if (foreignFieldType.getFieldName().equals(foreignFieldName)) {
              foreignColNames.get(klass).put(field.getName(), foreignFieldType.getColumnName());
              break;
            }
          }
        } else { // Get it by type
          for (FieldType foreignFieldType : foreignInfo.getFieldTypes()) {
            System.out.println("search type "+klass+ " found: "+foreignFieldType.getField().getType());
            if (foreignFieldType.getField().getType().equals(klass)) {
              //              System.out.println("found "+foreignFieldType.getColumnName());
              foreignColNames.get(klass).put(field.getName(), foreignFieldType.getColumnName());
              break;
            }
          }
        }
      }
    }
  }
  public JdbcRouter(Handler handler, String fullUrl, String url, Map <String,Object> attributes, String contentType, String accept, String body,
                    String tableName) {
    super(handler, fullUrl, url, attributes, contentType, accept, body, tableName);
    SortedSet<String> tables = new TreeSet<String>(handler.tablesToClasses.keySet());
    model.put("tables", tables);
    for (Class modelClass : API.models) {
      classesToTables.put(modelClass, API.tableName(modelClass));
      buildForeignCols(modelClass);
    }
  }
  public String show(String id){
    try {
      //dao.queryForId(id);
      PreparedStatement statement = handler.conn.prepareStatement(
          "SELECT * FROM "+tableName +" WHERE \"id\" = "+id);
      ResultSet results = statement.executeQuery();
      StringBuilder htmlData = new StringBuilder("<table class='box'>");
      if (!results.next()) {
        return "No Results";
      }
      String obj_id = "";
      for (int i = 1; i <= results.getMetaData().getColumnCount(); i++) {
        if (results.getMetaData().getColumnName(i).equals("id"))
          obj_id = results.getString(i);
        htmlData.append("<tr><th align='left'>");
        String colName = results.getMetaData().getColumnName(i);
        htmlData.append(colName.replaceFirst("_id$", "").replace('_', ' ')).append("</th><td>");
        FieldType fieldType = tableInfo.getFieldTypeByColumnName(colName);
        if (fieldType.isForeign() && results.getString(i) != null)
          htmlData.append("<a href='/"+Util.url+"/"+fieldType.getForeignIdField().getTableName().toLowerCase()+"/")
              .append(results.getString(i)).append("'>").append(results.getString(i)).append("</a>");
        else
          htmlData.append(results.getString(i));
        htmlData.append("</td></tr>");
      }
      for (FieldType fieldType : tableInfo.getForeignCollections()) {
        htmlData.append("<tr><th align='left'>").append(fieldType.getFieldName());
        htmlData.append("</th><td><a href='/").append(Util.url+"/").append(foreignTableNames.get(klass).get(fieldType.getFieldName())).append("?")
            .append(foreignColNames.get(klass).get(fieldType.getFieldName())).append("=").append(obj_id).append("'>Show</a></td></tr>");
      }
      htmlData.append("</table>");
      model.put("body", htmlData.toString());
      String html = handler.render("layout.jade", model);
      return html;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
  public String list(){
    try {
      List<String> whereCriterions = new ArrayList<String>();
      for (Map.Entry<String,Object> entry : attributes.entrySet()) {
        if (entry.getKey().equals("page"))
          continue;
        FieldType type = tableInfo.getFieldTypeByColumnName(entry.getKey());
        String criterion = "\""+entry.getKey()+"\" ";
        if (type.getSqlType() == SqlType.INTEGER || type.getSqlType() == SqlType.FLOAT || type.getSqlType() == SqlType.BOOLEAN) {
          criterion += "= "+entry.getValue();
        } else if (type.getSqlType() == SqlType.STRING) {
          criterion += "like '"+entry.getValue()+"'";
        } else if (type.getSqlType() == SqlType.BOOLEAN) {
          criterion += "= "+("yes".equals(entry.getValue()) ? 1 : 0);
        } else {
          return "Unmanaged type "+type.getSqlType();
        }
        whereCriterions.add(criterion);
      }
      String where = (whereCriterions.size() == 0) ? "" : "where "+ StringUtils.join(whereCriterions, " and ");
      int page = (attributes.containsKey("page")) ? Integer.parseInt((String)attributes.get("page")) : 1;
      PreparedStatement statement = handler.conn.prepareStatement(
          "SELECT * FROM (\n" +
              " SELECT a.*, rownum r__ FROM (\n" +
              "  SELECT * FROM " + tableName + " " + where +
              " ) a WHERE rownum < ((" + page + " * " + maxRows + ") + 1 )\n" +
              ")\n" +
              "WHERE r__ >= (((" + page + "-1) * " + maxRows + ") + 1)");
      ResultSet results = statement.executeQuery();
      int nbCols = results.getMetaData().getColumnCount();
      StringBuilder queryHtml = new StringBuilder("<form method='get'><table class='box'>");
      StringBuilder htmlData = new StringBuilder("<table class='box'>");
      htmlData.append("<tr>");
      Map<Integer,String> foreignHrefs = new HashMap<Integer,String>();
      for (int i = 1; i < nbCols; i++) {
        String colName = results.getMetaData().getColumnName(i);
        FieldType fieldType = tableInfo.getFieldTypeByColumnName(colName);
        if (i % 3 == 1)
          if (i == 1)
            queryHtml.append("<tr>");
          else if (i == nbCols-1)
            queryHtml.append("</tr>");
          else
            queryHtml.append("</tr><tr>");
        queryHtml.append("<th>"+colName+"</th><td><input name='"+colName+"'");
        if (attributes.get(colName) != null)
          queryHtml.append(" value='"+attributes.get(colName)+"'");
        queryHtml.append("></td>");
        if (fieldType.isForeign()) {
          colName = colName.replaceFirst("_id$", "");
          foreignHrefs.put(i, "/"+ Util.url+"/"+fieldType.getForeignIdField().getTableName().toLowerCase()+"/");
        }
        htmlData.append("<th>").append(colName.replace('_', ' ')).append("</th>");
      }
      queryHtml.append("<tr><td colspan='6' align='center'><input type='submit'></td></tr></table></form><br>");
      for (FieldType type : tableInfo.getFieldTypes()) {
        if (type.isForeignCollection()) {
          htmlData.append("<th>").append(type.getFieldName()).append("</th>");
        }
      }
      htmlData.append("</tr>");
      int nbRows = 0;
      while (results.next()) {
        nbRows++;
        htmlData.append("<tr");
        if (nbRows % 2 == 0)
          htmlData.append(" class='even'");
        htmlData.append(">");
        String obj_id = "";
        for (int i = 1; i < nbCols; i++) {
          if (results.getMetaData().getColumnName(i).equals("id"))
            obj_id = results.getString(i);
          int type = results.getMetaData().getColumnType(i);
          htmlData.append("<td");
          if (type == Types.INTEGER || type == Types.DECIMAL || type == Types.FLOAT || type == Types.NUMERIC
              || type == Types.NULL || type == Types.CHAR)
            htmlData.append(" align='right'");
          htmlData.append(">");
          if (foreignHrefs.get(i) != null && results.getString(i) != null) {
            htmlData.append("<a href='").append(foreignHrefs.get(i)).append(results.getString(i)).append("'>")
                .append(results.getString(i)).append("</a>");
          } else {
            if (results.getMetaData().getColumnType(i) == Types.VARCHAR && results.getString(i) != null)
              htmlData.append(results.getString(i).replaceAll("\n", "<br>"));
            else
              htmlData.append(results.getString(i));
          }
          htmlData.append("</td>");
        }
        for (FieldType fieldType : tableInfo.getForeignCollections()) {
          htmlData.append("<td><a href='").append(foreignTableNames.get(klass).get(fieldType.getFieldName())).append("?")
              .append(foreignColNames.get(klass).get(fieldType.getFieldName())).append("=").append(obj_id).append("'>Show</a></td>");
        }
        htmlData.append("</tr>");
      }
      //htmlData.append("<tr><td colspan='"+nbCols+"'>"+nbRows+"</td></tr>");
      if (page > 1 || nbRows == maxRows) {
        htmlData.append("<tr><td>");
        if (page > 1)
          htmlData.append("<a href='/"+fullUrl+"?page="+(page-1)+"'>Previous page</a> ");
        htmlData.append("</td><td colspan='"+(nbCols-1)+"'>");
        if (nbRows == maxRows)
          htmlData.append("<a href='/"+fullUrl+"?page="+(page+1)+"'>Next page</a>");
        htmlData.append("</td></tr>");
      }
      htmlData.append("</table>");
      model.put("body", queryHtml.toString()+htmlData.toString());
      return handler.render("layout.jade", model);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
  public String create() {
    throw new RuntimeException("TODO");
  };
  public String delete(String id){
    throw new RuntimeException("TODO");
  };
  public String update(){
    throw new RuntimeException("TODO");
  };
}
