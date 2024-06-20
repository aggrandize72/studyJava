package org.example.test2;

import org.example.test2.annotation.FieldJson;
import org.example.test2.annotation.TypeJson;

@TypeJson("student")
public class Student {
    @FieldJson("s_name")
    public String name;
    @FieldJson("s_age")
    public int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}