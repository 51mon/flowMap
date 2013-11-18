package flowMap.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "oses")
public class OS extends NamedIntId {
  public static final String STAGE_ID_COL = "stage_id";
  @DatabaseField(columnName = STAGE_ID_COL, foreign = true)
  Stage stage;

  public static final String VERSION_COL = "version";
  @DatabaseField(columnName = VERSION_COL)
  private String version;

  public static final String ARCHITECTURE_COL = "architecture";
  @DatabaseField(columnName = ARCHITECTURE_COL)
  private String architecture;

  public static final String HOST_NAME_COL = "host_name";
  @DatabaseField(columnName = HOST_NAME_COL)
  private String hostName;

  public OS() {}
  public OS(String hostName) {
    this.hostName = hostName;
  }
  public OS(String arch, String name, String version) {
    super(name);
    this.architecture = arch;
    this.version = version;
  }

  public String getHostName() {
    return hostName;
  }

  public void setHostName(String hostName) {
    this.hostName = hostName;
  }

  public String getArchitecture() {
    return architecture;
  }

  public void setArchitecture(String architecture) {
    this.architecture = architecture;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public Stage getStage() {
    return stage;
  }

  public void setStage(Stage stage) {
    this.stage = stage;
  }
}
