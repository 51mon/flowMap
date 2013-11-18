package flowMap.view;

public class Book {
  String name; double price; boolean isAvailable;
  public Book(String name, double price, boolean isAvailable) {
    this.name = name; this.price = price; this.isAvailable = isAvailable;
  }

  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public double getPrice() {
    return price;
  }
  public void setPrice(double price) {
    this.price = price;
  }
  public boolean isAvailable() {
    return isAvailable;
  }
  public void setAvailable(boolean available) {
    isAvailable = available;
  }
}
