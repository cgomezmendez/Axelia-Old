package us.axelia.axelia;

/**
 * Created by mac on 2/11/14.
 */
public enum AudioType {
    TRAFFIC(0),
    COMMERCIAL(1);
    private final int key;

    AudioType(int key) {
        this.key = key;
    }

    public static AudioType fromKey(int key) {
        for (AudioType type : AudioType.values()) {
            if (type.key == key) {
                return type;
            }
        }
        return null;
    }

    public int getKey() {
        return this.key;
    }
}
