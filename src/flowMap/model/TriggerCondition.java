package flowMap.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "trigger_conditions")
public class TriggerCondition extends NamedIntId implements IServiceUser {
  public static final String TRIGGER_ID_COL = "trigger_id";
  @DatabaseField(columnName = TRIGGER_ID_COL, foreign = true)
  Trigger trigger;

  // cf IServiceUser
  @DatabaseField(columnName = SERVICE_FULL_NAME_COL)
  String serviceNodeFullName;
  @DatabaseField(columnName = SERVICE_NODE_ID_COL, foreign = true)
  Node serviceNode;


  @ForeignCollectionField(eager = true)
  ForeignCollection<DocFilterPair> docFilterPairs;

  public TriggerCondition() {}
  public TriggerCondition(Trigger trigger, String name) {
    this.trigger = trigger;
    this.name = name;
  }

  public ForeignCollection<DocFilterPair> getDocFilterPairs() {
    return docFilterPairs;
  }

  public void setDocFilterPairs(ForeignCollection<DocFilterPair> docFilterPairs) {
    this.docFilterPairs = docFilterPairs;
  }

  public Node getServiceNode() {
    return serviceNode;
  }

  public void setServiceNode(Node serviceNode) {
    this.serviceNode = serviceNode;
  }

  public String getServiceNodeFullName() {
    return serviceNodeFullName;
  }

  public void setServiceNodeFullName(String serviceNodeFullName) {
    this.serviceNodeFullName = serviceNodeFullName;
  }

  public Trigger getTrigger() {
    return trigger;
  }

  public void setTrigger(Trigger trigger) {
    this.trigger = trigger;
  }
}
