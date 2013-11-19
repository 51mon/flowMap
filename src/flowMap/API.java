package flowMap;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.TableUtils;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import flowMap.model.Trigger;
import flowMap.model.*;
import flowMap.model.Adapter;
import flowMap.model.WmPackage;
import flowMap.model.flow.*;
import flowMap.model.tn.ProcessingRule;
import flowMap.server.Util;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.annotation.Annotation;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// com.j256.ormlite.jdbc.JdbcDatabaseResults (90)
public class API {
  //private ConnectionSource connectionSource;
  private JdbcPooledConnectionSource connectionSource;
  public String driver;
  public String dbURL;
  public String dbuser;
  public String dbpass;
  //private String viewsPath;
  //private MapItem<String,JadeTemplate> templates;
  public JdbcPooledConnectionSource getConnectionSource() {
    return connectionSource;
  }
  public void closeConnections() throws SQLException {
    connectionSource.close();
  }

  // Use directly only in the IDE, use get() in the IS
  public API(String driver, String dbURL, String dbuser, String dbpass) throws RuntimeException {
    this.driver = driver;
    this.dbURL = dbURL;
    this.dbuser = dbuser;
    this.dbpass = dbpass;
    //this.viewsPath = viewsPath;
    //templates = new HashMap<String,JadeTemplate>();
    try {
      //for (String name : new String[]{"index"})
      //  templates.put(name, Jade4J.getTemplate(viewsPath + "/"+name+".jade"));
      Class.forName(driver);
      //connectionSource = new JdbcConnectionSource("jdbc:oracle:thin:@"+dbName, "WMIS", "WMIS");
      connectionSource =
          new JdbcPooledConnectionSource(dbURL, dbuser, dbpass);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static Class[] models = new Class[]{
      AdapterType.class,
      Stage.class,
      OS.class,
      IS.class,
      WmPackage.class,
      IS_WmPackage.class,
      Node.class,
      Trigger.class,
      TriggerCondition.class,
      DocFilterPair.class,
      Adapter.class,
      Service.class,
      Record.class,
      //Document.class,
      Element.class,
      MapItem.class,
      Input.class,
      Mapping.class,
      Notification.class,
      ProcessingRule.class,
      Flow.class,
      FlowItem.class
  };
  //public String render(String name, HashMap<String, Object> model) {
  //  return Jade4J.render(templates.get(name), model);
  //}

  Map<Class,BaseDaoImpl> daos = new HashMap<Class,BaseDaoImpl>();
  public <T> BaseDaoImpl<T, Integer> getDao(Class<T> c) {
    try {
      if (daos.get(c) == null) {
          BaseDaoImpl dao = DaoManager.createDao(getConnectionSource(), c);
          daos.put(c, dao);
      }
      return daos.get(c);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
  public <T> void refresh(Class<T> c, T object) {
    try {
      getDao(c).refresh(object);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
  public <T> T createOrUpdate(Class<T> c, T object) throws SQLException {
    try {
      getDao(c).createOrUpdate(object);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return object;
  }
  public <T> void delete(Class<T> c, T object) throws SQLException {
    try {
      getDao(c).delete(object);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  // findOrCreate for named classes
  public OS findOrCreate(Dao<OS, Integer> dao, String arch, String name, String version) {
    try {
      List<OS> list =
          dao.queryBuilder().where()
              .eq(OS.ARCHITECTURE_COL, arch).and()
              .eq(OS.NAME_COL, name).and()
              .eq(OS.VERSION_COL, version)
              .query();
      return (list.size() != 0) ? list.get(0) : new OS(arch, name, version);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
  public OS findOrCreate(Dao<OS, Integer> dao, String hostName) throws SQLException {
    try {
      List<OS> list =
          dao.queryBuilder().where()
              .eq(OS.HOST_NAME_COL, hostName)
              .query();
      return (list.size() != 0) ? list.get(0) : new OS(hostName);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
public <T extends NamedIntId> T find(Class<T> c, String name)
    throws SQLException {
    BaseDaoImpl<T,Integer> dao = DaoManager.createDao(Util.api.getConnectionSource(), c);
    List<T> list =
        dao.queryBuilder().where()
            .eq(T.NAME_COL, name)
            .query();
    if (list.size() == 1) {
      return list.get(0);
    } else {
      throw new RuntimeException(list.size()+" "+c.getSimpleName()+" found for "+name);
    }
  }
  public <T extends NamedIntId> T findOrCreate(Class<T> c, String name)
      throws SQLException {
    BaseDaoImpl<T,Integer> dao = DaoManager.createDao(Util.api.getConnectionSource(), c);
    List<T> list =
        dao.queryBuilder().where()
            .eq(T.NAME_COL, name)
            .query();
    if (list.size() != 0) {
      return list.get(0);
    } else {
      try {
        T t = c.newInstance();
        t.setName(name);
        dao.createOrUpdate(t);
        return t;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  // findOrCreate for package items classes
  public <T extends Node> T findOrCreate(Class<T> c, String path, String name)
      throws SQLException {
    BaseDaoImpl<T, Integer> dao = DaoManager.createDao(Util.api.getConnectionSource(), c);
    List<T> list =
        dao.queryBuilder().where()
            .eq(T.PATH_COL, path).and()
            .eq(T.NAME_COL, name)
            .query();
    if (list.size() != 0) {
      return list.get(0);
    } else {
      try {
        T t = c.newInstance();
        t.setName(name);
        t.setPath(path);
        dao.createOrUpdate(t);
        return t;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
  public int deleteNode(String path, String name) throws SQLException {
    Dao<Node,Integer> dao = getDao(Node.class);
    // Delete the Node if it already exists
    List<Node> list = dao.queryBuilder().where()
            .eq(Node.PATH_COL, path).and()
            .eq(Node.NAME_COL, name)
            .query();
    int id = 0;
    for (Node oldNode : list) {
      id = oldNode.getId();
      dao.delete(oldNode);
    }
    return id;
  }

  int indexForConstraints = 0;
  private void addConstraint(Class model, String ref_key, Class ref_model) throws SQLException {
    String constraint = "ALTER TABLE "+tableName(model)+" add CONSTRAINT cons_"+indexForConstraints+++" FOREIGN KEY (\""+ref_key+"\") "+
        "REFERENCES "+tableName(ref_model)+" (\"id\") ON DELETE CASCADE";
    Log log = LogFactory.getLog(com.j256.ormlite.table.TableUtils.class);
    System.out.println(constraint);
    connectionSource.getReadWriteConnection().executeStatement(constraint, DatabaseConnection.DEFAULT_RESULT_FLAGS);
  }

  public static String tableName(Class modelClass) {
    for (Annotation annotation : modelClass.getAnnotations()) {
//      System.out.println(annotation.toString());
      if (annotation instanceof DatabaseTable) {
        DatabaseTable table = (DatabaseTable) annotation;
        return table.tableName();
      }
    }
    throw new RuntimeException("Class "+modelClass+" doesn't have the DatabaseTable annotation");
  }
  public void createTables() throws SQLException {
    for (Class modelClass : models) {
      TableUtils.createTableIfNotExists(connectionSource, modelClass);
    }
    addConstraint(Service.class,      Service.NODE_ID_COL,       Node.class);
    addConstraint(Trigger.class,      Trigger.NODE_ID_COL,       Node.class);
    addConstraint(Notification.class, Notification.NODE_ID_COL,  Node.class);
    addConstraint(Adapter.class,      Adapter.NODE_ID_COL,       Node.class);
    addConstraint(Adapter.class,      Adapter.ADAPTER_TYPE_ID_COL, AdapterType.class);
    addConstraint(Notification.class, Notification.ADAPTER_ID_COL, Adapter.class);
    addConstraint(Service.class,      Service.ADAPTER_ID_COL,    Adapter.class);
    addConstraint(Element.class,      Element.PARENT_ID_COL,     Element.class);
    addConstraint(MapItem.class,      MapItem.MAP_ID_COL,        Element.class);
    addConstraint(Mapping.class,      Mapping.MAP_COPY_FROM_COL, MapItem.class);
    addConstraint(Mapping.class,      Mapping.MAP_COPY_TO_COL,   MapItem.class);
    addConstraint(Mapping.class,      Mapping.MAP_UPDATE_COL,    MapItem.class);
    addConstraint(Mapping.class,      Mapping.PARENT_COL,        Mapping.class);
    addConstraint(Input.class,        Input.MAP_ITEM_ID_COL,     MapItem.class);
    addConstraint(Record.class,       Record.NODE_ID_COL,        Node.class);
    addConstraint(Record.class,       Record.PARENT_ID_COL,      Record.class);
    addConstraint(ProcessingRule.class, ProcessingRule.SERVICE_NODE_ID_COL, Node.class);
    addConstraint(ProcessingRule.class, ProcessingRule.RECORD_NODE_ID_COL, Node.class);
    addConstraint(Node.class,         Node.PACKAGE_ID_COL,       WmPackage.class);
    addConstraint(TriggerCondition.class, TriggerCondition.TRIGGER_ID_COL, Trigger.class);
    addConstraint(DocFilterPair.class, DocFilterPair.TRIGGER_CONDITION_ID_COL, TriggerCondition.class);
    addConstraint(Record.class,       Record.SERVICE_INPUT_ID_COL, Service.class);
    addConstraint(Record.class,       Record.SERVICE_OUTPUT_ID_COL, Service.class);
    addConstraint(Element.class,      Element.SERVICE_ID_COL,    Service.class);
    //addConstraint("services",       "adapter_type_to_service", "adapter_type_id", "adapter_types");
    addConstraint(IS_WmPackage.class, IS_WmPackage.IS_ID_COL,    IS.class);
    addConstraint(IS_WmPackage.class, IS_WmPackage.PACKAGE_ID_COL, WmPackage.class);
    addConstraint(IS.class,           IS.OS_ID_COL,              OS.class);
    addConstraint(OS.class,           OS.STAGE_ID_COL,           Stage.class);
    addConstraint(Input.class,        Input.PARENT_ID_COL,       Input.class);
    addConstraint(FlowItem.class,     FlowItem.FLOW_ID_COL,      Flow.class);
    addConstraint(FlowItem.class,     FlowItem.ELEMENT_ID_COL,   Element.class);
    addConstraint(FlowItem.class,     FlowItem.NODE_ID_COL,      Node.class);
  }
  public void dropTables() throws SQLException {
    //TableUtils.dropTable(connectionSource, AdapterService.class, true);
    // There is a circular dependency between service => adapter => service so we need to rm this constraint manually
    String[] dropConstraints = new String[]{
      //"ALTER TABLE adapters drop CONSTRAINT notification_to_adapter"
    };
    Log log = LogFactory.getLog(com.j256.ormlite.table.TableUtils.class);
    for (String constraint : dropConstraints) {
      log.info(constraint);
      connectionSource.getReadWriteConnection().executeStatement(constraint, DatabaseConnection.DEFAULT_RESULT_FLAGS);
    }
    for (int i = models.length-1; i>=0; i--) { // reverse order than creation to respect constraints
      TableUtils.dropTable(connectionSource, models[i], false); // ignoreError
    }
  }
}
