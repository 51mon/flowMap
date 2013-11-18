package flowMap.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import flowMap.model.flow.Mapping;

@DatabaseTable(tableName = "flows")
public class Flow extends NamedIntId {
  public static final String PROCESS_NAME_COL = "process_name";
  @DatabaseField(columnName = PROCESS_NAME_COL, width = 32)
  String processName;
  public static final String SUB_PROCESS_NAME_COL = "sub_process_name";
  @DatabaseField(columnName = SUB_PROCESS_NAME_COL, width = 32)
  String subProcessName;
  @ForeignCollectionField(eager = true, orderColumnName = "position")
  ForeignCollection<FlowItem> items;
  public Flow(){}
}
