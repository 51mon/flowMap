package flowMap.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "test")
public class TestInsert {
  @DatabaseField(columnName = "some_boolean", format = "integer")
  private boolean someBoolean;

  public void setSomeBoolean(boolean someBoolean) {
    this.someBoolean = someBoolean;
  }
}
