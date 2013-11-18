package flowMap.model.flow;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import flowMap.model.NamedIntId;

// This is not a WM object
@DatabaseTable(tableName = "substitutions")
public class Substitution extends NamedIntId {
  public static final String ELEMENT_ID_COL = "map_item_id";
  @DatabaseField(columnName = ELEMENT_ID_COL, foreign = true)
  private MapItem element;

  public static final String SETTER_ELEMENT_ID_COL = "setter_element_id";
  @DatabaseField(columnName = SETTER_ELEMENT_ID_COL, foreign = true)
  private Element setterElement;
}
