package flowMap.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "triggers")
public class Trigger extends NodeChild {
  public static final String DELIVERY_ENABLED_COL = "is_delivery_enabled";
  @DatabaseField(columnName = DELIVERY_ENABLED_COL, format = "integer")
  boolean isDeliveryEnabled;

  public static final String EXECUTE_ENABLED_COL = "is_execute_enabled";
  @DatabaseField(columnName = EXECUTE_ENABLED_COL, format = "integer")
  boolean isExecuteEnabled;

  @ForeignCollectionField(eager = true)
  ForeignCollection<TriggerCondition> conditions;

  public Trigger(){}
  public Trigger(Node node) {
    this.node = node;
  }

  public ForeignCollection<TriggerCondition> getConditions() {
    return conditions;
  }

  public void setConditions(ForeignCollection<TriggerCondition> conditions) {
    this.conditions = conditions;
  }

  public boolean isDeliveryEnabled() {
    return isDeliveryEnabled;
  }

  public void setDeliveryEnabled(boolean deliveryEnabled) {
    isDeliveryEnabled = deliveryEnabled;
  }

  public boolean isExecuteEnabled() {
    return isExecuteEnabled;
  }

  public void setExecuteEnabled(boolean executeEnabled) {
    isExecuteEnabled = executeEnabled;
  }
}
