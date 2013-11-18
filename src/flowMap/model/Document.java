package flowMap.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "documents")
public class Document extends NodeChild {
  public static final String IS_PUBLISHABLE_COL = "is_publishable";
  @DatabaseField(columnName = IS_PUBLISHABLE_COL, format = "integer")
  boolean isPublishable;

  public static final String RECORD_ID_COL = "record_id";
  @DatabaseField(columnName = RECORD_ID_COL, foreign = true)
  Record record;

  public Document() {}
  public Document(Node node, Record record) {
    this.node = node;
    this.record = record;
  }

  public boolean isPublishable() {
    return isPublishable;
  }

  public void setPublishable(boolean publishable) {
    isPublishable = publishable;
  }

  public Record getRecord() {
    return record;
  }

  public void setRecord(Record record) {
    this.record = record;
  }
}
