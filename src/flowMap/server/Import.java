package flowMap.server;

import com.j256.ormlite.dao.Dao;
import com.wm.app.b2b.server.*;
import com.wm.app.b2b.server.Package;
import com.wm.app.tn.doc.BizDocType;
import com.wm.app.tn.doc.XMLDocType;
import com.wm.app.tn.route.RoutingRule;
import com.wm.data.IDataCursor;
import com.wm.data.IDataUtil;
import com.wm.util.Values;
import flowMap.model.*;
import flowMap.model.tn.ProcessingRule;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Import {
  public static void recurseFolders(IDataCursor cursor, String _interface) throws ServiceException {
    com.wm.util.Values values = Util.invoke( "wm.server.xidl", "getInterfaces", new String[]{"interface", _interface});
    Values[] interfaces = values.getValuesArray("interfaces");
    if (interfaces != null)
      for (Values valuesInterface : interfaces) {
        recurseFolders(cursor, valuesInterface.getString("fullname"));
      }
    Values[] services = values.getValuesArray("services");
    if (services != null)
      for (Values service : services) {
        cursor.insertAfter(_interface, service.getString("name"));
      }
  }

  public static void importPackages(IDataCursor cursor) throws ServiceException {
    importIS(cursor);
    for (Package pkg : PackageManager.getAllPackages()) {
      if (pkg.getName().equals("WmEDI"))
        continue;
      Util.log("FlowMap import package "+pkg.getName());
      importPackage(cursor, pkg);
    }
  }
  public static void importPackage(IDataCursor cursor) throws ServiceException {
    String packageName = IDataUtil.getString(cursor, "packageName");
    importPackage(cursor, packageName);
  }
  public static void importPackage(IDataCursor cursor, String packageName) throws ServiceException {
    importPackage(cursor, PackageManager.getPackage(packageName));
  }

  public static void importPackage(IDataCursor cursor, Package pkg) throws ServiceException {
    try {
      cursor.insertAfter("package name", pkg.getName());
      WmPackage wmPackage = Util.api.findOrCreate(WmPackage.class, pkg.getName());
      //cursor.insertAfter(pkg.getName()+" id", wmPackage.getId());
      //Util.api.delete(WmPackage.class, pkg.getName());
      //WmPackage wmPackage = new WmPackage(pkg.getName());

      Dao<IS_WmPackage,Integer> iS_PackageDao = Util.api.getDao(IS_WmPackage.class);
      List<IS_WmPackage> listIsPackages = iS_PackageDao.queryBuilder().where()
          .eq(IS_WmPackage.IS_ID_COL, Util.getLocalIS()).and()
          .eq(IS_WmPackage.PACKAGE_ID_COL, wmPackage).query();
      if (listIsPackages.isEmpty())
        iS_PackageDao.create(new IS_WmPackage(Util.getLocalIS(), wmPackage));

      java.util.Enumeration services = pkg.getState().getLoaded();
      java.util.List<String> list = new java.util.ArrayList<String>();
      if ((services != null) && (services.hasMoreElements())) {
        while (services.hasMoreElements()) {
          String serviceName = (String)services.nextElement();
          String[] split = Util.splitFullName(serviceName);
          list.add(serviceName);
          Parse.parseService(cursor, serviceName, wmPackage);
        }
        cursor.insertAfter(pkg.getName(), list);
      }
    } catch (java.sql.SQLException e) {
      throw new ServiceException(e);
    }
  }

  public static IS importIS(IDataCursor cursor) throws ServiceException {
    String str_id = Util.getProperty(Util.IS_id);
    try {
      Dao<IS, Integer> isDao = Util.api.getDao(IS.class);
      IS is;
      if (str_id != null && str_id.matches("\\d+")) {
        int id = Integer.parseInt(str_id);
        is = isDao.queryForId(id);
        if (is == null) {
          is = new IS();
          is.setId(id);
        }
      } else
        is = new IS();
      is.setPort(ListenerAdmin.getPrimaryListener().getPort());
      is.setVersion(Build.getVersion());
      is.setBuild(Build.getBuild());
      is.setJavaVersion(System.getProperty("java.version"));
      is.setDirectory(System.getProperty("user.dir"));
      String[] splitDir = System.getProperty("user.dir").split("/");
      is.setName(splitDir[splitDir.length-2]);
      Dao<OS, Integer> osDao = Util.api.getDao(OS.class);
      OS os = Util.api.findOrCreate(osDao, System.getProperty("os.arch"), System.getProperty("os.name"),
          System.getProperty("os.version"));
      os.setHostName(java.net.InetAddress.getLocalHost().getHostName());
      osDao.createOrUpdate(os);
      is.setOS(os);
      isDao.createOrUpdate(is);
      Util.saveProperty(Util.IS_id, is.getId()+"");
      return is;
    } catch (Exception e) {
      throw new ServiceException(e);
    }
  }
  public static List<AdapterType> importAdapterTypes(IDataCursor cursor) throws ServiceException {
    Values output = Util.invoke("pub.art", "listRegisteredAdapters");
    Values[] adapters = output.getValuesArray("registeredAdapterList");
    List<AdapterType> result = new java.util.ArrayList<AdapterType>();
    try {
      Dao<AdapterType, Integer> adapterDao = Util.api.getDao(AdapterType.class);
      for (Values adapter : adapters) {
        String adapterTypeName = adapter.getString("adapterTypeName");
        String adapterDisplayName = adapter.getString("adapterDisplayName");
        AdapterType adapterType = Util.api.findOrCreate(AdapterType.class, adapterTypeName);
        adapterType.setDisplayName(adapterDisplayName);
        adapterDao.createOrUpdate(adapterType);
        result.add(adapterType);
        //cursor.insertAfter("adapterType.getId", adapterType.getId());
      }
      return result;
    } catch (java.sql.SQLException e) {
      throw new ServiceException(e);
    }
  }

  public static List<ProcessingRule> importTnProcessingRules(IDataCursor cursor) throws ServiceException {
    List<ProcessingRule> list = new ArrayList<ProcessingRule>();
    try {
      //wm.tn.route.list(pipeline);
      //com.wm.app.tn.route.RoutingRule rule = RoutingRuleStore.getRuleByName(ruleName);
      for (RoutingRule route : com.wm.app.tn.route.RoutingRuleStore.getList().list()) {
        //cursor.insertAfter(
        //  wm.tn.doctype.getRegistry().getType(route.getMessageType().getIData()))
        if (route.getMessageType() != null && route.getMessageType().length == 1) {
          String docName = route.getMessageTypeName()[0];
          BizDocType bizDoc = com.wm.app.tn.db.BizDocTypeStore.getByName(docName, false, false);
          if (bizDoc instanceof XMLDocType) {
            XMLDocType doc = (XMLDocType) bizDoc;
            cursor.insertAfter(route.getName(), route.getMessageTypeName());
  //          cursor.insertAfter(route.getName(), route.getServiceName());
  //          cursor.insertAfter(route.getName(), doc.getRecordBlueprint());
            ProcessingRule rule = new ProcessingRule(route.getMessageTypeName()[0], route.getServiceName(), doc.getRecordBlueprint().getFullName());
            Util.api.getDao(ProcessingRule.class).createOrUpdate(rule);
          }
        }
      }
    } catch (SQLException e) {
      throw new ServiceException(e);
    }
    return list;
  }
}
