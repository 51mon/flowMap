package flowMap.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.lang.*;

@DatabaseTable(tableName = "nsnodes")
public class Node extends NamedIntId {
  public static final String PATH_COL = "path";
  public static final String COMMENT_COL = "comment";
  public static final String TYPE_COL = "type";
  @DatabaseField(columnName = TYPE_COL, width = 32)
  Type type;

  public static final String PACKAGE_NAME_COL = "package_name";
  @DatabaseField(columnName = PACKAGE_NAME_COL, width = 32)
  String packageName;
  public static final String PACKAGE_ID_COL = "package_id";
  @DatabaseField(columnName = PACKAGE_ID_COL, foreign = true)
  WmPackage wmPackage;

  @DatabaseField(columnName = PATH_COL, width = 128)
  String path;

  @DatabaseField(columnName = COMMENT_COL, width = 4000)
  String comment;

  public enum Type {SERVICE, TRIGGER, NOTIFICATION, ADAPTER, RECORD}
  @ForeignCollectionField(eager = true)
  ForeignCollection<Service> services;
  @ForeignCollectionField(eager = true)
  ForeignCollection<Trigger> triggers;
  @ForeignCollectionField(eager = true)
  ForeignCollection<Notification> notifications;
  @ForeignCollectionField(eager = true)
  ForeignCollection<Adapter> connections;
  @ForeignCollectionField(eager = true, foreignFieldName = "node")
  ForeignCollection<Record> documents;

  public Service getService() {
    return (services == null || services.isEmpty()) ? null : services.iterator().next();
  }
  public Trigger getTrigger() {
    return (triggers == null || triggers.isEmpty()) ? null : triggers.iterator().next();
  }
  public Notification getNotification() {
    return (notifications == null || notifications.isEmpty()) ? null : notifications.iterator().next();
  }
  public Adapter getAdapter() {
    return (connections == null || connections.isEmpty() || connections.iterator() == null) ? null : connections.iterator().next();
  }
  public Record getRecord() {
    return (documents == null || documents.isEmpty() || documents.iterator() == null) ? null : documents.iterator().next();
  }
  //  public static final String SERVICE_ID_COL = "service_id";
//  @DatabaseField(columnName = SERVICE_ID_COL, foreign = true, foreignAutoRefresh = true)
//  Service service;
//  public static final String TRIGGER_ID_COL = "trigger_id";
//  @DatabaseField(columnName = TRIGGER_ID_COL, foreign = true, foreignAutoRefresh = true)
//  Trigger trigger;
//  public static final String NOTIFICATION_ID_COL = "notification_id";
//  @DatabaseField(columnName = NOTIFICATION_ID_COL, foreign = true, foreignAutoRefresh = true)
//  Notification notification;

  public Node() {
  }
  public Node(String name, String path) {
    this.name = name;
    this.path = path;
  }

//  public Service getService() {
//    return service;
//  }
//
//  public void setService(Service service) {
//    this.type = Type.SERVICE;
//    this.service = service;
//  }
//
//  public Trigger getTrigger() {
//    return trigger;
//  }
//
//  public void setTrigger(Trigger trigger) {
//    this.type = Type.TRIGGER;
//    this.trigger = trigger;
//  }
//
//  public Notification getNotification() {
//    return notification;
//  }
//
//  public void setNotification(Notification notification) {
//    this.type = Type.NOTIFICATION;
//    this.notification = notification;
//  }

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public WmPackage getWmPackage() {
    return wmPackage;
  }

  public void setWmPackage(WmPackage wmPackage) {
    this.wmPackage = wmPackage;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getFullName() {
    if (this.path == null || this.path.equals(""))
      return this.name;
    else
      return this.path+":"+this.name;
  }
}
