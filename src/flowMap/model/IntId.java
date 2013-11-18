package flowMap.model;

import com.j256.ormlite.field.DatabaseField;

public class IntId {
  @DatabaseField(generatedId = true, allowGeneratedIdInsert = true) // to allow delete and reset an object (ex Node)
  int id;

  public int getId() {
    return id;
  }
  public void setId(int id) {
    this.id = id;
  }
}
