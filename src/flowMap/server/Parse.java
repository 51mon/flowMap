package flowMap.server;

import com.j256.ormlite.dao.Dao;
import com.wm.app.b2b.server.BaseService;
import com.wm.app.b2b.server.FlowSvcImpl;
import com.wm.app.b2b.server.JavaService;
import com.wm.app.b2b.server.ServiceException;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataUtil;
import com.wm.app.b2b.server.dispatcher.trigger.Trigger;
import com.wm.lang.flow.*;
import com.wm.lang.ns.NSField;
import com.wm.lang.ns.NSNode;
import com.wm.lang.ns.NSRecord;
import com.wm.lang.ns.NSRecordRef;
import com.wm.msg.MessageTypeFilterPair;
import com.wm.util.Values;
import flowMap.model.*;
import flowMap.model.flow.Element;
import flowMap.model.flow.Input;
import flowMap.model.flow.MapItem;
import flowMap.model.flow.Mapping;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parse {
  public enum MapType { INPUT, OUTPUT, MAP }

  public static Element parseFlow(IDataCursor cursor, Element parent, int position, FlowElement flow,
                                  flowMap.model.Service parent_service) throws ServiceException, SQLException {
    Element element = new Element(parent, position);
    element.setService(parent_service);
    Util.api.createOrUpdate(Element.class, element);
    element.setLabel(flow.getDebugLabel());
    //cursor.insertAfter(flow.getClass().getName(), flow.getDebugLabel());
    element.setComment(flow.getComment());
    element.setScope(flow.getScope());
    element.setTimeout((int)flow.getTimeout());
    element.setEnabled(flow.isEnabled());
    if (flow instanceof FlowRoot) {
      element.setType(Element.Types.ROOT);
      cursor.insertAfter(flow.getClass().getName(), "");
      element.setService(parent_service);
    } else if (flow instanceof FlowInvoke) {
      FlowInvoke invoke = (FlowInvoke) flow;
      element.setType(Element.Types.INVOKE);
      String fullname = invoke.getService().getFullName();
      cursor.insertAfter(flow.getClass().getName(), fullname);
      element.setServiceNodeFullName(invoke.getService().getFullName());
      if (flow.getInputMap() != null)
        parseFlow(cursor, element, 0, flow.getInputMap(), parent_service);
      if (flow.getOutputMap() != null)
        parseFlow(cursor, element, 1, flow.getOutputMap(), parent_service);
    } else if (flow instanceof FlowMap) {
      element.setType(Element.Types.MAP);
      cursor.insertAfter(flow.getClass().getName(), "");
      parseMap(cursor, (FlowMap) flow, element, parent_service);
    } else if (flow instanceof FlowLoop) {
      element.setType(Element.Types.LOOP);
      FlowLoop loop = (FlowLoop) flow;
      cursor.insertAfter(flow.getClass().getName(), loop.getInArray()+" "+loop.getOutArray());
      element.setInputArray(loop.getInArray());
      element.setOutputArray(loop.getOutArray());
    } else if (flow instanceof FlowBranch) {
      element.setType(Element.Types.BRANCH);
      cursor.insertAfter(flow.getClass().getName(), "");
      FlowBranch branch = (FlowBranch) flow;
      Object evaluateLabelsObj = IDataUtil.get(branch.getAsData().getCursor(), "evaluate-labels");
      element.setEvaluateLabels("true".equals(evaluateLabelsObj));
      element.setSwitch(branch.getValues().getString("switch"));
    } else if (flow instanceof FlowSequence) {
      element.setType(Element.Types.SEQUENCE);
      cursor.insertAfter(flow.getClass().getName(), "");
      FlowSequence seq = (FlowSequence) flow;
      Values values = seq.getValues();
      element.setExitOn(values.getString("exit-on"));
    } else if (flow instanceof FlowRetry) {
      element.setType(Element.Types.REPEAT);
      cursor.insertAfter(flow.getClass().getName(), "");
      FlowRetry repeat = (FlowRetry) flow;
      Values values = repeat.getValues();
      element.setCount(values.getInt("count"));
      element.setRepeatInterval(values.getInt("backoff"));
      element.setRepeatOn(values.getString("repeat-on"));
    } else if (flow instanceof FlowExit) {
      element.setType(Element.Types.EXIT);
      cursor.insertAfter(flow.getClass().getName(), "");
      FlowExit exi = (FlowExit) flow;
      Values values = exi.getValues();
      element.setFrom(values.getString("from"));
      element.setSignal(values.getString("signal"));
      element.setFailureMessage(values.getString("failure-message"));
    } else {
      throw new ServiceException ("Unknown FlowElement type "+flow.getClass().getName());
    }
    Util.api.createOrUpdate(Element.class, element);

    if (flow instanceof FlowRoot || flow instanceof FlowBranch || flow instanceof FlowSequence
        || flow instanceof FlowRetry || flow instanceof FlowLoop) {
      for (int i = 0; i<flow.getNodes().length; i++) {
        parseFlow(cursor, element, i, flow.getNodes()[i], parent_service);
      }
    }
    return element;
  }
  public static void setPackageOnNode(IDataCursor cursor, Node node) throws ServiceException {
    try {
      List<WmPackage> list = Util.api.getDao(WmPackage.class).queryBuilder().where()
          .eq(WmPackage.NAME_COL, node.getPackageName()).query();
      if (list.isEmpty())
        return;
      WmPackage wmPackage = list.iterator().next();
      node.setWmPackage(wmPackage);
      Util.api.getDao(Node.class).update(node);
    } catch (java.sql.SQLException e) {
      throw new ServiceException(e);
    }
  }

  public static Node parseService(IDataCursor cursor, String serviceName, WmPackage wmPackage) throws ServiceException, SQLException {
    cursor.insertAfter("parseService", serviceName);
    NSNode node = Util.getNode(serviceName);
    //cursor.insertAfter("node class", node.getClass().getName());
    if (node == null) {
      throw new ServiceException("Service not found: "+serviceName);
      //return;
    }
    String[] splitName = Util.splitFullName(node.getNSName().getFullName());
    Dao<Node,Integer> dao = Util.api.getDao(Node.class);
    int id = Util.api.deleteNode(splitName[0], splitName[1]);

    Node myNode = new Node(splitName[1], splitName[0]);
    if (id != 0)
      myNode.setId(id);
    //Util.api.findOrCreate(serviceDao, splitName[0], splitName[1]);
    String packageName = node.getPackage().getName();
    myNode.setPackageName(packageName);
    if (wmPackage != null)
      myNode.setWmPackage(wmPackage);
    else
      setPackageOnNode(cursor, myNode);
    String comment = node.getComment();
    myNode.setComment(comment);
    dao.create(myNode);
    if (packageName.matches("Wm.*"))
      return myNode;
    //Dao<flowMap.model.Service,Integer> serviceDao = Util.api.getDao(Service.class);
    //cursor.insertAfter("NSNode package", packageName);
    //cursor.insertAfter("NSNode comment", comment);
    if (node instanceof BaseService) {
      myNode.setType(Node.Type.SERVICE);
      //cursor.insertAfter("BaseService", packageName);
      BaseService baseService = (BaseService) node;
      flowMap.model.Service myService = new flowMap.model.Service(myNode);
      Util.api.createOrUpdate(flowMap.model.Service.class, myService);
      if (node instanceof FlowSvcImpl || node instanceof JavaService) {
        if (baseService.getSignature() != null) {
          if (baseService.getSignature().getInput() != null) {
            Record inputRecord = parseNSRecord(cursor, baseService.getSignature().getInput(), null);
            inputRecord.setServiceInput(myService);
            Util.api.createOrUpdate(Record.class, inputRecord);
          }
          if (baseService.getSignature().getOutput() != null) {
            Record outputRecord = parseNSRecord(cursor, baseService.getSignature().getOutput(), null);
            outputRecord.setServiceOutput(myService);
            Util.api.createOrUpdate(Record.class, outputRecord);
          }
        }
        if (node instanceof FlowSvcImpl) {
          myService.setType(flowMap.model.Service.Type.FLOW);
          cursor.insertAfter("FlowSvcImpl", packageName);
          FlowSvcImpl flowSvc = (FlowSvcImpl) node;
          FlowRoot flowRoot = flowSvc.getFlowRoot();
          //cursor.insertAfter("flow service", flowRoot);
          Element myRoot = parseFlow(cursor, null, 0, flowRoot, myService);
          myService.setFlowRoot(myRoot);
          // recurse (or not)
        } else if (node instanceof JavaService) {
          //cursor.insertAfter("JavaService", packageName);
          myService.setType(flowMap.model.Service.Type.JAVA);
          JavaService javaService = (JavaService) node;
          cursor.insertAfter("className", javaService.getClassName());
          myService.setClassName(javaService.getClassName());
          myService.setMethodName(javaService.getMethodName());
        }
      } else if ("com.wm.pkg.art.ns.AdapterServiceNode".equals(node.getClass().getName())) {
        try {
          myService.setType(flowMap.model.Service.Type.ADAPTER);
          java.lang.reflect.Method getConnName = node.getClass().getMethod("getConnectionName", new Class[]{});
          String connName = (String) getConnName.invoke(node, new Object[] {});
          cursor.insertAfter("getConnectionName", connName);
          myService.setAdapterFullName(connName);
        } catch (Exception e) {
          throw new ServiceException(e);
        }
      }
      myService.setAvailable(baseService.isAvailable());
      myService.setStateless(baseService.isStateless());
      myService.setCacheEnabled(baseService.isCacheEnabled());
      myService.setCacheTTL(baseService.getCacheTTL());
      myService.setPrefetchEnabled(baseService.isPrefetchEnabled());
      myService.setPrefetchLevel(baseService.getPrefetchLevel());
      myService.setDescription(baseService.getDescription());
      myService.setBinding(baseService.getBinding());
      myService.setTemplate(baseService.getTemplate());
      myService.setTemplateType(baseService.getTemplateType());
      myService.setPackageName(baseService.getPackageName());
      myService.setLoadErr(baseService.getLoadErr());
      myService.setAuditLevel(baseService.getAuditLevel());
      myService.setSystemService(baseService.isSystemService());
      myService.setRetryMax(baseService.getRetryMax());
      myService.setRetryInterval(baseService.getRetryInterval());
      myService.setMaxRetryPeriod(baseService.getMaxRetryPeriod());
      Util.api.createOrUpdate(flowMap.model.Service.class, myService);
      //cursor.insertAfter("myService.getFlowRoot()", myService.getFlowRoot());
    } else if (node instanceof Trigger) {
      myNode.setType(Node.Type.TRIGGER);
      //cursor.insertAfter("parse TRIGGER", service);
      flowMap.model.Trigger myTrigger = new flowMap.model.Trigger(myNode);
      Trigger trigger = (Trigger) node;
      myTrigger.setDeliveryEnabled(trigger.getProcessingState().isDeliveryEnabled());
      myTrigger.setExecuteEnabled(trigger.getProcessingState().isExecuteEnabled());
      Util.api.getDao(flowMap.model.Trigger.class).create(myTrigger);
      for (com.wm.msg.ICondition cond : trigger.getConditions()) {
        TriggerCondition trigCond = new TriggerCondition(myTrigger, cond.getName());
        //cursor.insertAfter("parse TRIGGERcond", cond);
        cursor.insertAfter("cond.getName", cond.getName());
        trigCond.setServiceNodeFullName(cond.getServiceName().getFullName());
        Util.api.getDao(TriggerCondition.class).create(trigCond);
        List<MessageTypeFilterPair> pairs =
            (List<MessageTypeFilterPair>) cond.getMessageTypeFilterPairs();
        for (MessageTypeFilterPair pair : pairs) {
          cursor.insertAfter(pair.getMessageType(), pair.getFilter().getSource());
          DocFilterPair docFilter = new DocFilterPair(trigCond);
          docFilter.setRecordNodeFullName(pair.getMessageType());
          docFilter.setFilter(pair.getFilter().getSource());
          Util.api.getDao(DocFilterPair.class).create(docFilter);
        }
      }
    } else if (node.getClass().getName().equals("com.wm.pkg.art.ns.NotificationNode")) {
      myNode.setType(Node.Type.NOTIFICATION);
      try {
        Notification notification = new Notification(myNode);
        java.lang.reflect.Method getDoc = node.getClass().getMethod("getFullPublishableDocumentName", new Class[] {});
        String docName = (String) getDoc.invoke(node, new Object[] {});
        //cursor.insertAfter("docName", docName);
        //cursor.insertAfter("myNode notif", node.getAsData());

        IData getNotificationProperties = (IData) node.getClass().getMethod("getNotificationProperties", new Class[] {})
            .invoke(node, new Object[] {});
        cursor.insertAfter("myNode node.getNotificationProperties", getNotificationProperties);
        notification.setRecordNodeFullName(docName);

        String connName = (String) node.getClass().getMethod("getConnectionDataNodeName", new Class[]{}).invoke(node, new Object[]{});
        String listenName = (String) node.getClass().getMethod("getListenerNodeName", new Class[]{}).invoke(node, new Object[]{});
        if (connName != null) {
          notification.setAdapterKind(Adapter.Kind.CONNECTION);
          notification.setAdapterFullName(connName);
        } else {
          notification.setAdapterKind(Adapter.Kind.LISTENER);
          notification.setAdapterFullName(listenName);
        }

      /*java.lang.reflect.Method getAdapterTypeName = node.getClass().getMethod("getAdapterTypeName", new Class[] {});
      String adapterTypeName = (String) getAdapterTypeName.invoke(node, new Object[] {});
      notification.setAdapterTypeName(adapterTypeName);*/

        Util.api.getDao(Notification.class).create(notification);
      } catch (Exception e) {
        throw new ServiceException(e);
      }
    } else if (node.getClass().getName().equals("com.wm.pkg.art.ns.ListenerNode")) {
      try {
        cursor.insertAfter("com.wm.pkg.art.ns.ListenerNode", node);
        myNode.setType(Node.Type.ADAPTER);
        Adapter conn = new Adapter(myNode);
        conn.setKind(Adapter.Kind.LISTENER);
        java.lang.reflect.Method getAdapterTypeName = node.getClass().getMethod("getAdapterTypeName", new Class[] {});
        String adapterTypeName = (String) getAdapterTypeName.invoke(node, new Object[] {});
        conn.setAdapterTypeName(adapterTypeName);

        IData getListenerProperties = (IData) node.getClass().getMethod("getListenerProperties", new Class[] {})
            .invoke(node, new Object[] {});
        conn.setHostname(IDataUtil.getString(getListenerProperties.getCursor(), "gatewayHost"));
        conn.setUsername(IDataUtil.getString(getListenerProperties.getCursor(), "gatewayService"));
        conn.setAlias("programId="+IDataUtil.getString(getListenerProperties.getCursor(), "programId")
            +" repositoryServer="+IDataUtil.getString(getListenerProperties.getCursor(), "repositoryServer"));
        conn.setEnabled((Boolean) node.getClass().getMethod("getEnabledStatus", new Class[]{}).invoke(node, new Object[] {}));
        Util.api.getDao(Adapter.class).create(conn);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } else if (node.getClass().getName().equals("com.wm.pkg.art.ns.ConnectionDataNode")) { // Not present
      Adapter conn = new Adapter(myNode);
      myNode.setType(Node.Type.ADAPTER);
      conn.setKind(Adapter.Kind.CONNECTION);
      try {
        String getNodeName = (String) node.getClass().getMethod("getNodeName", new Class[]{}).invoke(node, new Object[] {});
        cursor.insertAfter("CONNECTION getNodeName", getNodeName);
        IData getConnectionProperties = (IData) node.getClass().getMethod("getConnectionProperties", new Class[]{}).invoke(node, new Object[] {});
        //cursor.insertAfter("CONNECTION getConnectionProperties", getConnectionProperties);

        java.lang.reflect.Method getAdapterTypeName = node.getClass().getMethod("getAdapterTypeName", new Class[] {});
        String adapterTypeName = (String) getAdapterTypeName.invoke(node, new Object[] {});
        //cursor.insertAfter("adapterTypeName", adapterTypeName);
        conn.setAdapterTypeName(adapterTypeName);
        if ("WmSAP".equals(adapterTypeName)) {
          conn.setAlias(IDataUtil.getString(getConnectionProperties.getCursor(), "alias"));
          conn.setUsername(IDataUtil.getString(getConnectionProperties.getCursor(), "user"));
          conn.setHostname(IDataUtil.getString(getConnectionProperties.getCursor(), "appServerHost"));
          IDataUtil.getString(getConnectionProperties.getCursor(), "client");
          IDataUtil.getString(getConnectionProperties.getCursor(), "systemNumber");
        } else if ("JDBCAdapter".equals(adapterTypeName)) {
          conn.setAlias(IDataUtil.getString(getConnectionProperties.getCursor(), "databaseName"));
          conn.setUsername(IDataUtil.getString(getConnectionProperties.getCursor(), "user"));
          conn.setHostname(IDataUtil.getString(getConnectionProperties.getCursor(), "serverName"));
        }
        conn.setEnabled((Boolean) node.getClass().getMethod("isEnabled", new Class[]{}).invoke(node, new Object[] {}));
        Util.api.getDao(Adapter.class).create(conn);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } else if (node instanceof com.wm.lang.ns.NSRecord) {
      myNode.setType(Node.Type.RECORD);
      Record record = parseNSRecord(cursor, (com.wm.lang.ns.NSRecord) node, null);
      record.setNode(myNode);
      Util.api.createOrUpdate(Record.class, record);
    } else {
      cursor.insertAfter("unknown node class name", node.getClass().getName());
      cursor.insertAfter("unknown node object", node);
    }
    dao.update(myNode);
    return myNode;
  }

  public static Record parseNSRecord(IDataCursor cursor, NSField record, Record parent)
      throws ServiceException {
    if (record == null) return null;
    //cursor.insertAfter("start parseNSRecords", record.getName());
    Record myRecord = new Record(record.getName());
    if (parent != null)
      myRecord.setParent(parent);
    //String type = "unknown";
    myRecord.setType(Record.getTypeFromInt(record.getType()));
    //cursor.insertAfter("type", type);
    //cursor.insertAfter("getDimensions", record.getDimensions());
    try {
      Util.api.getDao(Record.class).create(myRecord);
      if (record instanceof NSRecordRef) {
        myRecord.setRecordNodeFullName(((NSRecordRef)record).getTargetName().getFullName());
      } else if (record instanceof NSRecord) {
        for (NSField field : ((NSRecord)record).getFields()) {
          parseNSRecord(cursor, field, myRecord);
        }
      }
      Util.api.getDao(Record.class).update(myRecord);
    } catch (Exception e) {
      throw new ServiceException(e);
    }
    return myRecord;
  }

  public static Element parseMap(IDataCursor cursor, FlowMap inputMap, Element el, flowMap.model.Service parent_service)
      throws ServiceException, SQLException {
    //Map<String,String> results = new HashMap<String,String>();
    if (inputMap == null) return null;
    if (inputMap.getSetMaps() != null)
      for (FlowMapSet set : inputMap.getSetMaps()) {
        //cursor.insertAfter("variableSubstitution?", !set.isVariables());
        cursor.insertAfter("parseMap", set.getField());
        //cursor.insertAfter("parseMapping", set.getInput());
        MapItem mapItem = new MapItem(el, MapItem.Type.SET);
        Util.api.getDao(MapItem.class).create(mapItem);
        //Util.log(set.getInput().getClass().getName());
        if (set.getInput() instanceof com.wm.util.Values) { // Doc
          IDataCursor cursor1 = ((com.wm.util.Values) set.getInput()).getCursor();
          while (cursor1.hasMoreData()) {
            Util.api.getDao(Input.class).create(new Input(mapItem, cursor1.getKey(), cursor1.getValue()));
            cursor1.next();
          }
        } else if (set.getInput() instanceof com.wm.util.Values[]) { // Array of docs
          com.wm.util.Values[] valuesArray = (com.wm.util.Values[]) set.getInput();
          for (int i = 0; i<valuesArray.length; i++) {
            IDataCursor cursor1 = valuesArray[i].getCursor();
            while (cursor1.hasMoreData()) {
              Util.api.getDao(Input.class).create(new Input(mapItem, i, cursor1.getKey(), cursor1.getValue()));
              cursor1.next();
            }
          }
        } else if (set.getInput() instanceof Object) {
          Util.api.createOrUpdate(Input.class, new Input(mapItem, set.getInput()));
        } else if (set.getInput() instanceof Object[]) {
          String[] array = (String[]) set.getInput();
          for (int i = 0; i<array.length; i++)
            Util.api.getDao(Input.class).create(new Input(mapItem, i, array[i]));
        } else if (set.getInput() instanceof String[][]) {
          String[][] array = (String[][]) set.getInput();
          for (int i = 0; i<array.length; i++) {
            Input input = Util.api.createOrUpdate(Input.class, new Input(mapItem, i, null));
            for (int j = 0; j<array[i].length; i++) {
              Util.api.createOrUpdate(Input.class, new Input(mapItem, j, array[i][j]));
            }
          }
        } else if (set.getInput() == null) {
        } else {
          throw new RuntimeException("Unknown input "+set.getInput().getClass().getName()+" "+set.getInput());
        }
        mapItem.setVariableSubstitution(set.isVariables());
        //Util.log("check mapItem "+mapItem.getInput()+" "+mapItem.isVariableSubstitution());
        Util.api.getDao(MapItem.class).update(mapItem);
        Mapping mapping = parseMapping(set.getField(), null, null, mapItem, null);
        //el.(set.getField().replaceAll("^[/]", "").replaceAll(";1;0$", ""), set.getInput().toString());
      }
    if (inputMap.getCopyMaps() != null)
      for (FlowMapCopy set : inputMap.getCopyMaps()) {
        //cursor.insertAfter("parseMapping", set.getMapFrom());
        //cursor.insertAfter("parseMapping", set.getMapTo());
        MapItem mapItem = new MapItem(el, MapItem.Type.COPY);
        Util.api.createOrUpdate(MapItem.class, mapItem);
        Mapping fromMapping = parseMapping(set.getMapFrom(), null, mapItem, null, null);
        Mapping toMapping = parseMapping(set.getMapTo(), null, null, mapItem, null);
      }
    if (inputMap.getDeleteMaps() != null)
      for (FlowMapDelete del : inputMap.getDeleteMaps()) {
        //cursor.insertAfter("parseMapping", del.getField());
        MapItem mapItem = new MapItem(el, MapItem.Type.DELETE);
        Util.api.createOrUpdate(MapItem.class, mapItem);
        Mapping mapping = parseMapping(del.getField(), null, null, null, mapItem);
      }
    // transformers
    if (inputMap.getInvokeMaps() != null)
      for (FlowMapInvoke invokeMap : inputMap.getInvokeMaps())
        parseFlow(cursor, el, invokeMap.getInvokeOrder(), invokeMap, parent_service);
    return el;
  }

  public static Mapping parseMapping(String variable, Mapping parent, MapItem mapCopyFrom,
                                     MapItem mapCopyTo, MapItem mapUpdate) throws ServiceException {
    //Doesn't work because sometimes a ; is missing !!
    //String[] split = variable.split(";");
    //Pattern pattern = Pattern.compile("^([^\\[;])+(?:\\[(\\d+)\\])?(?:\\[(\\d+)\\])?");
    Pattern pattern = Pattern.compile("^([^\\[;]*)(?:\\[([a-zA-Z_0-9%]+)\\])?(?:\\[([a-zA-Z_0-9%]+)\\])?;((?:\\d+\\.)?\\d+);((?:\\d+\\.)?\\d+)(?:;?(.*))?");
    Matcher m = pattern.matcher(variable);
    if (!m.matches())
      return parent;
    Mapping mapping = new Mapping(m.group(1), m.group(2), m.group(3), m.group(4), m.group(5), parent);
    mapping.setMapCopyFrom(mapCopyFrom);
    mapping.setMapCopyTo(mapCopyTo);
    mapping.setMapUpdate(mapUpdate);
    //Mapping root = null;
    //for (int i = 0; i<split.length/3; i++) {
    //  Matcher m = pattern.matcher(split[3*i+0]);
    //  if (!m.matches())
    //    throw new RuntimeException(split[3*i+0]);
    //  Mapping mapping = new Mapping(m.group(1), m.group(2), m.group(3), split[3*i+1], split[3*i+2], parent);
    //  if (root == null)
    //    root = mapping;
    //  parent = mapping;
    try {
      Util.api.getDao(Mapping.class).create(mapping);
    } catch (Exception e) {
      throw new ServiceException(e);
    }
    //}
    //return root;
    return parseMapping(m.group(6), mapping, null, null, null);
  }

}
