package flowMap;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.TableInfo;
import com.j256.ormlite.table.TableUtils;
import com.wm.data.IDataCursor;
import flowMap.model.*;
import flowMap.model.flow.Element;
import flowMap.model.flow.MapItem;
import flowMap.server.Util;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.sql.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestOracle {
  public static void main(String[] args) throws SQLException, Exception {
    System.out.println("Start");

    /*Class.forName("oracle.jdbc.driver.OracleDriver");
    String dbName = "eaiwbmd01:1530:EAID1";

    ConnectionSource connectionSource =
        new JdbcConnectionSource("jdbc:oracle:thin:@"+dbName, "WMIS", "WMIS");


    TableUtils.dropTable(connectionSource, TestInsert.class, true);
    TableUtils.createTableIfNotExists(connectionSource, TestInsert.class);

    TestInsert test = new TestInsert();
    test.setSomeBoolean(true);
    DaoManager.createDao(connectionSource, TestInsert.class).create(test);
    System.exit(0);*/

    API api = new API("oracle.jdbc.driver.OracleDriver",
        "jdbc:oracle:thin:@eaiwbmd01:1530:EAID1", "WMIS", "WMIS");


//    Dao<Node,Integer> _dao = api.getDao(Node.class);
//    List<Node> _list = _dao.queryBuilder().where()
//        .eq(Node.PATH_COL, "flowMap").and()
//        .eq(Node.NAME_COL, "getPubs")
//        .query();
//    for (Node node : _list) {
//      System.out.println(node.getService().getFlowRoot());
//    }
//    for (Annotation annotation : Class.forName("flowMap.model.Node").getAnnotations()) {
//      System.out.println(annotation.toString());
//      if (annotation instanceof DatabaseTable) {
//        DatabaseTable table = (DatabaseTable) annotation;
//        System.out.println(table.tableName());
//      }
//    }
//    System.out.println(IOUtils.toString(api.getClass().getClassLoader().getResourceAsStream("flowMap/Test.java")));

//    StringBuilder sb = new StringBuilder("<ul>");
//    Enumeration<URL> ress = api.getClass().getClassLoader().getResources("flowMap/static");
//    if (ress.hasMoreElements()) {
//      File folder = new File(ress.nextElement().toURI());
//      for (String file : folder.list())
//        sb.append("<li>"+file+"</li>");
//    }
//    System.out.println(sb.append("</ul>").toString());

    Map<String, Object> attributes = new HashMap<String, Object>();
    Handler.match(attributes, "test[sub][sub2]", "a");
    System.out.println(attributes);

    System.exit(0);

    api.dropTables();
    api.createTables();
    System.exit(0);

    Dao<Service,Integer> dao = api.getDao(Service.class);
    // Delete the Node if it already exists
    List<Service> list =
        dao.queryBuilder().where()
            .eq(Service.NODE_ID_COL, 1)
            .query();
    for (Service service : list)
      System.out.println(service.getNode());
    System.exit(0);
      //dao.delete(node);



    //System.out.println("1".matches("\\d$"));
    //Dao<Element, Integer> elementDao = api.getElementDao();
    //elementDao.queryForId(1);

    OS os = new OS("arch", "name", "version");
    Dao<OS, Integer> osDao = api.getDao(OS.class);
    os.setHostName("eaiwbmd01");
    osDao.createOrUpdate(os);

    // instantiate the dao
    Dao<IS, Integer> isDao = api.getDao(IS.class);

    //IS mktg = new IS("Marketing", 8010);
    //isDao.create(mktg);

    List<IS> isList =
        isDao.queryBuilder().where()
            .eq(IS.PORT_COL, 8010)
            .query();
    IS supply = new IS("Supply", 7010);
    isDao.create(supply);
    System.out.println("ises " + isList);
    //System.out.println(mktg.getId());
    System.out.println(supply.getId());

    api.getConnectionSource().close();
  }


  public static void runAllQueries(java.sql.Connection conn, String sqls) throws SQLException {
    for (String sql : sqls.split(";")) {
      sql = sql.replaceAll("(--.*|^ *)(\n|$)", "");
      if (!sql.matches(" *\n?")) {
        //System.out.println("sql " + sql);
        try {
          PreparedStatement query = conn.prepareStatement(sql);
          int result = query.executeUpdate();
          //System.out.println("result " + );
        } catch (SQLException e)  {
          if (e.getErrorCode() == 30000) {
            System.out.println(e.getMessage());
          } else {
            throw e;
          }
        }
      }
    }
  }

}
