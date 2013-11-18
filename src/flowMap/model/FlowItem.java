package flowMap.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import flowMap.model.flow.Element;

@DatabaseTable(tableName = "flow_items")
public class FlowItem extends IntId {
  public static final String FLOW_ID_COL = "flow_id";
  @DatabaseField(columnName = FLOW_ID_COL, foreign = true)
  Flow flow;
  public static final String NODE_ID_COL = "node_id";
  @DatabaseField(columnName = NODE_ID_COL, foreign = true)
  Node node;
  public static final String ADAPTER_ID_COL = "adapter_id";
  @DatabaseField(columnName = ADAPTER_ID_COL, foreign = true)
  Adapter adapter;
  public static final String ELEMENT_ID_COL = "element_id";
  @DatabaseField(columnName = ELEMENT_ID_COL, foreign = true)
  Element element;
  public static final String POSITION_COL = "position";
  @DatabaseField(columnName = POSITION_COL)
  int position;

  public FlowItem() {
    position = 0;
  }
  public FlowItem(int position) {
    this.position = position;
  }
  public FlowItem(Flow flow, int position) {
    this.flow = flow;
    this.position = position;
  }

  public Element getElement() {
    return element;
  }

  public void setElement(Element element) {
    this.element = element;
  }

  public Flow getFlow() {
    return flow;
  }

  public void setFlow(Flow flow) {
    this.flow = flow;
  }

  public Node getNode() {
    return node;
  }

  public void setNode(Node node) {
    this.node = node;
  }

  public Adapter getAdapter() {
    return adapter;
  }

  public void setAdapter(Adapter adapter) {
    this.adapter = adapter;
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }
}
