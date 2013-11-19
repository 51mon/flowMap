package flowMap;

import com.wm.app.b2b.server.*;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.net.HttpHeader;
import com.wm.net.HttpInputStream;
import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.filter.CoffeeScriptFilter;
import de.neuland.jade4j.template.FileTemplateLoader;
import de.neuland.jade4j.template.JadeTemplate;
import flowMap.server.Util;
import flowMap.view.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Handler implements HTTPHandler {
  public String templateDir;
  public Map<String,JadeTemplate> templates;
  public JadeConfiguration config;
  //JdbcPooledConnectionSource connectionSource;
  public API api;
  Rest rest;
  public Connection conn;
  long maxRows = 25;

  Pattern restPattern;
  public String svcPath = "svc";
  Pattern servicePattern = Pattern.compile("/"+svcPath+"(?:/([^/]+)/([^/]+))?");
  Pattern staticPattern = Pattern.compile("/static(?:/(.+))?");
  public Map<String,Class> tablesToClasses = new HashMap<String,Class>();
  Map<Class,String> classesToTables = new HashMap<Class,String>();
  Map<Class,Map<String,String>> foreignColNames = new HashMap<Class, Map<String,String>>();
  Map<Class,Map<String,String>> foreignTableNames = new HashMap<Class, Map<String,String>>();

  public Handler(API api, String templateDir) {
    this.api = api;
    this.templateDir = templateDir;
    templates = new java.util.HashMap<String,JadeTemplate>();
    config = new JadeConfiguration();
    config.setFilter("coffeescript", new CoffeeScriptFilter());
    config.setTemplateLoader(new FileTemplateLoader(templateDir, "UTF-8"));

    rest = new Rest(api);
    try {
      conn = DriverManager.getConnection(api.dbURL, api.dbuser, api.dbpass);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    for (Class modelClass : API.models) {
      tablesToClasses.put(API.tableName(modelClass), modelClass);
    }
    restPattern = Pattern.compile("^/(?:(jdbc|orm|rest|)/)?("+StringUtils.join(tablesToClasses.keySet(), "|")+")(/(\\d+)(?:/(edit))?|new)?");
  }
  static {
    //System.out.println("flowMap initJadeTemplates "+new java.util.Date());
  /*File[] view = new File().listFiles();
  for (File view : view) {
    templates.put(view.getName(), config.getTemplate(view));
  }*/
  }

  public String render(String templateName, Map<String, Object> model) {
    String result = null;
    try  {
      JadeTemplate template = config.getTemplate(templateName);
      result = config.renderTemplate(template, model);
    } catch (Exception e) { // JadeCompilerException, IOException
      throw new RuntimeException(e);
    }
    return result;
  }

  static Pattern qsRe = Pattern.compile("(.*)(?:\\[(.*)\\])(.*)");
  public static void match(Map<String, Object> attributes, String name, String value) {
    //Map<String, Object> List<String>
    Matcher matcher = qsRe.matcher(name);
    if (!matcher.matches()) {
      attributes.put(name, value);
    } else {
      name = matcher.group(1);
      String subName = matcher.group(2);
      String rest = matcher.group(3);
        if (rest.equals("")) {
          if (attributes.get(name) == null)
            attributes.put(name, new HashMap<String,String>());
          ((Map<String,String>) attributes.get(name)).put(subName, value);
        } else {
          if (attributes.get(name) == null)
            attributes.put(name, new HashMap<String,Object>());
          match((Map<String,Object>) attributes.get(name), subName, value);
        }
    }
  }


  public Map<String,Object> parseQs(String qs) {
    Map <String,Object> attributes = new HashMap<String,Object>();
    if (qs != null) {
      try {
        qs = URLDecoder.decode(qs, "UTF-8");
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException(e);
      }
      String[] splitQs = qs.split("&");
      for (String attr : splitQs) {
          String[] splitAttr = attr.split("=");
          if (splitAttr.length == 2) {
            //String name = splitAttr[0];
            match(attributes, splitAttr[0], splitAttr[1]);
          }
      }
    }
    return attributes;
  }
  class BytesWrapper {
    byte[] bytes;
    public BytesWrapper(byte[] bytes) { this.bytes = bytes; }
  }
  Map<String,BytesWrapper> staticResources = new HashMap<String,BytesWrapper>();
  public Map<String, Object> getModel() {
    Map<String, Object> model = new HashMap<String, Object>();
    model.put("urlRoot", Util.url);
    model.put("svcPath", svcPath);
    SortedSet<String> tables = new TreeSet<String>(tablesToClasses.keySet());
    model.put("tables", tables);
    return model;
  }
  @Override
  public boolean process(ProtocolState state) throws IOException, AccessException {
    try {
      int code = 200;
      state.setResponse(code, HTTPServerUtil.getRecomendedReason(code + ""));
      String originalUrl = URLDecoder.decode(state.getHttpRequestUrl(), "UTF-8");
      String url = originalUrl.replaceFirst(Util.url, "");
      Map <String,Object> attributes = parseQs(state.getHttpRequestUrlQuery());
      String contentType = state.getContentType();
      String accept = state.getRequestFieldValue("Accept");
      Util.log("Process URL "+url+" contentType "+contentType+" Accept "+accept);

      // Ready body data
      HttpInputStream bodyStream = state.getInStream();
      //StringWriter writer = new StringWriter(); // doesn't work, wait forever
      //IOUtils.copy(bodyStream, writer);
      int READ_BUFFER_SIZE = 4000;
      //BufferedOutputStream bos = new BufferedOutputStream(state.getResponse().getOutputStream(), READ_BUFFER_SIZE);
      ByteBuffer bb = ByteBuffer.allocate(READ_BUFFER_SIZE);
      byte[] data = new byte[READ_BUFFER_SIZE];
      //int count = 0;
      while (bodyStream.hasMoreData()) {
        //Util.log("Read body data from request");
        if (bodyStream.read(data, 0, READ_BUFFER_SIZE) < 0)
          break;
        bb.put(data);
        //bos.write(data, 0, bytesRead);
        //count += bytesRead;
      }
      //String body = bos.toString();
      String body = bb.toString();

      //String body = writer.toString();
      Matcher restMatcher = restPattern.matcher(url);
      Matcher serviceMatcher = servicePattern.matcher(url);
      Matcher staticMatcher = staticPattern.matcher(url);
      if (restMatcher.matches()) {
        String kind = restMatcher.group(1); // jdbc orm rest
        String tableName = restMatcher.group(2);
        Router router;
        if (kind != null && kind.equals("orm")) {
          router = new OrmRouter(this, originalUrl, url, attributes, contentType, accept, body,
              tableName);
        } else {
          router = new JdbcRouter(this, originalUrl, url, attributes, contentType, accept, body,
              tableName);
        }
        //String className = modelPrefix+tablesToClasses.get(tableName);
        //Util.log("flowMap tableName " + tableName);
        boolean isNew = restMatcher.group(3) != null && "/new".equals(restMatcher.group(3));
        String id = restMatcher.group(4);
        boolean isEdit = restMatcher.group(5) != null;
        //String qs = restMatcher.group(5);

        // We need the DSL to have the type of the fields for the queries

        if (isNew) {
          throw new RuntimeException("To implement");
        } else if (isEdit) {
          throw new RuntimeException("To implement");
        } else if (id != null) { // show or save
          switch (state.getRequestType()) {
            case HttpHeader.GET : // show
              String html = router.show(id);
              state.write(html.getBytes("UTF-8"));
              return true;
            case HttpHeader.POST : // save
              throw new RuntimeException("To implement");
            default:
              throw new RuntimeException("Not managed");
          }
        } else { // list
          String html = router.list();
          state.write(html.getBytes("UTF-8"));
          return true;
        }
      } else if (url.matches("\\/?")) {
        Map<String, Object> model = getModel();
        model.put("body", "<h1>Welcome</h1>");
        String html = render("layout.jade", model);
        state.write(html.getBytes("UTF-8"));

      } else if (serviceMatcher.matches()) {
        //Util.log("serviceMatcher "+serviceMatcher.group(1)+" "+serviceMatcher.group(2));
        Map<String, Object> model = getModel();
        model.put("services", Services.services);
        model.put("mainClass", "box");
        if (serviceMatcher.group(1) != null) {
          String className = serviceMatcher.group(1);
          String methodName = serviceMatcher.group(2);
          //String qs = serviceMatcher.group(3);
          IData idata = IDataFactory.create();
          for (Map.Entry<String,Object> entry : attributes.entrySet()) {
            idata.getCursor().insertAfter(entry.getKey(), entry.getValue());
          }
          Method method = Class.forName(className).getMethod(methodName, IDataCursor.class);
          method.invoke(null, idata.getCursor());
          model.put("results", parseIData(idata.getCursor()));
        }
        model.put("body", render("import.jade", model));
        state.write(render("layout.jade", model).getBytes("UTF-8"));

      } else if (staticMatcher.matches()) {
        String filename = staticMatcher.group(1);
        if (filename == null) {
          //Util.log("/static");
          StringBuilder sb = new StringBuilder("<ul>");
          File folder = new File(Util.packageName+"/static");
          if (folder.exists())
            for (String file : folder.list())
              sb.append("<li>"+file+"</li>");
          else
            sb.append("<li>No resource found</li>");

//          Enumeration<URL> ress = api.getClass().getClassLoader().getResources("flowMap/static");
//          if (ress.hasMoreElements()) {
//            File folder = new File(ress.nextElement().toURI());
//            for (String file : folder.list())
//              sb.append("<li>"+file+"</li>");
//          } else {
//            sb.append("<li>No resource found</li>");
//          }
          Map<String, Object> model = getModel();
          model.put("body", sb.append("</ul>").toString());
          state.write(render("layout.jade", model).getBytes("UTF-8"));
          return true;
        }
        if (staticResources.get(filename) == null) {
          byte[] bytes = FileUtils.readFileToByteArray(new File(Util.packageName+"/static/"+filename));
//          Util.log("Get static resource "+"flowMap/static/"+filename);
//          byte[] bytes = IOUtils.toByteArray(
//              api.getClass().getClassLoader().getResourceAsStream("flowMap/static/"+filename));
          staticResources.put(filename, new BytesWrapper(bytes));
        }
        state.write(staticResources.get(filename).bytes);

      } else if ("/test".equals(url)) {
        Map<String, Object> model = new HashMap<String, Object>();
        List<Book> books = new ArrayList<Book>();
        books.add(new Book("The Hitchhiker's Guide to the Galaxy", 5.70, true));
        books.add(new Book("Life, the Universe and Everything", 5.60, false));
        books.add(new Book("The Restaurant at the End of the Universe", 5.40, true));
        model.put("books", books);
        model.put("pageName", "My Bookshelf");
        Util.log("test model: " + model.toString());
        String html = render("example.jade", model);
        state.write(html.getBytes("UTF-8"));

      } else if ("application/json".equals(contentType)) {
        //String method = HttpHeader.reqStrType[state.getRequestType()];
        state.getResponse().setContentType("application/json");
        rest.render(state.getRequestType(), url, body, state.getResponse().getOutputStream());
      } else {
        state.write((url+" not found").getBytes("UTF-8"));
      }
      //InputStream stream = new ByteArrayInputStream(html.getBytes("UTF-8"));
      //state.getResponse().setOutputByStream(stream, html.length());
      //state.getResponse().send();
      //com.wm.app.b2b.server.StateManager.terminate();
      return true;
    } catch (Exception exception) {
//      String html2 = "<html><head><title>An error was encountered</title></head><body>"+
//          "<h2>"+e.getClass().getName()+" was throwed</h2>"+
//          "<h3>Message: "+e.getMessage()+"</h3>"+
//          "<table>";
//      for (StackTraceElement el : e.getStackTrace())
//        html2 += "<tr><td></td><td>"+el.toString()+"</td></tr>";
//      html2 += "<tr><td>Cause</td><td>"+e.getCause()+"</td></tr></table>"+
//          "</body></html>";
      Map<String, Object> model = getModel();
      model.remove("body");
      model.put("moreCSS", "#main { padding: 0.5em; }");
      model.put("mainClass", "box");
      Throwable t = exception;
      List<Throwable> throwables = new ArrayList<Throwable>();
      while (t.getCause() != null) {
        t = t.getCause();
        throwables.add(t);
      }
      model.put("cause", t);
      model.put("throwables", throwables);
      model.put("body", render("error.jade", model));
      String html = render("layout.jade", model);
      state.write(html.getBytes("UTF-8"));
      throw new RuntimeException(exception);
    }
  }
  public String parseIData(IDataCursor cursor) {
    StringBuilder sb = new StringBuilder();
    sb.append("<table cellspacing='0'>");
    parseIData(cursor, sb, "");
    return sb.append("</table>").toString();
  }
  void parseIDataHelper(StringBuilder sb, String gif, String path, Object value) {
    sb.append("<tr><td align='right' style='font-style: italic;'>").append(path)
        .append("</td><td align='center' style='padding: 0; padding-left: 0.5em;'><img src='/"+Util.url+"/static/"+gif+"'></td><td>").append(value).append("</td></tr>");
  }
  void parseIData(IDataCursor cursor, StringBuilder sb, String _path) {
    cursor.first();
    while (cursor.hasMoreData()) {
      String path = _path.equals("") ? cursor.getKey() : _path+"/"+cursor.getKey();
      if (cursor.getValue() instanceof IData)
        parseIData(((IData)cursor.getValue()).getCursor(), sb, path);
      else if (cursor.getValue() instanceof String)
        parseIDataHelper(sb, "field_string.gif", path, cursor.getValue());
      else if (cursor.getValue() instanceof Integer)
        parseIDataHelper(sb, "obj_integer.gif", path, cursor.getValue());
      else if (cursor.getValue() instanceof Boolean)
        parseIDataHelper(sb, "obj_boolean.gif", path, cursor.getValue());
      else if (cursor.getValue() instanceof String[])
        parseIDataHelper(sb, "field_stringlist.gif", path, StringUtils.join(cursor.getValue(), "<br>"));
      else if (cursor.getValue() instanceof Integer[])
        parseIDataHelper(sb, "obj_list_integer.gif", path, StringUtils.join(cursor.getValue(), "<br>"));
      else if (cursor.getValue() instanceof Boolean[])
        parseIDataHelper(sb, "obj_list_boolean.gif", path, StringUtils.join(cursor.getValue(), "<br>"));
      else
        parseIDataHelper(sb, "field_object.gif", path, cursor.getValue());
      cursor.next();
    }
  }
}
