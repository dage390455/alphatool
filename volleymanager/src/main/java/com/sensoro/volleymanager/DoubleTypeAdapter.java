package com.sensoro.volleymanager;

import android.util.Log;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * Created by tangrisheng on 2016/5/6.
 * used for json number exception.
 */
public class DoubleTypeAdapter extends TypeAdapter<Double> {

    private static final String TAG = DoubleTypeAdapter.class.getSimpleName();


    @Override
    public void write(JsonWriter out, Double value) throws IOException {
        Log.i(TAG, "write");
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value);
        }
    }

    @Override
    public Double read(JsonReader in) throws IOException {
        Log.i(TAG, "read");
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String tmpString = in.nextString();
        try {
            Double value = Double.valueOf(tmpString);
            return value;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
