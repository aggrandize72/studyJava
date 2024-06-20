package org.example.test2;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.*;

public class Format{
    public Object obj;
    private Class<?> clazz;
    private final Map<String, Field> fieldMap = new HashMap<>();

    public Format(Class<?> clazz) {
        this.clazz = clazz;
//        fieldMap = BaseMapper.mapCache.get(clazz);
        
    }

    public Object format(Object jsb) {
        try {
            if (clazz.isPrimitive() || clazz.equals(String.class)){
                obj = jsb;
            } else {
                obj = clazz.getConstructor().newInstance();
                JSONObject jsonObject = (JSONObject) jsb;
                for (Map.Entry<String, Field> e : fieldMap.entrySet()){
                    Field f = e.getValue();
                    Format format = new Format(f.getType());
                    format.format(jsonObject.opt(e.getKey()));
                    f.set(obj, format.obj);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }
}
