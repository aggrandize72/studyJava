package org.example.test2;

import org.example.test2.IFormat;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class StudentFormat implements IFormat<Student> {
    private final Map<String, String> map = new HashMap<>();

    {
        map.put("s_age", "age");
        map.put("s_name", "name");
    }

    @Override
    public Student format(JSONObject jsonObject) {
        Student s = new Student();
        for (var e : map.entrySet()) {
            if ("age".equals(e.getValue())){
                s.setAge(jsonObject.optInt(e.getKey()));
            }
            if ("name".equals(e.getValue())){
                s.setName(jsonObject.optString(e.getKey()));
            }
        }
        return s;
    }
}
