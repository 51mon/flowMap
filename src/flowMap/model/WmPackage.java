package flowMap.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "packages")
public class WmPackage extends NamedIntId {
  @ForeignCollectionField(eager = true)
  ForeignCollection<IS_WmPackage> iS_WmPackages;

  // contains services, triggers & adapter_services
  public WmPackage() {}
  public WmPackage(String name) {
    this.name = name;
  }

  public ForeignCollection<IS_WmPackage> getIS_WmPackages() {
    return iS_WmPackages;
  }

  public void setIS_WmPackages(ForeignCollection<IS_WmPackage> iS_WmPackages) {
    this.iS_WmPackages = iS_WmPackages;
  }
}
