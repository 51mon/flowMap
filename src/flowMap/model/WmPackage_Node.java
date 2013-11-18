package flowMap.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

// Managed with key directly into Node
//@DatabaseTable(tableName = "package_nodes")
public class WmPackage_Node extends IntId {
  public static final String NODE_ID_COL = "nsnode_id";
  @DatabaseField(columnName = NODE_ID_COL, foreign = true)
  private Node node;
  public static final String NODE_FULL_NAME_COL = "node_full_name";
  @DatabaseField(columnName = NODE_FULL_NAME_COL, width = 256)
  String nodeFullName;

  public static final String PACKAGE_ID_COL = "package_id";
  @DatabaseField(columnName = PACKAGE_ID_COL, foreign = true)
  private WmPackage wmPackage;

  public WmPackage_Node() {}

  public Node getNode() {
    return node;
  }

  public void setNode(Node node) {
    this.node = node;
  }

  public String getNodeFullName() {
    return nodeFullName;
  }

  public void setNodeFullName(String nodeFullName) {
    this.nodeFullName = nodeFullName;
  }

  public WmPackage getWmPackage() {
    return wmPackage;
  }

  public void setWmPackage(WmPackage wmPackage) {
    this.wmPackage = wmPackage;
  }
}
