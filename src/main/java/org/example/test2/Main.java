package org.example.test2;

import org.json.JSONObject;

public class Main {
    public static void main(String[] args) {
        JSONObject json = new JSONObject();
        JSONObject st = new JSONObject();
        st.put("s_name", "caoxing");
        st.put("s_age", 10);
        json.put("student", st);
        Base b = FormatManager.format(json, Base.class);
        System.out.println(b);
        System.out.println(b.student);
        System.out.println(b.student.age);
        System.out.println(b.student.name);
    }
}
