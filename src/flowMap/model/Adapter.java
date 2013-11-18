package flowMap.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "adapters")
public class Adapter extends NodeChild {
  public static final String ADAPTER_TYPE_NAME_COL = "adapter_type_name";
  @DatabaseField(columnName = ADAPTER_TYPE_NAME_COL, width = 256)
  String adapterTypeName;
  public static final String ADAPTER_TYPE_ID_COL = "adapter_type_id";
  @DatabaseField(columnName = ADAPTER_TYPE_ID_COL, foreign = true)
  private AdapterType adapterType;

  @DatabaseField(columnName = "is_enabled", format = "integer")
  private boolean isEnabled;

  public static final String ALIAS_COL = "alias";
  @DatabaseField(columnName = ALIAS_COL, width = 64)
  String alias;

  public static final String USERNAME_COL = "username";
  @DatabaseField(columnName = USERNAME_COL, width = 64)
  String username;

  public static final String HOSTNAME_COL = "hostname";
  @DatabaseField(columnName = HOSTNAME_COL, width = 64)
  String hostname;

  public static final String OS_ID_COL = "os_id";
  @DatabaseField(columnName = OS_ID_COL, foreign = true)
  OS oS;

  public enum Kind { CONNECTION, LISTENER }
  public static final String KIND_COL = "adapter_kind";
  @DatabaseField(columnName = KIND_COL, width = 32)
  Kind kind;

  public Adapter(){}
  public Adapter(Node node) {
    this.node = node;
  }

  public String getAdapterTypeName() {
    return adapterTypeName;
  }

  public void setAdapterTypeName(String adapterTypeName) {
    this.adapterTypeName = adapterTypeName;
  }

  public AdapterType getAdapterType() {
    return adapterType;
  }

  public void setAdapterType(AdapterType adapterType) {
    this.adapterType = adapterType;
  }
  public boolean isEnabled() {
    return isEnabled;
  }
  public void setEnabled(boolean enabled) {
    isEnabled = enabled;
  }

  public String getAlias() {
    return alias;
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public OS getOS() {
    return oS;
  }

  public void setOS(OS oS) {
    this.oS = oS;
  }

  public Kind getKind() {
    return kind;
  }

  public void setKind(Kind kind) {
    this.kind = kind;
  }
}
