package flowMap.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "records")
public class Record extends NamedIntId implements IRecordUser { // NSRecordRef, NSRecord & NSField
//  public static final String RECORD_FULL_NAME_COL = "record_full_name";
//  @DatabaseField(columnName = RECORD_FULL_NAME_COL, width = 256)
//  String recordFullName;
//
//  public static final String RECREF_NODE_ID_COL = "recref_nsnode_id";
//  @DatabaseField(columnName = RECREF_NODE_ID_COL, foreign = true)
//  Node recRef;

  @DatabaseField(columnName = RECORD_FULL_NAME_COL) // RECREF NAME
  String recordNodeFullName;
  @DatabaseField(columnName = RECORD_NODE_ID_COL, foreign = true) // RECREF
  Node recordNode;

  public static final String NODE_ID_COL = "nsnode_id";
  @DatabaseField(columnName = NODE_ID_COL, foreign = true)
  Node node;
  public static final String SERVICE_INPUT_ID_COL = "service_input_id";
  @DatabaseField(columnName = SERVICE_INPUT_ID_COL, foreign = true)
  Service serviceInput;
  public static final String SERVICE_OUTPUT_ID_COL = "service_output_id";
  @DatabaseField(columnName = SERVICE_OUTPUT_ID_COL, foreign = true)
  Service serviceOutput;

  @ForeignCollectionField(eager = true)
  ForeignCollection<Record> records;

  public static final String TYPE_COL = "type";
  public enum Type { STRING, RECORD, OBJECT, RECREF }
  public static Record.Type getTypeFromInt(int i) {
    switch (i) {
      case 1: return Record.Type.STRING;
      case 2: return Record.Type.RECORD;
      case 3: return Record.Type.OBJECT;
      case 4: return Record.Type.RECREF;
    }
    return null;
  }

  @DatabaseField(columnName = TYPE_COL, width = 32)
  Type type;

  public static final String PARENT_ID_COL = "parent_id";
  @DatabaseField(columnName = PARENT_ID_COL, foreign = true)
  Record parent;

  public Record() {}
  public Record(String name) {
    this.name = name;
  }

  public Node getNode() {
    return node;
  }

  public void setNode(Node node) {
    this.node = node;
  }

  public Service getServiceOutput() {
    return serviceOutput;
  }

  public void setServiceOutput(Service serviceOutput) {
    this.serviceOutput = serviceOutput;
  }

  public Service getServiceInput() {
    return serviceInput;
  }

  public void setServiceInput(Service serviceInput) {
    this.serviceInput = serviceInput;
  }

//  public Node getRecRef() {
//    return recRef;
//  }
//
//  public void setRecRef(Node recRef) {
//    this.recRef = recRef;
//  }
//
//  public String getRecordFullName() {
//    return recordFullName;
//  }
//
//  public void setRecordFullName(String recordFullName) {
//    this.recordFullName = recordFullName;
//  }

  public String getRecordNodeFullName() {
    return recordNodeFullName;
  }

  public void setRecordNodeFullName(String recordNodeFullName) {
    this.recordNodeFullName = recordNodeFullName;
  }

  public Node getRecordNode() {
    return recordNode;
  }

  public void setRecordNode(Node recordNode) {
    this.recordNode = recordNode;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public Record getParent() {
    return parent;
  }

  public void setParent(Record parent) {
    this.parent = parent;
  }

  public ForeignCollection<Record> getRecords() {
    return records;
  }

  public void setRecords(ForeignCollection<Record> records) {
    this.records = records;
  }
}
