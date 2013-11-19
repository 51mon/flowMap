package flowMap.model;

import com.j256.ormlite.field.DatabaseField;

public class IntId {
  public static final String ID_COL = "id";
  @DatabaseField(generatedId = true, allowGeneratedIdInsert = true, columnName = ID_COL) // to allow delete and reset an object (ex Node)
  int id;

  public int getId() {
    return id;
  }
  public void setId(int id) {
    this.id = id;
  }
}
