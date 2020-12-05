package centralworks.layouts.settings;

import lombok.Data;

@Data
public abstract class InventorySettings {

    private boolean toggle;
    private String name;
    private int rows;

    public String getName() {
        return name.replace("&", "§");
    }
}
