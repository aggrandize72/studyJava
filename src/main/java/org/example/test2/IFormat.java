package org.example.test2;

import org.json.JSONObject;

public interface IFormat<T> {
    T format(JSONObject jsonObject);
}
