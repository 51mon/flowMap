package flowMap.model.flow;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import flowMap.model.IServiceUser;
import flowMap.model.IntId;
import flowMap.model.Node;
import flowMap.model.Service;

@DatabaseTable(tableName = "flow_elements")
public class Element extends IntId implements IServiceUser {
  public static final String POSITION_COL = "position";
  public static final String IS_ENABLED_COL = "is_enabled";
  public static final String TYPE_COL = "type"; // MapItem, Invoke, Control
  public static final String SCOPE_COL = "scope";
  public static final String COMMENT_COL = "comment";
  public static final String TIMEOUT_COL = "timeout";
  public static final String LABEL_COL = "label";
  public static final String PARENT_ID_COL = "parent_id";
  @DatabaseField(columnName = PARENT_ID_COL, foreign = true)
  private Element parent;
  @ForeignCollectionField(eager = true, orderColumnName = POSITION_COL)
  ForeignCollection<Element> children;

  @DatabaseField(columnName = IS_ENABLED_COL, format = "integer")
  private boolean isEnabled;

  public enum Types { ROOT, INVOKE, MAP, LOOP, BRANCH, SEQUENCE, REPEAT, EXIT }
  @DatabaseField(columnName = TYPE_COL, width = 32)
  private Types type;

  @DatabaseField(columnName = TIMEOUT_COL)
  private int timeout;

  @DatabaseField(columnName = LABEL_COL, width = 1024)
  private String label;

  @DatabaseField(columnName = SCOPE_COL, width = 128)
  private String scope;

  @DatabaseField(columnName = COMMENT_COL, width = 256)
  private String comment;

  @DatabaseField(columnName = POSITION_COL)
  private int position;

  // Root
  public static final String SERVICE_ID_COL = "service_id";
  @DatabaseField(columnName = SERVICE_ID_COL, foreign = true)
  private Service service;

  // Invoke
//  public static final String INPUT_MAP_ID_COL = "input_map_id";
//  public static final String OUTPUT_MAP_ID_COL = "output_map_id";
//  @DatabaseField(columnName = INPUT_MAP_ID_COL, foreign = true)
//  Element inputMap;
//  @DatabaseField(columnName = OUTPUT_MAP_ID_COL, foreign = true)
//  Element outputMap;

  // cf IServiceUser
  @DatabaseField(columnName = SERVICE_FULL_NAME_COL, width = 256)
  String serviceNodeFullName;
  @DatabaseField(columnName = SERVICE_NODE_ID_COL, foreign = true)
  Node serviceNode;
  //

  // MapItem
  @ForeignCollectionField(eager = true)
  ForeignCollection<MapItem> mapItems;
//  @ForeignCollectionField(eager = true)
//  ForeignCollection<MapCopy> copies;
//  @ForeignCollectionField(eager = true)
//  ForeignCollection<MapDelete> deletes;

  // Loop
  public static final String INPUT_ARRAY_COL = "input_array";
  @DatabaseField(columnName = INPUT_ARRAY_COL, width = 128)
  private String inputArray;
  public static final String OUTPUT_ARRAY_COL = "output_array";
  @DatabaseField(columnName = OUTPUT_ARRAY_COL, width = 128)
  private String outputArray;
  // Branch
  public static final String SWITCH_COL = "switch";
  @DatabaseField(columnName = SWITCH_COL, width = 256)
  private String _switch;
  public static final String EVALUATE_LABEL_COL = "evaluate_labels";
  @DatabaseField(columnName = EVALUATE_LABEL_COL, format = "integer")
  private boolean evaluateLabels;
  // Repeat
  public static final String COUNT_COL = "count";
  @DatabaseField(columnName = COUNT_COL)
  private int count;
  public static final String REPEAT_INTERVAL_COL = "repeat_interval";
  @DatabaseField(columnName = REPEAT_INTERVAL_COL)
  private int repeatInterval;
  public static final String REPEAT_ON_COL = "repeat_on";
  @DatabaseField(columnName = REPEAT_ON_COL, width = 256)
  private String repeatOn;
  // Sequence
  public static final String EXIT_ON_COL = "exit_on";
  @DatabaseField(columnName = EXIT_ON_COL, width = 256)
  private String exitOn;
  // Exit
  public static final String FROM_COL = "from";
  @DatabaseField(columnName = FROM_COL, width = 256)
  private String from;
  public static final String SIGNAL_COL = "signal";
  @DatabaseField(columnName = SIGNAL_COL, width = 256)
  private String signal;
  public static final String FAILURE_MESSAGE_COL = "failure_message";
  @DatabaseField(columnName = FAILURE_MESSAGE_COL, width = 256)
  private String failureMessage;

  public Element() {}
  public Element(Types type) {
    this.type = type;
  }
  public Element(Element parent, int position) {
    this.parent = parent;
    this.position = position;
  }
  public Element(Element parent, int position, Types type) {
    this(parent, position);
    this.type = type;
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

  public Service getService() {
    return service;
  }

  public void setService(Service service) {
    this.service = service;
  }

  public boolean isEnabled() {
    return isEnabled;
  }

  public void setEnabled(boolean enabled) {
    isEnabled = enabled;
  }

  public Types getType() {
    return type;
  }

  public void setType(Types type) {
    this.type = type;
  }

  public int getTimeout() {
    return timeout;
  }

  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public Element getParent() {
    return parent;
  }

  public void setParent(Element parent) {
    this.parent = parent;
  }

  public String getInputArray() {
    return inputArray;
  }

  public void setInputArray(String inputArray) {
    this.inputArray = inputArray;
  }

  public String getOutputArray() {
    return outputArray;
  }

  public void setOutputArray(String outputArray) {
    this.outputArray = outputArray;
  }

  public String getSwitch() {
    return _switch;
  }

  public void setSwitch(String flowSwitch) {
    this._switch = flowSwitch;
  }

  public boolean evaluateLabels() {
    return evaluateLabels;
  }

  public void setEvaluateLabels(boolean evaluateLabels) {
    this.evaluateLabels = evaluateLabels;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public int getRepeatInterval() {
    return repeatInterval;
  }

  public void setRepeatInterval(int repeatInterval) {
    this.repeatInterval = repeatInterval;
  }

  public String getRepeatOn() {
    return repeatOn;
  }

  public void setRepeatOn(String repeatOn) {
    this.repeatOn = repeatOn;
  }

  public String getExitOn() {
    return exitOn;
  }

  public void setExitOn(String exitOn) {
    this.exitOn = exitOn;
  }

  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public String getSignal() {
    return signal;
  }

  public void setSignal(String signal) {
    this.signal = signal;
  }

  public String getFailureMessage() {
    return failureMessage;
  }

  public void setFailureMessage(String failureMessage) {
    this.failureMessage = failureMessage;
  }

  public ForeignCollection<MapItem> getMapItems() {
    return mapItems;
  }

  public void setMapItems(ForeignCollection<MapItem> mapItems) {
    this.mapItems = mapItems;
  }

  public ForeignCollection<Element> getChildren() {
    return children;
  }
}
