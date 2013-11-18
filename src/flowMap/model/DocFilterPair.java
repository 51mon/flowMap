package flowMap.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "doc_filter_pairs")
public class DocFilterPair extends IntId implements IRecordUser {
  public static final String TRIGGER_CONDITION_ID_COL = "trigger_condition_id";
  public static final String FILTER_COL = "filter";

  @DatabaseField(columnName = TRIGGER_CONDITION_ID_COL, foreign = true)
  private TriggerCondition triggerCondition;

  @DatabaseField(columnName = RECORD_FULL_NAME_COL, width = 256)
  String documentFullName;
  @DatabaseField(columnName = RECORD_NODE_ID_COL, foreign = true)
  private Node recordNode;

  @DatabaseField(columnName = FILTER_COL, width = 4000)
  private String filter;

  public DocFilterPair() {}
  public DocFilterPair(TriggerCondition triggerCondition) {
    this.triggerCondition = triggerCondition;
  }

  public TriggerCondition getTriggerCondition() {
    return triggerCondition;
  }

  public void setTriggerCondition(TriggerCondition triggerCondition) {
    this.triggerCondition = triggerCondition;
  }

  public String getRecordNodeFullName() {
    return documentFullName;
  }

  public void setRecordNodeFullName(String documentFullName) {
    this.documentFullName = documentFullName;
  }

  public Node getRecordNode() {
    return recordNode;
  }

  public void setRecordNode(Node document) {
    this.recordNode = document;
  }

  public String getFilter() {
    return filter;
  }

  public void setFilter(String filter) {
    if (filter.length() > 4000)
      throw new RuntimeException("Value too important for a varchar field");
    this.filter = filter;
  }
}
