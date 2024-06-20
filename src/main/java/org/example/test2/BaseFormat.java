package org.example.test2;

import org.example.test2.IFormat;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class BaseFormat implements IFormat<Base> {
    private final Map<String, String> map = new HashMap<>();

    {
        map.put("student", "student");
    }

    @Override
    public Base format(JSONObject jsonObject) {
        Base s = new Base();
        for (Map.Entry<String, String> e : map.entrySet()) {
            if ("student".equals(e.getValue())){
                s.setStudent(FormatManager.format(jsonObject.optJSONObject(e.getKey()), Student.class));
            }
        }
        return s;
    }
}
