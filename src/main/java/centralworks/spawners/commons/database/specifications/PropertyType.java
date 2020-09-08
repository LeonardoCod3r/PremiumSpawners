package centralworks.spawners.commons.database.specifications;

public enum PropertyType {

    KEY_NAME("key_name"),
    KEY_DATATYPE("key_datatype"),
    KEY_AUTOINCREMENT("key_autoincrement"),
    TABLE_NAME("table_name");

    private final String name;

    PropertyType(String name) {
        this.name = name;
    }

    public String getId() {
        return name;
    }
}
