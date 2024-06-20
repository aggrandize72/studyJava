package org.example.test2;

import org.example.test2.IFormat;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class FormatManager {
    private static final Map<Type, IFormat<?>> map = new HashMap<>();
    static {
        map.put(Student.class, new StudentFormat());
        map.put(Base.class, new BaseFormat());
    }

    public static <T>T format(String str, Class<T> type){
        return format(new JSONObject(str), type);
    }

    public static <T>T format(JSONObject jsonObject, Class<T> type){
        return (T) map.get(type).format(jsonObject);
    }
}
