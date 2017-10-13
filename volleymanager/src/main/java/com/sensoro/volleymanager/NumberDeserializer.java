package com.sensoro.volleymanager;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Created by tangrisheng on 2016/5/6.
 * number element.if "" or an exception appears , return 0;
 */
public class NumberDeserializer implements JsonDeserializer<Number> {
    @Override
    public Number deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        try {
            if (typeOfT.equals(double.class) || typeOfT.equals(Double.class)) {
                return json.getAsDouble();
            } else if (typeOfT.equals(float.class) || typeOfT.equals(Float.class)) {
                return json.getAsFloat();
            } else if (typeOfT.equals(int.class) || typeOfT.equals(Integer.class)) {
                return json.getAsInt();
            } else if (typeOfT.equals(long.class) || typeOfT.equals(Long.class)) {
                return json.getAsLong();
            } else if (typeOfT.equals(short.class) || typeOfT.equals(Short.class)) {
                return json.getAsShort();
            } else {
                return null;
            }
        } catch (Exception e) {
            return 0;
        }
    }
}
