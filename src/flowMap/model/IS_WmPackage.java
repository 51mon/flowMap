package flowMap.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "is_packages")
public class IS_WmPackage extends IntId {
  public static final String IS_ID_COL = "is_id";
  public static final String PACKAGE_ID_COL = "package_id";

  @DatabaseField(columnName = IS_ID_COL, foreign = true)
  private IS is;

  @DatabaseField(columnName = PACKAGE_ID_COL, foreign = true)
  private WmPackage wmPackage;

  public IS_WmPackage() {}
  public IS_WmPackage(IS is, WmPackage wmPackage) {
    this.is = is;
    this.wmPackage = wmPackage;
  }

  public IS getIs() {
    return is;
  }

  public void setIs(IS is) {
    this.is = is;
  }

  public WmPackage getWmPackage() {
    return wmPackage;
  }

  public void setWmPackage(WmPackage wmPackage) {
    this.wmPackage = wmPackage;
  }
}
