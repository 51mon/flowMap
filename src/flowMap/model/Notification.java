package flowMap.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "notifications")
public class Notification extends AdapterItem implements IRecordUser {
  @DatabaseField(columnName = RECORD_FULL_NAME_COL)
  String recordNodeFullName;
  @DatabaseField(columnName = RECORD_NODE_ID_COL, foreign = true)
  Node recordNode;

  public Notification() {}
  public Notification(Node node) {
    this.node = node;
  }

  public String getRecordNodeFullName() {
    return recordNodeFullName;
  }

  public void setRecordNodeFullName(String documentFullName) {
    this.recordNodeFullName = documentFullName;
  }

  public Node getRecordNode() {
    return recordNode;
  }

  public void setRecordNode(Node document) {
    this.recordNode = document;
  }
}
