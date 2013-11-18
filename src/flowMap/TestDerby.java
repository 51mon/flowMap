package flowMap;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import flowMap.model.*;
import flowMap.model.WmPackage;
import flowMap.model.flow.Element;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class TestDerby {
  public static void main(String[] args) throws SQLException, Exception {
    System.out.println("Start");
    // Derby
    Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
    String dbName = "FLOW_MAP";
    ConnectionSource connectionSource =
        new JdbcConnectionSource("jdbc:derby:" + dbName + ";create=true");

    dropTables(connectionSource);
    createTables(connectionSource);

    // instantiate the dao
    Dao<IS, Integer> isDao =
        DaoManager.createDao(connectionSource, IS.class);

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
    connectionSource.close();

    // stop the embedded database
    try { DriverManager.getConnection("jdbc:derby:" + dbName + ";shutdown=true"); } catch (SQLException e) {
      if (e.getErrorCode() != 45000)
        throw e;
    }
  }

  private static void createTables(ConnectionSource connectionSource) throws SQLException {
    TableUtils.createTableIfNotExists(connectionSource, IS.class);
    TableUtils.createTableIfNotExists(connectionSource, WmPackage.class);
    TableUtils.createTableIfNotExists(connectionSource, IS_WmPackage.class);
    TableUtils.createTableIfNotExists(connectionSource, Record.class);
    TableUtils.createTableIfNotExists(connectionSource, Service.class);
    TableUtils.createTableIfNotExists(connectionSource, Document.class);
    TableUtils.createTableIfNotExists(connectionSource, Element.class);
    //TableUtils.createTableIfNotExists(connectionSource, MapItem.class);
//    TableUtils.createTableIfNotExists(connectionSource, MapSet.class);
//    TableUtils.createTableIfNotExists(connectionSource, MapCopy.class);
//    TableUtils.createTableIfNotExists(connectionSource, MapDelete.class);
    //TableUtils.createTableIfNotExists(connectionSource, Invoke.class);
    TableUtils.createTableIfNotExists(connectionSource, Trigger.class);
    TableUtils.createTableIfNotExists(connectionSource, TriggerCondition.class);
    TableUtils.createTableIfNotExists(connectionSource, DocFilterPair.class);
    TableUtils.createTableIfNotExists(connectionSource, Adapter.class);
    //TableUtils.createTableIfNotExists(connectionSource, AdapterService.class);
  }
  private static void dropTables(ConnectionSource connectionSource) throws SQLException {
    //TableUtils.dropTable(connectionSource, AdapterService.class, true);
    TableUtils.dropTable(connectionSource, Adapter.class, true);
    TableUtils.dropTable(connectionSource, DocFilterPair.class, true);
    TableUtils.dropTable(connectionSource, TriggerCondition.class, true);
    TableUtils.dropTable(connectionSource, Trigger.class, true);
    //TableUtils.dropTable(connectionSource, Invoke.class, true);
//    TableUtils.dropTable(connectionSource, MapDelete.class, true);
//    TableUtils.dropTable(connectionSource, MapSet.class, true);
//    TableUtils.dropTable(connectionSource, MapCopy.class, true);
    //TableUtils.dropTable(connectionSource, MapItem.class, true);
    TableUtils.dropTable(connectionSource, Element.class, true);
    TableUtils.dropTable(connectionSource, Document.class, true);
    TableUtils.dropTable(connectionSource, Service.class, true);
    TableUtils.dropTable(connectionSource, Record.class, true);
    TableUtils.dropTable(connectionSource, IS_WmPackage.class, true);
    TableUtils.dropTable(connectionSource, WmPackage.class, true);
    TableUtils.dropTable(connectionSource, IS.class, true);
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
