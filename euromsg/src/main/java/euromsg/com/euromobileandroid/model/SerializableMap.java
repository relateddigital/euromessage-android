package euromsg.com.euromobileandroid.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SerializableMap implements Serializable {
    private static final long serialVersionUID = 1L;
    private Map<String, Object> map;

    public SerializableMap() {
        map = new HashMap<>();
    }

    public void put(String key, Object value) {
        map.put(key, value);
    }

    public Map<String, Object> getMap() {
        return map;
    }
}