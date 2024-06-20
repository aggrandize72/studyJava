package org.example.test2;

import org.example.test2.annotation.FieldJson;
import org.example.test2.annotation.TypeJson;

@TypeJson("Base")
public class Base {
    @FieldJson("student")
    public Student student;

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }
}