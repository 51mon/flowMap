package flowMap.model;

import com.j256.ormlite.field.DatabaseField;

public class AdapterItem extends NodeChild {

  public static final String ADAPTER_KIND_COL = "adapter_kind";
  @DatabaseField(columnName = ADAPTER_KIND_COL)
  Adapter.Kind adapterKind;
  public static final String ADAPTER_FULL_NAME_COL = "adapter_full_name";
  @DatabaseField(columnName = ADAPTER_FULL_NAME_COL)
  String adapterFullName;
  public static final String ADAPTER_ID_COL = "adapter_id";
  @DatabaseField(columnName = ADAPTER_ID_COL, foreign = true, foreignAutoRefresh = true)
  Adapter adapter;

  public AdapterItem() {}

  public Adapter.Kind getAdapterKind() {
    return adapterKind;
  }

  public void setAdapterKind(Adapter.Kind adapterKind) {
    this.adapterKind = adapterKind;
  }

  public String getAdapterFullName() {
    return adapterFullName;
  }

  public void setAdapterFullName(String adapterFullName) {
    this.adapterFullName = adapterFullName;
  }

  public Adapter getAdapter() {
    return adapter;
  }

  public void setAdapter(Adapter adapter) {
    this.adapter = adapter;
  }
}
