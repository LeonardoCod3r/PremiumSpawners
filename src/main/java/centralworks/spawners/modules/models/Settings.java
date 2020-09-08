package centralworks.spawners.modules.models;

import lombok.Data;

@Data
public class Settings{

    private static Settings me;

    public static Settings get() {
        return me == null ? me = new Settings() : me;
    }

    private String NBT_TAG_SPAWNER_AMOUNT;
    private String NBT_TAG_ENTITY_TYPE;
    private String NBT_TAG_ENTITY_AMOUNT;
    private String NBT_TAG_BOOSTER_TYPE;
    private String NBT_TAG_BOOSTER_VALUE;
    private String NBT_TAG_BOOSTER_DELAY;

    public Settings() {
        this.NBT_TAG_SPAWNER_AMOUNT = "spawners_amount_spawners";
        this.NBT_TAG_ENTITY_TYPE = "spawners_entity_type";
        this.NBT_TAG_ENTITY_AMOUNT = "entities_entity_amount";
        this.NBT_TAG_BOOSTER_TYPE = "booster_type";
        this.NBT_TAG_BOOSTER_VALUE = "booster_value";
        this.NBT_TAG_BOOSTER_DELAY = "booster_delay";
    }

}
