package flowMap.model;

import com.j256.ormlite.field.DatabaseField;

public class NamedIntId extends IntId {
  public static final String NAME_COL = "name";
  @DatabaseField(columnName = NAME_COL)
  public String name;

  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public NamedIntId() {}
  public NamedIntId(String name) {
    this.name = name;
  }
}
