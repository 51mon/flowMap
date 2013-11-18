package flowMap.model.flow;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import flowMap.model.IntId;

@DatabaseTable(tableName = "flow_inputs")
public class Input extends IntId {
  public static final String MAP_ITEM_ID_COL = "map_item_id";
  @DatabaseField(columnName = MAP_ITEM_ID_COL, foreign = true)
  MapItem mapItem;

  enum Type { STRING, INT, BOOLEAN}
  // Simple value
  public static final String STRING_COL = "string";
  @DatabaseField(columnName = STRING_COL, width = 512)
  String string;
  public static final String INT_COL = "int";
  @DatabaseField(columnName = INT_COL)
  int integer;
  public static final String BOOLEAN_COL = "boolean";
  @DatabaseField(columnName = BOOLEAN_COL, format = "integer")
  boolean bool;

  // Doc & doc array
  public static final String PARENT_ID_COL = "parent_id";
  @DatabaseField(columnName = PARENT_ID_COL, foreign = true)
  Input parent;
  public static final String KEY_COL = "key";
  @DatabaseField(columnName = KEY_COL, width = 512)
  String key;

  // Doc array
  public static final String POSITION_COL = "position";
  @DatabaseField(columnName = POSITION_COL)
  int position;

  public Input(){}
  public Input(MapItem mapItem, Object value){ // Set a value
    this.mapItem = mapItem;
    if (value != null)
      setValue(value);
  }
  public Input(MapItem mapItem, int position, Object value){ // set a value of an array
    this.mapItem = mapItem;
    this.position = position;
    setValue(value);
  }
  public Input(MapItem mapItem, String key, Object value){ // set a value of a doc
    this.mapItem = mapItem;
    setKey(key);
    setValue(value);
  }
  public Input(MapItem mapItem, int position, String key, Object value){ // set a value of a doc of an array
    this.mapItem = mapItem;
    this.position = position;
    setKey(key);
    setValue(value);
  }

  public String getString() {
    return string;
  }

  public void setString(String string) {
    this.string = string;
  }

  public int getInteger() {
    return integer;
  }

  public void setInteger(int integer) {
    this.integer = integer;
  }

  public boolean isBool() {
    return bool;
  }

  public void setBool(boolean bool) {
    this.bool = bool;
  }

  public Input getParent() {
    return parent;
  }

  public void setParent(Input parent) {
    this.parent = parent;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = (key != null && key.length() > 512) ? key.substring(0, 512) : key;
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public MapItem getMapItem() {
    return mapItem;
  }

  public void setMapItem(MapItem mapItem) {
    this.mapItem = mapItem;
  }

  public void setValue(Object value) {
    if (value == null)
      ;
    else if (value instanceof String) {
      String str = (String) value;
      this.string = (str.length() > 512) ? str.substring(0, 512) : str;
    } else if (value instanceof Integer)
      this.integer = (Integer) value;
    else if (value instanceof Boolean)
      this.bool = (Boolean) value;
  }
}
