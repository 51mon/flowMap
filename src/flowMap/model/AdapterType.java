package flowMap.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "adapter_types")
public class AdapterType extends NamedIntId {
  public static final String DISPLAY_NAME_COL = "display_name";

  @DatabaseField(columnName = DISPLAY_NAME_COL, width = 32)
  private String displayName;

  public AdapterType() {}
  public AdapterType(String name) { // name = typeName
    this.name = name;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }
}
