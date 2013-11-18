package flowMap.view;

import de.neuland.jade4j.Jade4J;
import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.filter.CoffeeScriptFilter;
import de.neuland.jade4j.template.JadeTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TestJade4J {
  public static void main(String[] args) {
    try {
      TestJade4J testJade4J = new TestJade4J();
      //System.out.println(testJade4J.generate());
      //System.out.println(testJade4J.testForLoop());
      System.out.println(testJade4J.testWhile());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  String path = "src/flowMap/view";
  JadeConfiguration config;
  public TestJade4J() {
    config = new JadeConfiguration();
    config.setFilter("coffeescript", new CoffeeScriptFilter());
  }
  public String generate() throws java.io.IOException {
    List<Book> books = new ArrayList<Book>();
    books.add(new Book("The Hitchhiker's Guide to the Galaxy", 5.70, true));
    books.add(new Book("Life, the Universe and Everything", 5.60, false));
    books.add(new Book("The Restaurant at the End of the Universe", 5.40, true));

    HashMap<String, Object> model = new HashMap<String, Object>();
    model.put("books", books);
    model.put("pageName", "My Bookshelf");
    JadeTemplate template = config.getTemplate(path+"/test.jade");
    return config.renderTemplate(template, model);
  }
  public String testForLoop() throws java.io.IOException {
    HashMap<String, Object> model = new HashMap<String, Object>();
    HashMap<String, String> map = new HashMap<String, String>();
    map.put("one", "1");
    map.put("two", "2");
    model.put("map", map);
    JadeTemplate template = config.getTemplate(path+"/testForLoop.jade");
    return config.renderTemplate(template, model);
  }
  public String testWhile() throws java.io.IOException {
    HashMap<String, Object> model = new HashMap<String, Object>();
    HashMap<String, String> map = new HashMap<String, String>();
    map.put("one", "1");
    map.put("two", "2");
    model.put("map", map);
    JadeTemplate template = config.getTemplate(path+"/testWhile.jade");
    return config.renderTemplate(template, model);
  }
}
