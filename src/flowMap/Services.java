package flowMap;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.stmt.QueryBuilder;
import com.wm.app.b2b.server.ServiceException;
import com.wm.data.IDataCursor;
import flowMap.model.*;
import flowMap.model.flow.Element;
import flowMap.model.flow.MapItem;
import flowMap.model.flow.Mapping;
import flowMap.model.tn.ProcessingRule;
import flowMap.server.Import;
import flowMap.server.Parse;
import flowMap.server.Util;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
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
    services.add(new ServiceDef(Services.class.getName(), "linkAll"));
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
    services.add(new ServiceDef(Services.class.getName(), "createFlows"));
    services.add(new ServiceDef(Services.class.getName(), "createTables"));
    services.add(new ServiceDef(Services.class.getName(), "dropTables"));
  }
  public static void createTables(IDataCursor cursor) throws ServiceException {
    try {
      Util.api.createTables();
    } catch (SQLException e) {
      throw new ServiceException(e);
    }
  }
  public static void dropTables(IDataCursor cursor) throws ServiceException {
    try {
      Util.api.dropTables();
    } catch (SQLException e) {
      throw new ServiceException(e);
    }
  }
  public static void importAndLinkAll(IDataCursor cursor) throws ServiceException {
    Import.importIS(cursor);
    Import.importAdapterTypes(cursor);
    Import.importPackages(cursor);
    Import.importTnProcessingRules(cursor);
    linkAll(cursor);
  }
  public static void linkAll(IDataCursor cursor) throws ServiceException {
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

  public static int parseService(IDataCursor cursor, Service service, int position, Flow flow)
      throws SQLException {
    service = Util.api.getDao(Service.class).queryForId(service.getId());
    for (Element flowElement : service.getFlows()) { // All the flows of the service !
      //Util.api.refresh(Element.class, flowElement); // doesn't work for loading the collections
      flowElement = Util.api.getDao(Element.class).queryForId(flowElement.getId());
      position = parseElement(cursor, flowElement, position, flow);
    }
    return position;
  }
  public static int parseElement(IDataCursor cursor, Element flowElement, int position, Flow flow)
      throws SQLException {
    String name = flowElement.getServiceNodeFullName();
    cursor.insertAfter("parseElement", flowElement.getId()+" "+name);
    if (flowElement.getType() == Element.Types.ROOT ||
        flowElement.getType() == Element.Types.BRANCH ||
        flowElement.getType() == Element.Types.LOOP ||
        flowElement.getType() == Element.Types.REPEAT ||
        flowElement.getType() == Element.Types.SEQUENCE) { // No need to recurse on them as all flows are listed before
      //cursor.insertAfter("parseService loop on the children", flowElement.getId());
//      for (Element subFlowItem : flowElement.getChildren()) {
//        cursor.insertAfter("parseService children", subFlowItem.getId());
//        Util.api.refresh(Element.class, subFlowItem);
//        parseElement(cursor, subFlowItem, position, flow);
//      }
    } else if (flowElement.getType() == Element.Types.MAP) { // search for invokes (transformer)
      cursor.insertAfter("parseService Invoke in map", flowElement.getId());
      Iterator<Element> inputIt = flowElement.getChildren().iterator();
      while (inputIt.hasNext()) {
        Element invoke = inputIt.next();
        if (invoke.getType() == Element.Types.INVOKE) {
          position = parseElement(cursor, invoke, position, flow);
        }
      }
    } else if (flowElement.getType() == Element.Types.INVOKE) {
      if (name.equals("FrmkTeSupervision.utils:sendTrace") ||
          name.equals("FrmkTrace:sendTrace") ||
          name.equals("pub.publish:publish") ||
          name.equals("FrmkTeSupervision.utils:initSEAI")) {
        //cursor.insertAfter("Service", name);
        FlowItem item = new FlowItem(flow, position++);
        item.setElement(flowElement);
        Util.api.createOrUpdate(FlowItem.class, item);
        if (name.equals("FrmkTeSupervision.utils:initSEAI")) { // Define the flow
          if (flow.getName() != null) { // If already defined create a new one
            flow = new Flow();
            Util.api.createOrUpdate(Flow.class, flow);
          }
          if (flowElement.getChildren().size() != 2)
            throw new RuntimeException("Expecting 2 children : input & output");
          Element mapInputEl = flowElement.getChildren().iterator().next();
          Iterator<MapItem> inputMapItems = mapInputEl.getMapItems().iterator();
          Element outputEl = flowElement.getChildren().iterator().next();
          String flowName = null;
          String subFlowName = null;
          while (inputMapItems.hasNext()) {
            MapItem inputMap = inputMapItems.next();
            Util.api.refresh(MapItem.class, inputMap);
            if (inputMap.getType() == MapItem.Type.SET) {
              Iterator<Mapping> itMappings = inputMap.getUpdateMappings().iterator();
              while (itMappings.hasNext()) {
                String field = itMappings.next().getName();
                String value = inputMap.getInputs().iterator().next().getString();
                cursor.insertAfter("initSEAI setter", field+" = "+value);
                if (field.equals("/processName"))
                  flowName = value;
                else if (field.equals("/subProcessName"))
                  subFlowName = value;
              }
            } else if (inputMap.getType() == MapItem.Type.COPY) {
              Iterator<Mapping> itFromMappings = inputMap.getCopyFromMappings().iterator();
              Iterator<Mapping> itToMappings = inputMap.getCopyToMappings().iterator();
              while (itFromMappings.hasNext()) {
                Mapping fromMapping = itFromMappings.next();
                Mapping toMapping = itToMappings.next();
                if (toMapping.getName().equals("/processName"))
                  flowName = fromMapping.getName();
                if (toMapping.getName().equals("/subProcessName"))
                  subFlowName = fromMapping.getName();
              }
            }
          }
          Flow alreadyCreated = Util.api.getDao(Flow.class).queryBuilder().where()
              .eq(Flow.NAME_COL, flowName).and()
              .eq(Flow.SUB_PROCESS_NAME_COL, subFlowName).and()
              .ne(Flow.ID_COL, flow.getId())
              .queryForFirst();
          if (alreadyCreated != null) {
            Util.api.refresh(Flow.class, flow); // Refresh to get the items
            Iterator<FlowItem> itItems = flow.getItems().iterator();
            while (itItems.hasNext()) {         // Re assign the items
              FlowItem _item = itItems.next();
              _item.setFlow(alreadyCreated);
              Util.api.createOrUpdate(FlowItem.class, _item);
            }
            //Util.api.getDao(Flow.class).delete(flow);
            flow.setId(alreadyCreated.getId());
          }
          flow.setName(flowName);
          flow.setSubProcessName(subFlowName);
          Util.api.createOrUpdate(Flow.class, flow);
        }
      } else if (flowElement.getServiceNode() == null) {
      } else { // service normal
        Util.api.refresh(Node.class, flowElement.getServiceNode());
        if (flowElement.getServiceNode().getType() == Node.Type.SERVICE) {
          //cursor.insertAfter("Service", flowElement.getServiceNode().getFullName());
          if (flowElement.getServiceNode().getService() != null) {
            position = parseService(cursor, flowElement.getServiceNode().getService(), position, flow);
          } else {
            cursor.insertAfter("warning", "Service is null");
          }
        } else {
          //cursor.insertAfter("Service", flowElement.getServiceNode().getType());
          FlowItem item = new FlowItem(flow, position++);
          item.setNode(flowElement.getServiceNode());
        }
      }
    }
    return position;
  }
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
      List<Node> triggerNodes = Util.api.getDao(Node.class).queryBuilder().where()
          .eq(Node.TYPE_COL, Node.Type.TRIGGER).query();
      for (Node triggerNode : triggerNodes) {
        QueryBuilder<Flow,Integer> flowQb = Util.api.getDao(Flow.class).queryBuilder();
        QueryBuilder<FlowItem,Integer> flowItemQb = Util.api.getDao(FlowItem.class).queryBuilder();
        flowItemQb.where().eq(FlowItem.NODE_ID_COL, triggerNode);
        if (flowQb.join(flowItemQb).queryForFirst() != null)
          return;
        Flow flow = new Flow();
        Util.api.createOrUpdate(Flow.class, flow);
        Util.log("trigger "+triggerNode.getName());
        int position = 0;
        FlowItem item = new FlowItem(flow, position++);
        item.setNode(triggerNode);
        Util.api.createOrUpdate(FlowItem.class, item);
        ForeignCollection<TriggerCondition> conditions = triggerNode.getTrigger().getConditions();
        if (conditions.size() != 1)
          throw new RuntimeException("# of conditions != 1 on "+triggerNode.getFullName());
        TriggerCondition condition = conditions.iterator().next();
        Util.log("condition "+condition.getName());
        ForeignCollection<DocFilterPair> docFilterPairs = condition.getDocFilterPairs();
        if (docFilterPairs.size() != 1)
          throw new RuntimeException("# of docFilterPairs != 1 on condition on "+triggerNode.getFullName());
        DocFilterPair docFilterPair = docFilterPairs.iterator().next();
        item = new FlowItem(flow, position++);
        item.setNode(docFilterPair.getRecordNode());
        Util.api.createOrUpdate(FlowItem.class, item);
        Util.log("docFilterPair "+docFilterPair.getRecordNodeFullName());
        Util.api.refresh(Node.class, docFilterPair.getRecordNode());
        Notification notification = docFilterPair.getRecordNode().getNotification();
        if (notification != null) {
          item = new FlowItem(flow, position++);
          item.setNode(notification.getAdapter().getNode());
          Util.api.createOrUpdate(FlowItem.class, item);
        }
        Util.api.createOrUpdate(FlowItem.class, item);          // Set top level item : trigger (with adapter if it is one)
        item = new FlowItem(flow, position++); // Add the top level service
        item.setNode(condition.getServiceNode());
        Util.api.createOrUpdate(FlowItem.class, item);
        if (condition.getServiceNode().getType() == Node.Type.SERVICE && condition.getServiceNode().getService() != null) {
          Util.log("Service "+condition.getServiceNode().getFullName());
          position = parseService(cursor, condition.getServiceNode().getService(), position, flow);
        } else {
          throw new RuntimeException("Unexpected node "+condition.getServiceNode().getFullName());
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
          cursor.insertAfter("warning", "FlowMap: could not link service invoke "+serviceUser.getServiceNodeFullName());
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
