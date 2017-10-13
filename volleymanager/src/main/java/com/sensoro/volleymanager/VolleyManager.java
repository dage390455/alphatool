package com.sensoro.volleymanager;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.reflect.TypeToken;

import java.util.Map;

import okhttp3.OkHttpClient;


public class VolleyManager {
    private volatile static VolleyManager mVolleyManager = null;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;


    /**
     * @param context
     */
    private VolleyManager(Context context) {

        mRequestQueue = Volley.newRequestQueue(context, new OkHttp3Stack(new OkHttpClient()));

        mImageLoader = new ImageLoader(mRequestQueue,
                new LruBitmapCache(context));
    }

    /**
     * singleton VolleyManager
     *
     * @return VolleyManager instance
     */
    public static VolleyManager getInstance(Context context) {
        if (mVolleyManager == null) {
            synchronized (VolleyManager.class) {
                if (mVolleyManager == null) {
                    mVolleyManager = new VolleyManager(context);
                }
            }
        }

        return mVolleyManager;
    }

    private <T> Request<T> add(Request<T> request) {
        return mRequestQueue.add(request);//添加请求到队列
    }

    /**
     * string request with http method.
     *
     * @param tag           request tag.
     * @param url           url
     * @param listener      result listener.
     * @param errorListener errorListener
     * @return the request.
     */
    public StringRequest strRequest(Object tag, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        StringRequest request = new StringRequest(url, listener, errorListener);
        request.setTag(tag);
        add(request);
        return request;
    }

    /**
     * string request with http method.
     *
     * @param tag           request tag.
     * @param method        http method.
     * @param url           url
     * @param listener      result listener.
     * @param errorListener errorListener
     * @return the request.
     */
    public StringRequest strRequest(Object tag, int method, String url, Response.Listener<String> listener,
                                    Response.ErrorListener errorListener) {
        StringRequest request = new StringRequest(method, url, listener, errorListener);
        request.setTag(tag);
        add(request);
        return request;
    }

    /**
     * imageRequest
     *
     * @param tag           request tag.
     * @param url           url
     * @param listener      result listener
     * @param maxWidth      image max width
     * @param maxHeight     image max height
     * @param scaleType     the ImageViews ScaleType used to calculate the needed image size
     * @param decodeConfig  Format to decode the bitmap to
     * @param errorListener errorListener
     * @return the request
     */
    public ImageRequest imageRequest(Object tag, String url, Response.Listener<Bitmap> listener,
                                     int maxWidth, int maxHeight, ImageView.ScaleType scaleType,
                                     Bitmap.Config decodeConfig, Response.ErrorListener errorListener) {
        ImageRequest request = new ImageRequest(url, listener, maxWidth, maxHeight, scaleType,
                decodeConfig, errorListener);
        request.setTag(tag);
        add(request);
        return request;
    }

    /**
     * ImageLoader with default image size.
     *
     * @param imageView         imageView
     * @param imgViewUrl        imgViewUrl
     * @param defaultImageResId defaultImageResId
     * @param errorImageResId   errorImageResId
     */
    public void imageLoaderRequest(ImageView imageView, String imgViewUrl, int defaultImageResId,
                                   int errorImageResId) {
        ImageLoader.ImageListener listener = ImageLoader.getImageListener(imageView, defaultImageResId,
                errorImageResId);
        mImageLoader.get(imgViewUrl, listener);
    }


    /**
     * ImageLoader with image size.
     *
     * @param imageView         imageView
     * @param imgViewUrl        imgViewUrl
     * @param defaultImageResId defaultImageResId
     * @param errorImageResId   errorImageResId
     * @param maxWidth          maxWidth
     * @param maxHeight         maxHeight
     */
    public void imageLoaderRequest(ImageView imageView, String imgViewUrl, int defaultImageResId,
                                   int errorImageResId, int maxWidth, int maxHeight) {
        ImageLoader.ImageListener listener = ImageLoader.getImageListener(imageView, defaultImageResId,
                errorImageResId);
        mImageLoader.get(imgViewUrl, listener, maxWidth, maxHeight);
    }

    /**
     * Http Get with Template class.
     *
     * @param <T>           template
     * @param tag           request tag
     * @param url           url
     * @param clazz         template class.
     * @param listener      result listener.
     * @param errorListener errorListener
     * @return the request
     */
    public <T> GsonRequest<T> gsonGetRequest(Object tag, String url, Class<T> clazz, Response.Listener<T> listener,
                                             Response.ErrorListener errorListener) {
        GsonRequest<T> request = new GsonRequest<T>(url, clazz, listener, errorListener);
        request.setTag(tag);
        add(request);
        return request;
    }


    /**
     * Http with params.
     *
     * @param tag           request tag
     * @param params        map params
     * @param url           url
     * @param clazz         template class.
     * @param listener      result listener.
     * @param errorListener errorListener
     * @param <T>           template
     * @return the request
     */
    public <T> GsonRequest<T> gsonRequest(Object tag, int method, Map<String, String> params, String url,
                                          Class<T> clazz, Response.Listener<T> listener,
                                          Response.ErrorListener errorListener) {
        GsonRequest<T> request = new GsonRequest<T>(method, params, url, clazz, listener, errorListener);
        if (tag != null) {
            request.setTag(tag);
        }
        add(request);
        return request;
    }

    /**
     * Http request by gson.
     *
     * @param tag           request tag
     * @param method        http method
     * @param json          json body
     * @param url           url
     * @param clazz         template class.
     * @param listener      result listener.
     * @param errorListener errorListener
     * @param <T>           template
     * @return the request
     */
    public <T> GsonRequest<T> gsonRequest(Object tag, int method, String json, String url,
                                          Class<T> clazz, Response.Listener<T> listener,
                                          Response.ErrorListener errorListener) {
        GsonRequest<T> request = new GsonRequest<T>(method, json, url, clazz, listener, errorListener);
        if (tag != null) {
            request.setTag(tag);
        }
        add(request);
        return request;
    }

    /**
     * Http Post with map.
     *
     * @param tag           request tag
     * @param params        map params
     * @param url           url
     * @param typeToken     template class.
     * @param listener      result listener.
     * @param errorListener errorListener
     * @param <T>           template
     * @return the request
     */
    public <T> GsonRequest<T> gsonRequest(Object tag, int method, Map<String, String> params, String url,
                                          TypeToken<T> typeToken, Response.Listener<T> listener,
                                          Response.ErrorListener errorListener) {
        GsonRequest<T> request = new GsonRequest<T>(method, params, url, typeToken, listener, errorListener);
        if (tag != null) {
            request.setTag(tag);
        }
        add(request);
        return request;
    }

    /**
     * Http request by gson.
     *
     * @param tag           request tag
     * @param method        http method
     * @param json          json body
     * @param url           url
     * @param typeToken     template class.
     * @param listener      result listener.
     * @param errorListener errorListener
     * @param <T>           template
     * @return the request
     */
    public <T> GsonRequest<T> gsonRequest(Object tag, int method, String json, String url,
                                          TypeToken<T> typeToken, Response.Listener<T> listener,
                                          Response.ErrorListener errorListener) {
        GsonRequest<T> request = new GsonRequest<T>(method, json, url, typeToken, listener, errorListener);
        if (tag != null) {
            request.setTag(tag);
        }
        add(request);
        return request;
    }

    /**
     * Http with params.
     *
     * @param tag           request tag
     * @param method        http method
     * @param headers       http headers
     * @param params        map params
     * @param url           url
     * @param clazz         template class.
     * @param listener      result listener.
     * @param errorListener errorListener
     * @param <T>           template
     * @return the request
     */
    public <T> GsonRequest<T> gsonRequest(Object tag, int method, Map<String, String> headers, Map<String, String> params, String url,
                                          Class<T> clazz, Response.Listener<T> listener,
                                          Response.ErrorListener errorListener) {
        GsonRequest<T> request = new GsonRequest<T>(method, headers, params, url, clazz, listener, errorListener);
        if (tag != null) {
            request.setTag(tag);
        }
        add(request);
        return request;
    }

    /**
     * Http request by gson.
     *
     * @param tag           request tag
     * @param method        http method
     * @param headers       http headers
     * @param json          json body
     * @param url           url
     * @param clazz         template class.
     * @param listener      result listener.
     * @param errorListener errorListener
     * @param <T>           template
     * @return the request
     */
    public <T> GsonRequest<T> gsonRequest(Object tag, int method, Map<String, String> headers, String json, String url,
                                          Class<T> clazz, Response.Listener<T> listener,
                                          Response.ErrorListener errorListener) {
        headers.put("User-Agent", "Android/"+android.os.Build.VERSION.RELEASE);
        GsonRequest<T> request = new GsonRequest<T>(method, headers, json, url, clazz, listener, errorListener);
        if (tag != null) {
            request.setTag(tag);
        }
        add(request);
        return request;
    }

    /**
     * Http Post with map.
     *
     * @param tag           request tag
     * @param method        http method
     * @param headers       http headers
     * @param params        map params
     * @param url           url
     * @param typeToken     template class.
     * @param listener      result listener.
     * @param errorListener errorListener
     * @param <T>           template
     * @return the request
     */
    public <T> GsonRequest<T> gsonRequest(Object tag, int method, Map<String, String> headers, Map<String, String> params, String url,
                                          TypeToken<T> typeToken, Response.Listener<T> listener,
                                          Response.ErrorListener errorListener) {
        GsonRequest<T> request = new GsonRequest<T>(method, headers, params, url, typeToken, listener, errorListener);
        if (tag != null) {
            request.setTag(tag);
        }
        add(request);
        return request;
    }

    /**
     * Http request by gson.
     *
     * @param tag           request tag
     * @param method        http method
     * @param headers       http headers
     * @param json          json body
     * @param url           url
     * @param typeToken     template class.
     * @param listener      result listener.
     * @param errorListener errorListener
     * @param <T>           template
     * @return the request
     */
    public <T> GsonRequest<T> gsonRequest(Object tag, int method, Map<String, String> headers, String json, String url,
                                          TypeToken<T> typeToken, Response.Listener<T> listener,
                                          Response.ErrorListener errorListener) {
        GsonRequest<T> request = new GsonRequest<T>(method, headers, json, url, typeToken, listener, errorListener);
        if (tag != null) {
            request.setTag(tag);
        }
        add(request);
        return request;
    }

    /**
     * cancel request with tag.
     *
     * @param tag tag of the request.
     */
    public void cancel(Object tag) {
        mRequestQueue.cancelAll(tag);
    }

    public void stop() {
        mRequestQueue.stop();
    }
}
