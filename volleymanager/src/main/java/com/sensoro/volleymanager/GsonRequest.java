package com.sensoro.volleymanager;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class GsonRequest<T> extends Request<T> {

    private final Listener<T> mListener;
    private Gson gson;
    private Class<T> mClass;
    private Map<String, String> mParams;    //post Params
    private TypeToken<T> mTypeToken;
    private String jsonBody;
    private Map<String, String> headers;    // headers


    private Gson createGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(double.class, new NumberDeserializer())
                .registerTypeAdapter(int.class, new NumberDeserializer())
                .registerTypeAdapter(Number.class, new NumberDeserializer());

        Gson gson = gsonBuilder.create();
        return gson;
    }

    public GsonRequest(int method, Map<String, String> params, String url, Class<T> clazz, Listener<T> listener,
                       ErrorListener errorListener) {
        this(method, null, params, url, clazz, listener, errorListener);
    }

    public GsonRequest(int method, Map<String, String> params, String url, TypeToken<T> typeToken, Listener<T> listener,
                       ErrorListener errorListener) {
        this(method, null, params, url, typeToken, listener, errorListener);
    }

    public GsonRequest(int method, String jsonBody, String url, Class<T> clazz, Listener<T> listener,
                       ErrorListener errorListener) {
        this(method, null, jsonBody, url, clazz, listener, errorListener);
    }

    public GsonRequest(int method, String jsonBody, String url, TypeToken<T> typeToken, Listener<T> listener,
                       ErrorListener errorListener) {
        this(method, null, jsonBody, url, typeToken, listener, errorListener);
    }


    public GsonRequest(int method, Map<String, String> headers, Map<String, String> params, String url, Class<T> clazz, Listener<T> listener,
                       ErrorListener errorListener) {
        super(method, url, errorListener);
        mClass = clazz;
        mListener = listener;
        this.headers = headers;
        mParams = params;
        setMyRetryPolicy();
        this.gson = createGson();
    }

    public GsonRequest(int method, Map<String, String> headers, Map<String, String> params, String url, TypeToken<T> typeToken, Listener<T> listener,
                       ErrorListener errorListener) {
        super(method, url, errorListener);
        mTypeToken = typeToken;
        mListener = listener;
        this.headers = headers;
        mParams = params;
        setMyRetryPolicy();
        this.gson = createGson();
    }

    public GsonRequest(int method, Map<String, String> headers, String jsonBody, String url, Class<T> clazz, Listener<T> listener,
                       ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mClass = clazz;
        this.mListener = listener;
        this.headers = headers;
        this.jsonBody = jsonBody;
        setMyRetryPolicy();
        this.gson = createGson();
    }

    public GsonRequest(int method, Map<String, String> headers, String jsonBody, String url, TypeToken<T> typeToken, Listener<T> listener,
                       ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mTypeToken = typeToken;
        this.mListener = listener;
        this.headers = headers;
        this.jsonBody = jsonBody;
        setMyRetryPolicy();
        this.gson = createGson();
    }


    private void setMyRetryPolicy() {
        setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    //get
    public GsonRequest(String url, Class<T> clazz, Listener<T> listener, ErrorListener errorListener) {
        this(Method.GET, (Map<String, String>) null, url, clazz, listener, errorListener);
    }

    // get
    public GsonRequest(String url, TypeToken<T> typeToken, Listener<T> listener, ErrorListener errorListener) {
        this(Method.GET, (Map<String, String>) null, url, typeToken, listener, errorListener);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        if (headers == null) {
            return super.getHeaders();
        }
        return headers;
    }

    @Override
    public String getBodyContentType() {
        return "application/json";
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        if (jsonBody == null) {
            return super.getBody();
        }

        return jsonBody.getBytes();
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return super.getParams();
    }


    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
//            if (BuildConfig.DEBUG) {
                Log.d("GsonRequest", "======>" + jsonString);
//            }
            if (mTypeToken == null) {
                T t = gson.fromJson(jsonString, mClass);
                return Response.success(t,
                        HttpHeaderParser.parseCacheHeaders(response));
            } else {
                return (Response<T>) Response.success(gson.fromJson(jsonString, mTypeToken.getType()),
                        HttpHeaderParser.parseCacheHeaders(response));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(T response) {
        mListener.onResponse(response);
    }

}