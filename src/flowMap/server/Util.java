package flowMap.server;

import com.wm.app.b2b.server.HTTPDispatch;
import com.wm.app.b2b.server.JDBCConnectionManager;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
import com.wm.app.b2b.server.ns.Namespace;
import com.wm.data.IDataCursor;
import com.wm.jdbc.IJDBCFunctionConfig;
import com.wm.lang.ns.NSNode;
import com.wm.util.JournalLogger;
import com.wm.util.Values;
import flowMap.API;
import flowMap.Handler;
import flowMap.model.IS;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Properties;

public final class Util {

  public static String packageName;
  public static Properties properties;
  public static String url;
  public static API api;
  public static Handler handler;
  static {
    packageName = System.getProperty("user.dir")+"/packages/FlowMap";
    properties = new Properties();
    try {
      String propertyFileName = packageName+"/config/config.properties";
      File file = new File(propertyFileName);
      if (!file.exists())
        file.createNewFile();
      properties.load(new FileInputStream(propertyFileName));
    } catch (java.io.FileNotFoundException e) {
      throw new RuntimeException(e);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    String dbUrl = properties.getProperty("db.url");
    if (dbUrl == null) {dbUrl = "jdbc:oracle:thin:@eaiwbmd01:1530:EAID1"; saveProperty("db.url", dbUrl); }
    String dbDriver = properties.getProperty("db.driver");
    if (dbDriver == null) {dbDriver = "oracle.jdbc.driver.OracleDriver"; saveProperty("db.driver", dbDriver); }
    String dbUser = properties.getProperty("db.user");
    if (dbUser == null) {dbUser = "WMIS"; saveProperty("db.user", dbUser); }
    String dbPass = properties.getProperty("db.pass");
    if (dbPass == null) {dbPass = "WMIS"; saveProperty("db.pass", dbPass); }
    api = new API(dbDriver, dbUrl, dbUser, dbPass);
    url = properties.getProperty("URL_root");
    if (url == null) {url = "flowmap"; saveProperty("URL_root", url); }

    String viewsDir = Util.properties.getProperty("views.dir", "views");
    handler = new Handler(api, Util.packageName+"/"+viewsDir+"/");
    HTTPDispatch.removeHandler(Util.url);
    HTTPDispatch.addHandler(Util.url, handler);
    Util.log("HTTP handler "+Util.url+" added");

    //mapOrmliteLoggingToShittyWmLogging();
  }

  private static IS iS = null;
  public static String IS_id = "IS.id";
  public static IS getLocalIS() {
    if (iS == null) {
      String is_id_str = properties.getProperty(IS_id);
      try {
        if (is_id_str == null || (iS = api.getDao(IS.class).queryForId(Integer.parseInt(is_id_str))) == null) {
          //String stage_id = properties.getProperty("Stage.id");
          //if (stage_id == null)
          //  saveProperty("Stage.id", Util.api.createOrUpdate(Stage.class, new Stage("Development")).getId());
          iS = Import.importIS(null);
          saveProperty(IS_id, iS.getId()+"");
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return iS;
  }

  public static java.util.Date date = new java.util.Date(); // test initialization

  public static String getProperty(String key) throws ServiceException {
    return properties.getProperty(key);
  }
  public static void saveProperty(String key, String value) throws RuntimeException {
    try {
      properties.setProperty(key, value);
      String propertyFileName = packageName+"/config/config.properties";
      properties.store(new java.io.FileOutputStream(propertyFileName), null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


  public static String[] splitFullName(String fullName) {
    String[] split = fullName.split(":");
    if (split.length == 1)
      split = new String[]{"", fullName};
    return split;
  }

  public static String getConfigFileAsString(String fileName) throws ServiceException {
    try {
      java.io.File srcFile = new java.io.File(fileName);
      java.io.InputStream stream = new java.io.BufferedInputStream(
          new java.io.FileInputStream(srcFile), 1024);
      java.io.BufferedReader reader = new java.io.BufferedReader(
          new java.io.InputStreamReader(stream));
      StringBuilder sb = new StringBuilder();
      String line = reader.readLine();
      while (line != null) {
        sb.append(line).append('\n');
        line = reader.readLine();
      }
      return sb.toString();
    } catch (Exception e) {
      throw new ServiceException(e);
    }
  }
  public static List<Integer> executeUpdates(String[] sqls, boolean exitOnError) throws ServiceException {
    try {
      Connection conn = DriverManager.getConnection("jdbc:derby:FLOW_MAP;create=true");
      //Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
      List<Integer> results = new java.util.ArrayList<Integer>();
      try { // manage connection
        for (String sql : sqls) {
          try { // manage sql errors
            PreparedStatement query = conn.prepareStatement(sql);
            results.add(query.executeUpdate());
          } catch (Exception e) {
            if (exitOnError)
              throw e;
            else
              results.add(-1);
          }
        }
      } finally {
        conn.close();
      }
      return results;
    } catch (Exception e) {
      throw new ServiceException(e);
    }
  }

  public static void runAllQueries(IDataCursor cursor, String sqls) {
    for (String sql : sqls.split(";")) {
      sql = sql.replaceAll("(--.*|^ *)(\n|$)", "");
      if (!sql.matches(" *\n?")) {
        cursor.insertAfter("sql", sql);
        try {
          cursor.insertAfter("result", executeUpdate(sql));
        } catch (Exception e)  {
          cursor.insertAfter("error", e.getMessage());
        }
      }
    }
  }

  public static int executeUpdate(String sql) throws ServiceException {
    try {
      Connection conn = DriverManager.getConnection("jdbc:derby:FLOW_MAP;create=true");
      //Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
      try { // manage connection
        PreparedStatement query = conn.prepareStatement(sql);
        return query.executeUpdate();
      } finally {
        conn.close();
      }
    } catch (Exception e) {
      throw new ServiceException(e);
    }
  }

  public static NSNode getNode(String service) throws ServiceException {
    //Values output = Util.invoke( "wm.server.ns", "getNode", new String[]{"name", service});
    //return (NSNode) output.get("node");
    return Namespace.current().getNode(service);
  }

  public static String getPackageName(String service) throws ServiceException {
    return getNode(service).getPackage().getName();
  }

  public static final IJDBCFunctionConfig[] getJDBC() throws ServiceException {
    try {
      return JDBCConnectionManager.getAllJDBCFunctionConfigFromFile();
    } catch (Exception e) {
      throw new ServiceException(e);
    }
  }

  // one liner invoke(path, name, new String[]{label1, value1, ...})
  @SuppressWarnings("deprecation")
  public static Values invoke(String path, String name, String[] inputs) throws ServiceException {
    Values input = new Values();
    for (int i = 0; i < inputs.length; i += 2)
      input.put(inputs[i], inputs[i+1]);
    try {
      return Service.doInvoke(path, name, input);
    } catch (Exception e) {
      throw new ServiceException(e);
    }
  }
  @SuppressWarnings("deprecation")
  public static Values invoke(String path, String name) throws ServiceException {
    return invoke(path, name, new Values());
  }
  @SuppressWarnings("deprecation")
  public static Values invoke(String path, String name, Values input) throws ServiceException {
    try {
      return Service.doInvoke(path, name, input);
    } catch (Exception e) {
      throw new ServiceException(e);
    }
  }

  public static void printMethods(IDataCursor cursor, Object obj) throws ServiceException {
    cursor.insertAfter("Object class", obj.getClass().getName());
    for (java.lang.reflect.Method m : obj.getClass().getMethods()) {
      String key = m.getReturnType()+" "+m.getName()+"(";
      for (Class klass : m.getParameterTypes()) {
        key += klass.getName()+", ";
      }
      key += ")";
      if (m.getName().regionMatches(0, "get", 0, 3) //|| m.getName().regionMatches(0, "is", 0, 2))
          && m.getParameterTypes().length == 0) {
        try {
          cursor.insertAfter(key, m.invoke(obj));
        } catch (Exception e) {
          throw new ServiceException(e);
        }
        //if (m.getReturnType().equals(String.class) &&
      } else {
        //cursor.insertAfter(key, "Not run");
      }
    }
  }

  public static void log(String message) {
    JournalLogger.log(3, 90, 0, message);
  }
}
