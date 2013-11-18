package flowMap.model.tn;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import flowMap.model.IRecordUser;
import flowMap.model.IServiceUser;
import flowMap.model.NamedIntId;
import flowMap.model.Node;

@DatabaseTable(tableName = "processing_rules")
public class ProcessingRule extends NamedIntId implements IRecordUser, IServiceUser {
  // cf IRecordUser
  @DatabaseField(columnName = RECORD_FULL_NAME_COL)
  String recordNodeFullName;
  @DatabaseField(columnName = RECORD_NODE_ID_COL, foreign = true)
  Node recordNode;

  // The bizDocType == name

  // cf IServiceUser
  @DatabaseField(columnName = SERVICE_FULL_NAME_COL)
  String serviceNodeFullName;
  @DatabaseField(columnName = SERVICE_NODE_ID_COL, foreign = true)
  Node serviceNode;

  public ProcessingRule(){}
  public ProcessingRule(String doctype, String serviceNodeFullName, String recordNodeFullName) {
    this.name = doctype;
    this.serviceNodeFullName = serviceNodeFullName;
    this.recordNodeFullName = recordNodeFullName;
  }

  public String getRecordNodeFullName() {
    return recordNodeFullName;
  }

  public void setRecordNodeFullName(String recordNodeFullName) {
    this.recordNodeFullName = recordNodeFullName;
  }

  public String getServiceNodeFullName() {
    return serviceNodeFullName;
  }

  public void setServiceNodeFullName(String serviceNodeFullName) {
    this.serviceNodeFullName = serviceNodeFullName;
  }

  public Node getServiceNode() {
    return serviceNode;
  }

  public void setServiceNode(Node serviceNode) {
    this.serviceNode = serviceNode;
  }

  public Node getRecordNode() {
    return recordNode;
  }
  public void setRecordNode(Node document) {
    this.recordNode = document;
  }
}
