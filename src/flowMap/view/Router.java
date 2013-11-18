package flowMap.view;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.table.TableInfo;
import flowMap.API;
import flowMap.Handler;
import flowMap.server.Util;

import java.util.HashMap;
import java.util.Map;

public abstract class Router {
  String fullUrl;
  String url;
  Map <String,Object> attributes;
  String contentType;
  String accept;
  String body;
  String tableName;
  Map<String, Object> model;
  long maxRows = 25;
  Handler handler;
  TableInfo tableInfo;
  BaseDaoImpl dao;
  Class klass;
  public Router(Handler handler, String fullUrl, String url, Map <String,Object> attributes, String contentType, String accept, String body,
                String tableName) {
    this.handler = handler;
    this.fullUrl = fullUrl;
    this.url = url;
    this.attributes = attributes;
    this.contentType = contentType;
    this.accept = accept;
    this.body = body;
    this.tableName = tableName;
    klass = handler.tablesToClasses.get(tableName);
    dao = handler.api.getDao(klass);
    tableInfo = dao.getTableInfo();
    model = new HashMap<String, Object>();
    model.put("urlRoot", Util.url);
    model.put("svcPath", handler.svcPath);

  }
  public abstract String show(String id);
  public abstract String list();
  public abstract String create();
  public abstract String delete(String id);
  public abstract String update();
}
