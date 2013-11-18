package flowMap;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.stmt.QueryBuilder;
import com.wm.app.b2b.server.ServiceException;
import com.wm.data.IDataCursor;
import flowMap.model.*;
import flowMap.model.flow.Element;
import flowMap.model.tn.ProcessingRule;
import flowMap.server.Import;
import flowMap.server.Parse;
import flowMap.server.Util;

import java.util.ArrayList;
import java.util.List;

public class Services {
  public static List<ServiceDef> services;
  static {
    services = new ArrayList<ServiceDef>();
    services.add(new ServiceDef(Services.class.getName(), "importAndLinkAll"));
    services.add(new ServiceDef(Import.class.getName(), "importIS"));
    services.add(new ServiceDef(Import.class.getName(), "importAdapterTypes"));
    services.add(new ServiceDef(Import.class.getName(), "importPackages"));
    services.add(new ServiceDef(Import.class.getName(), "importTnProcessingRules"));
    services.add(new ServiceDef(Import.class.getName(), "importPackage", new String[]{"packageName"}));
    services.add(new ServiceDef(Services.class.getName(), "linkNotificationsToAdapters"));
    services.add(new ServiceDef(Services.class.getName(), "linkServicesToAdapters"));
    services.add(new ServiceDef(Services.class.getName(), "linkAdaptersToAdapterTypes"));
    services.add(new ServiceDef(Services.class.getName(), "linkNotificationsToDocuments"));
    services.add(new ServiceDef(Services.class.getName(), "linkDocFilterPairsToDocuments"));
    services.add(new ServiceDef(Services.class.getName(), "linkPackagesToNodes"));
    services.add(new ServiceDef(Services.class.getName(), "linkTriggerConditionsToServiceNodes"));
    services.add(new ServiceDef(Services.class.getName(), "linkElementToServiceNodes"));
    services.add(new ServiceDef(Services.class.getName(), "linkRulesToServiceNodes"));
    services.add(new ServiceDef(Services.class.getName(), "linkRulesToDocuments"));
    services.add(new ServiceDef(Services.class.getName(), "linkRecordsToDocuments"));
  }
  public static void importAndLinkAll(IDataCursor cursor) throws ServiceException {
    Import.importIS(cursor);
    Import.importAdapterTypes(cursor);
    Import.importPackages(cursor);
    Import.importTnProcessingRules(cursor);
    linkNotificationsToAdapters(cursor);
    linkServicesToAdapters(cursor);
    linkAdaptersToAdapterTypes(cursor);
    linkNotificationsToDocuments(cursor);
    linkDocFilterPairsToDocuments(cursor);
    linkPackagesToNodes(cursor);
    linkTriggerConditionsToServiceNodes(cursor);
    linkElementToServiceNodes(cursor);
    linkRulesToServiceNodes(cursor);
    linkRulesToDocuments(cursor);
    linkRecordsToDocuments(cursor);
  }
// TODO
  public static void createFlows(IDataCursor cursor) {
    try {
//      List<Element> invokes = Util.api.getDao(Element.class).queryBuilder().where()
//          .eq(Element.SERVICE_FULL_NAME_COL, "FrmkTeSupervision.utils:sendTrace").query();
//      for (Element invoke : invokes) {
//        if (!invoke.getType().equals(Element.Types.INVOKE))
//          continue;
//        Node node = invoke.getServiceNode();
//        // Do precedents
//      }
      List<Node> topNodes = Util.api.getDao(Node.class).queryBuilder().where()
          .eq(Node.TYPE_COL, Node.Type.TRIGGER).query();
      Dao<FlowItem,Integer> itemDao = Util.api.getDao(FlowItem.class);
      for (Node triggerNode : topNodes) {
        int position = 0;
        FlowItem item = new FlowItem(position++);
        ForeignCollection<TriggerCondition> conditions = triggerNode.getTrigger().getConditions();
        if (conditions.size() != 1)
          throw new RuntimeException("# of conditions != 1 on "+triggerNode.getFullName());
        TriggerCondition condition = conditions.iterator().next();
        ForeignCollection<DocFilterPair> docFilterPairs = condition.getDocFilterPairs();
        if (docFilterPairs.size() != 1)
          throw new RuntimeException("# of docFilterPairs != 1 on condition on "+triggerNode.getFullName());
        DocFilterPair docFilterPair = docFilterPairs.iterator().next();
        item.setNode(docFilterPair.getRecordNode());
        Notification notification = docFilterPair.getRecordNode().getNotification();
        if (notification != null) {
          item.setAdapter(notification.getAdapter());
        }
        itemDao.createOrUpdate(item);
        item = new FlowItem(position++);
        item.setNode(condition.getServiceNode());
        item = new FlowItem(position++);
        if (condition.getServiceNode().getType() == Node.Type.SERVICE) {
          for (Element flow : condition.getServiceNode().getService().getFlows()) {

          }
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static <T extends IServiceUser> void linkServiceUserToServiceNodes(Class<T> c, IDataCursor cursor) {
    try {
      Dao<T,Integer> dao = Util.api.getDao(c);
      for (T serviceUser : Util.api.getDao(c).queryBuilder()
          .where().isNotNull(IServiceUser.SERVICE_FULL_NAME_COL).query()) {
        //.where().eq(Element.TYPE_COL, Element.Types.INVOKE
        String[] splitName = Util.splitFullName(serviceUser.getServiceNodeFullName());
        List<Node> nodes = Util.api.getDao(Node.class).queryBuilder().where()
            .eq(Node.PATH_COL, splitName[0]).and()
            .eq(Node.NAME_COL, splitName[1]).query();
        if (!nodes.isEmpty())
          serviceUser.setServiceNode(nodes.get(0));
        else
          Util.log("FlowMap: could not link service invoke "+serviceUser.getServiceNodeFullName());
        dao.update(serviceUser);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  public static <T> void linkRulesToServiceNodes(IDataCursor cursor) {
    linkServiceUserToServiceNodes(ProcessingRule.class, cursor);
  }
  public static <T> void linkElementToServiceNodes(IDataCursor cursor) {
    linkServiceUserToServiceNodes(Element.class, cursor);
  }
  public static void linkTriggerConditionsToServiceNodes(IDataCursor cursor) {
    linkServiceUserToServiceNodes(TriggerCondition.class, cursor);
  }
  public static <T extends AdapterItem> void linkAdapterItemsToAdapters(Class<T> c) {
    try {
      Dao<T,Integer> dao = Util.api.getDao(c);
      for (T notif : dao.queryBuilder().where().isNotNull(AdapterItem.ADAPTER_FULL_NAME_COL).query()) {
        //String packageName = notif.getDocumentFullName().split(".")[0].split(":")[0];
        String[] splitConnName = Util.splitFullName(notif.getAdapterFullName());
        QueryBuilder queryNode = Util.api.getDao(Node.class).queryBuilder();
        queryNode.where().eq(Node.PATH_COL, splitConnName[0]).and().eq(Node.NAME_COL, splitConnName[1]);
        QueryBuilder queryConnection = Util.api.getDao(Adapter.class).queryBuilder();
        List<Adapter> conns = queryConnection.join(queryNode).query();
        if (conns.size() == 1) {
          notif.setAdapter(conns.get(0));
          Util.api.getDao(c).update(notif);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  public static <T> void linkNotificationsToAdapters(IDataCursor cursor) {
    linkAdapterItemsToAdapters(Notification.class);
  }
  public static <T> void linkServicesToAdapters(IDataCursor cursor) {
    linkAdapterItemsToAdapters(flowMap.model.Service.class);
  }

  public static void linkAdaptersToAdapterTypes(IDataCursor cursor) {
    try {
      for (Adapter adapter : Util.api.getDao(Adapter.class)) {
        AdapterType adapterType = Util.api.find(AdapterType.class, adapter.getAdapterTypeName());
        adapter.setAdapterType(adapterType);
        Util.api.getDao(Adapter.class).update(adapter);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  public static <T extends IRecordUser> void linkToDocuments(Class<T> c, IDataCursor cursor) {
    try {
      Dao<T,Integer> dao = Util.api.getDao(c);
      List<T> list = dao.queryBuilder().where()
          .isNotNull(IRecordUser.RECORD_FULL_NAME_COL)
          .query();
      for(T docUser : list) {
        String[] splitName = Util.splitFullName(docUser.getRecordNodeFullName());
        List<Node> nodes = Util.api.getDao(Node.class).queryBuilder().where()
            .eq(Node.PATH_COL, splitName[0]).and()
            .eq(Node.NAME_COL, splitName[1]).query();
        Node docNode = (!nodes.isEmpty()) ? nodes.get(0) :
            Parse.parseService(cursor, docUser.getRecordNodeFullName(), null);
        docUser.setRecordNode(docNode);

        dao.update(docUser);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  public static void linkDocFilterPairsToDocuments(IDataCursor cursor) {
    linkToDocuments(DocFilterPair.class, cursor);
  }
  public static void linkNotificationsToDocuments(IDataCursor cursor) {
    linkToDocuments(Notification.class, cursor);
  }
  public static void linkRulesToDocuments(IDataCursor cursor) {
    linkToDocuments(ProcessingRule.class, cursor);
  }
  public static void linkRecordsToDocuments(IDataCursor cursor) {
    linkToDocuments(Record.class, cursor);
  }
  public static void linkPackagesToNodes(IDataCursor cursor) throws ServiceException {
      for(Node node : Util.api.getDao(Node.class)) {
        Parse.setPackageOnNode(cursor, node);
      }
  }
}
