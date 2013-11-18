package flowMap;


import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.List;

public class TestPureJDBC {
  public static void main(String[] args) throws SQLException, Exception {
    System.out.println("Start");
    Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
    String dbName = "FLOW_MAP";

    // Classic way
    Connection conn = DriverManager.getConnection("jdbc:derby:" + dbName + ";create=true", null);
    runAllQueries(conn, dropTables);

    try { DriverManager.getConnection("jdbc:derby:"+dbName+";shutdown=true"); } catch (SQLException e) {
      if (e.getErrorCode() != 45000)
        throw e;
    }
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

  static String createTables = "create table ises (\n" +
      "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) CONSTRAINT IS_PK PRIMARY KEY,\n" +
      "name varchar(32),\n" +
      "port INTEGER\n" +
      ");\n" +
      "\n" +
      "create table packages (\n" +
      "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) CONSTRAINT PACKAGE_PK PRIMARY KEY,\n" +
      "name varchar(32)\n" +
      ");\n" +
      "\n" +
      "create table is_packages (\n" +
      "is_id INTEGER CONSTRAINT IS_FK REFERENCES ises (id),\n" +
      "package_id INTEGER CONSTRAINT PACKAGE_FK REFERENCES packages (id)\n" +
      ");\n" +
      "\n" +
      "create table services (\n" +
      "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) CONSTRAINT SERVICE_PK PRIMARY KEY,\n" +
      "package_id INTEGER NOT NULL,\n" +
      "name varchar(32),\n" +
      "path varchar(64),\n" +
      "CONSTRAINT SERVICES_PACKAGE_FK FOREIGN KEY (package_id) REFERENCES packages (id)\n" +
      ");\n" +
      "\n" +
      "create table elements (\n" +
      "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) CONSTRAINT ELEMENT_PK PRIMARY KEY,\n" +
      "service_id INTEGER CONSTRAINT ELEMENT_SERVICE_FK REFERENCES services (id),\n" +
      "is_enabled SMALLINT,\n" +
      "type varchar(32) CONSTRAINT ELEMENT_TYPE CHECK (type IN ('INVOKE', 'JAVA', 'MAP', 'LOOP', 'BRANCH', 'SEQUENCE', 'REPEAT', 'DOCUMENT', 'TRIGGER', 'ADAPTER')), \n" +
      "scope varchar(32),\n" +
      "timeout INTEGER,\n" +
      "label varchar(64)\n" +
      ");\n" +
      "\n" +
      "-- MAPS -------------\n" +
      "create table maps (\n" +
      "element_id INTEGER NOT NULL,\n" +
      "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) CONSTRAINT MAP_PK PRIMARY KEY,\n" +
      "input_map_id int,\n" +
      "output_map_id int,\n" +
      "CONSTRAINT MAP_ELEMENT_FK FOREIGN KEY (element_id) REFERENCES maps (id)\n" +
      ");\n" +
      "\n" +
      "create table map_sets (\n" +
      "map_id INTEGER NOT NULL,\n" +
      "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) CONSTRAINT MAP_SET_PK PRIMARY KEY,\n" +
      "name varchar(64),\n" +
      "value varchar(64),\n" +
      "CONSTRAINT MAP_SET_FK FOREIGN KEY (map_id) REFERENCES maps (id)\n" +
      ");\n" +
      "\n" +
      "create table map_copies (\n" +
      "map_id INTEGER NOT NULL,\n" +
      "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) CONSTRAINT MAP_COPY_PK PRIMARY KEY,\n" +
      "source varchar(64),\n" +
      "target varchar(64),\n" +
      "CONSTRAINT MAP_COPY_FK FOREIGN KEY (map_id) REFERENCES maps (id)\n" +
      ");\n" +
      "\n" +
      "create table map_deletes (\n" +
      "map_id INTEGER NOT NULL,\n" +
      "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) CONSTRAINT MAP_DEL_PK PRIMARY KEY,\n" +
      "name varchar(64),\n" +
      "CONSTRAINT MAP_DEL_FK FOREIGN KEY (map_id) REFERENCES maps (id)\n" +
      ");\n" +
      "\n" +
      "-- trigger -----------\n" +
      "create table trigger_conditions (\n" +
      "element_id INTEGER,\n" +
      "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) CONSTRAINT TRIG_COND_PK PRIMARY KEY,\n" +
      "name varchar(32),\n" +
      "service_id INTEGER,\n" +
      "CONSTRAINT TRIG_ELEMENT_FK FOREIGN KEY (element_id) REFERENCES elements (id)\n" +
      ");\n" +
      "\n" +
      "create table doc_filter_pairs (\n" +
      "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) CONSTRAINT DOC_FILT_PK PRIMARY KEY,\n" +
      "condition_id INTEGER,\n" +
      "doc_id INTEGER,\n" +
      "filter varchar(64),\n" +
      "CONSTRAINT TRIG_PAIR_CONDITION_FK FOREIGN KEY (condition_id) REFERENCES trigger_conditions (id)\n" +
      ");\n" +
      "\n" +
      "-- adapter ----------------\n" +
      "create table connections (\n" +
      "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) CONSTRAINT CONN_PK PRIMARY\n" +
      " KEY,\n" +
      "name varchar(64)\n" +
      ");\n" +
      "\n" +
      "create table adapters (\n" +
      "element_id INTEGER,\n" +
      "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) CONSTRAINT ADAPTER_PK PRIMARY KEY,\n" +
      "service_id INTEGER,\n" +
      "sql_query varchar(128),\n" +
      "connection_id INTEGER,\n" +
      "CONSTRAINT ADAPTER_CONNECTION_FK FOREIGN KEY (connection_id) REFERENCES connections (id)\n" +
      ");\n" +
      "\n" +
      "-- process -----------------------\n" +
      "create table processes (\n" +
      "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) CONSTRAINT PROC_PK PRIMARY KEY,\n" +
      "name varchar(32)\n" +
      ");\n" +
      "create table sub_processes (\n" +
      "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) CONSTRAINT SUB_PROC_PK PRIMARY KEY,\n" +
      "process_id INTEGER,\n" +
      "name varchar(32),\n" +
      "CONSTRAINT SUBPROC_PROC_FK FOREIGN KEY (process_id) REFERENCES processes (id)\n" +
      ");\n" +
      "--create table sub_process_to_services (\n" +
      "--sub_process_id INTEGER,\n" +
      "--service_id INTEGER\n" +
      "--);\n" +
      "create table sub_process_ends (\n" +
      "isInput SMALLINT,\n" +
      "sub_process_id INTEGER,\n" +
      "end_type varchar(16) CONSTRAINT END_TYPE CHECK (end_type IN ('CONNECTION', 'DOCUMENT', 'SERVICE')), -- connection ART, doc, service (sendToXFB, trigger)\n" +
      "end_id INTEGER,\n" +
      "CONSTRAINT SUBPROC_END_SUBPROC_FK FOREIGN KEY (sub_process_id) REFERENCES sub_processes (id)\n" +
      ");\n" +
      "create table applications (\n" +
      "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) CONSTRAINT APP_PK PRIMARY KEY,\n" +
      "name varchar(16)\n" +
      ");\n";

  static String dropTables = "drop table applications;\n" +
      "drop table sub_process_ends;\n" +
      "drop table sub_processes;\n" +
      "drop table processes;\n" +
      "drop table adapters;\n" +
      "drop table connections;\n" +
      "drop table doc_filter_pairs;\n" +
      "drop table trigger_conditions;\n" +
      "drop table map_deletes;\n" +
      "drop table map_copies;\n" +
      "drop table map_sets;\n" +
      "drop table maps;\n" +
      "drop table elements;\n" +
      "drop table services;\n" +
      "drop table is_packages;\n" +
      "drop table packages;\n" +
      "drop table ises;\n";

}
