package flowMap.model;

public interface IServiceUser {
  public static final String SERVICE_FULL_NAME_COL = "service_node_full_name";
  public String getServiceNodeFullName();
  public void setServiceNodeFullName(String recordFullName);
  public static final String SERVICE_NODE_ID_COL = "service_node_id";
  public Node getServiceNode();
  public void setServiceNode(Node document);
}
