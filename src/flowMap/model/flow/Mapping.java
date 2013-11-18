package flowMap.model.flow;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import flowMap.model.NamedIntId;

@DatabaseTable(tableName = "flow_mappings")
public class Mapping extends NamedIntId {
  public static final String MAP_COPY_FROM_COL = "map_copy_from_id";
  @DatabaseField(columnName = MAP_COPY_FROM_COL, foreign = true)
  MapItem mapCopyFrom;
  public static final String MAP_COPY_TO_COL = "map_copy_to_id";
  @DatabaseField(columnName = MAP_COPY_TO_COL, foreign = true)
  MapItem mapCopyTo;
  public static final String MAP_UPDATE_COL = "map_update_id";
  @DatabaseField(columnName = MAP_UPDATE_COL, foreign = true)
  MapItem mapUpdate;

  public static final String PARENT_COL = "parent_id";
  @DatabaseField(columnName = PARENT_COL, foreign = true)
  Mapping parent;
  public static final String INDEX1_COL = "index1";
  @DatabaseField(columnName = INDEX1_COL)
  String index1;
  public static final String INDEX2_COL = "index2";
  @DatabaseField(columnName = INDEX2_COL)
  String index2;
  public static final String NUM1_COL = "num1";
  @DatabaseField(columnName = NUM1_COL)
  float num1;
  public static final String NUM2_COL = "num2";
  @DatabaseField(columnName = NUM2_COL)
  float num2;

  public Mapping(){}
  public Mapping(String name, String index1, String index2, String num1, String num2, Mapping mapping){
    this.name = name;
//    if (index1 != null) {
//      this.index1 = Integer.parseInt(index1);
//    }
//    if (index2 != null) {
//      this.index2 = Integer.parseInt(index2);
//    }
    this.index1 = index1;
    this.index2 = index2;
    this.num1 = Float.parseFloat(num1);
    this.num2 = Float.parseFloat(num2);
    this.parent = mapping;
  }

  public MapItem getMapCopyFrom() {
    return mapCopyFrom;
  }

  public void setMapCopyFrom(MapItem mapCopyFrom) {
    this.mapCopyFrom = mapCopyFrom;
  }

  public MapItem getMapCopyTo() {
    return mapCopyTo;
  }

  public void setMapCopyTo(MapItem mapCopyTo) {
    this.mapCopyTo = mapCopyTo;
  }

  public MapItem getMapUpdate() {
    return mapUpdate;
  }

  public void setMapUpdate(MapItem mapUpdate) {
    this.mapUpdate = mapUpdate;
  }

  public Mapping getParent() {
    return parent;
  }

  public void setParent(Mapping parent) {
    this.parent = parent;
  }

  public String getIndex1() {
    return index1;
  }

  public void setIndex1(String index1) {
    this.index1 = index1;
  }

  public String getIndex2() {
    return index2;
  }

  public void setIndex2(String index2) {
    this.index2 = index2;
  }

  public float getNum1() {
    return num1;
  }

  public void setNum1(float num1) {
    this.num1 = num1;
  }

  public float getNum2() {
    return num2;
  }

  public void setNum2(int num2) {
    this.num2 = num2;
  }
}
