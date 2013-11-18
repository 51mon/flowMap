package flowMap.model;

import com.j256.ormlite.field.DatabaseField;

public class NodeChild extends IntId {
  public static final String NODE_ID_COL = "nsnode_id";
  @DatabaseField(columnName = NODE_ID_COL, foreign = true)
  Node node;

  public Node getNode() {
    return node;
  }

  public void setNode(Node node) {
    this.node = node;
  }
}
