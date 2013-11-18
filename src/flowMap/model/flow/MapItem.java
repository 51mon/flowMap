package flowMap.model.flow;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import flowMap.model.IntId;
import flowMap.server.Util;

@DatabaseTable(tableName = "flow_map_items")
public class MapItem extends IntId {
  public static final String MAP_ID_COL = "map_id";

  public enum Type {COPY, SET, DELETE}
  public static final String TYPE_COL = "type";
  @DatabaseField(columnName = TYPE_COL, width = 32)
  Type type;

  @DatabaseField(columnName = MAP_ID_COL, foreign = true)
  Element map;

  // Setter & delete
  @ForeignCollectionField(foreignFieldName = "mapUpdate", eager = true)
  ForeignCollection<Mapping> updateMappings;

  @ForeignCollectionField(eager = true)
  ForeignCollection<Input> inputs;

  public static final String VARIABLE_SUBSTITUTION_COL = "variable_substitution";
  @DatabaseField(columnName = VARIABLE_SUBSTITUTION_COL, format = "integer")
  boolean variableSubstitution;

  // Copy
  @ForeignCollectionField(foreignFieldName = "mapCopyFrom", eager = true)
  ForeignCollection<Mapping> copyFromMappings;
  @ForeignCollectionField(foreignFieldName = "mapCopyTo", eager = true)
  ForeignCollection<Mapping> copyToMappings;

  public MapItem(){}
  public MapItem(Element map, Type type){
    this.map = map;
    this.type = type;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public ForeignCollection<Input> getInputs() {
    return inputs;
  }

  public void setInputs(ForeignCollection<Input> inputs) {
    this.inputs = inputs;
  }

  public Element getMap() {
    return map;
  }
  public void setMap(Element map) {
    this.map = map;
  }

  public ForeignCollection<Mapping> getUpdateMappings() {
    return updateMappings;
  }

  public void setUpdateMappings(ForeignCollection<Mapping> updateMappings) {
    this.updateMappings = updateMappings;
  }

  public boolean isVariableSubstitution() {
    return variableSubstitution;
  }

  public void setVariableSubstitution(boolean variableSubstitution) {
    this.variableSubstitution = variableSubstitution;
  }

  public ForeignCollection<Mapping> getCopyFromMappings() {
    return copyFromMappings;
  }

  public void setCopyFromMappings(ForeignCollection<Mapping> copyFromMappings) {
    this.copyFromMappings = copyFromMappings;
  }

  public ForeignCollection<Mapping> getCopyToMappings() {
    return copyToMappings;
  }

  public void setCopyToMappings(ForeignCollection<Mapping> copyToMappings) {
    this.copyToMappings = copyToMappings;
  }
}
