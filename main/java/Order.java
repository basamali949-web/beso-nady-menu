public class Order {
    public int id;
    public String tableName;
    public String description;
    public double price;

    public Order(int id, String tableName, String description, double price) {
        this.id = id;
        this.tableName = tableName;
        this.description = description;
        this.price = price;
    }
}