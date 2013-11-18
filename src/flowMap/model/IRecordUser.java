package flowMap.model;

public interface IRecordUser {
  public static final String RECORD_FULL_NAME_COL = "record_node_full_name";
  public String getRecordNodeFullName();
  public void setRecordNodeFullName(String recordFullName);
  public static final String RECORD_NODE_ID_COL = "record_node_id";
  public Node getRecordNode();
  public void setRecordNode(Node document);
}
