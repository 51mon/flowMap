package flowMap.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "ises")
public class IS extends NamedIntId {

  // for QueryBuilder to be able to find the fields
  public static final String PORT_COL = "port";
  @DatabaseField(columnName = PORT_COL)
  private int port;

  public static final String OS_ID_COL = "os_id";
  @DatabaseField(columnName = OS_ID_COL, foreign = true)
  OS oS;

  public static final String VERSION_COL = "version";
  @DatabaseField(columnName = VERSION_COL)
  private String version;

  public static final String BUILD_COL = "build";
  @DatabaseField(columnName = BUILD_COL)
  private String build;

  public static final String JAVA_VERSION_COL = "java_version";
  @DatabaseField(columnName = JAVA_VERSION_COL)
  private String javaVersion;

  public static final String DIRECTORY_COL = "directory";
  @DatabaseField(columnName = DIRECTORY_COL)
  private String directory;

  public IS() {}
  public IS(String name) {
    super(name);
  }
  public IS(String name, int port) {
    this.name = name;
    this.port = port;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public OS getOS() {
    return oS;
  }

  public void setOS(OS oS) {
    this.oS = oS;
  }

  @Override
  public boolean equals(Object other) {
    if (other == null || other.getClass() != getClass()) {
      return false;
    }
    return port == ((IS) other).port;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getBuild() {
    return build;
  }

  public void setBuild(String build) {
    this.build = build;
  }

  public String getJavaVersion() {
    return javaVersion;
  }

  public void setJavaVersion(String javaVersion) {
    this.javaVersion = javaVersion;
  }

  public String getDirectory() {
    return directory;
  }

  public void setDirectory(String directory) {
    this.directory = directory;
  }

}