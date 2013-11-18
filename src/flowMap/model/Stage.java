package flowMap.model;

import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "stages")
public class Stage extends NamedIntId {
  public Stage() {}
  public Stage(String name) {
    super(name);
  }
}
