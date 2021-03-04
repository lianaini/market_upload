package org.jeme;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class Upload {
    public static JsonObject post(String serverUrl, Map<String,Object> params,Map<String,Object> header) throws IOException {
        if(Utils.isEmpty(serverUrl)) {
            return null;
        }
        OkHttpClient client = new OkHttpClient();
        MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        if(params != null) {
            for (Map.Entry<String, Object> entity : params.entrySet()) {
                if (entity.getValue() instanceof File) {
                    File file = (File) entity.getValue();
                    requestBodyBuilder.addFormDataPart(entity.getKey(), file.getName()
                            , RequestBody.create(MediaType.parse("application/octet-stream")
                                    , file));
                } else {
                    requestBodyBuilder.addFormDataPart(entity.getKey(), entity.getValue().toString());
                }
            }
        }else {
            requestBodyBuilder.addFormDataPart("empty","");
        }

        Request.Builder request = new Request.Builder()
                .url(serverUrl)
                .post(requestBodyBuilder.build());


        if(header != null && header.size() != 0) {
            for(Map.Entry<String,Object> entry : header.entrySet()) {
                request.addHeader(entry.getKey(),entry.getValue().toString());
            }
        }

        Call call = client.newCall(request.build());
        Response response = call.execute();
        ResponseBody body = response.body();
        if (body == null) {
            return null;
        } else {
            String b = new String(body.bytes());
            System.out.println("返回结果:" +b);
            if(Utils.isEmpty(b)){
                return null;
            }

            return JsonParser.parseString(b).getAsJsonObject();
        }
    }
    public static JsonObject postBodyJson(String serverUrl, Map<String,Object> params) throws IOException {
        if(Utils.isEmpty(serverUrl)) {
            return null;
        }
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create( MediaType.parse("application/json"),new Gson().toJson(params));

        Request request = new Request.Builder()
                .url(serverUrl)
                .post(requestBody)
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();
        ResponseBody body = response.body();
        if (body == null) {
            return null;
        } else {
            String b = new String(body.bytes());
            System.out.println("返回结果:" +b);
            if(Utils.isEmpty(b)){
                return null;
            }

            return JsonParser.parseString(b).getAsJsonObject();
        }
    }

    public static JsonObject get(String serverUrl, Map<String,Object> params,Map<String,Object> header) throws IOException {
        if(Utils.isEmpty(serverUrl)) {
            return null;
        }
        StringBuffer sb = new StringBuffer(serverUrl);
        if(params != null && params.size() != 0) {
            int pos = 0;
            for(Map.Entry<String,Object> entry : params.entrySet()) {
                if(pos == 0) {
                    sb.append("?");
                }else {
                    sb.append("&");
                }
                sb.append(entry.getKey()).append("=").append(entry.getValue());
                pos++;
            }
        }

        OkHttpClient client = new OkHttpClient();

        Request.Builder request = new Request.Builder()
                .url(sb.toString());
        if(header != null && header.size() != 0) {
            for(Map.Entry<String,Object> entry : header.entrySet()) {
                request.addHeader(entry.getKey(),entry.getValue().toString());
            }
        }


        Call call = client.newCall(request.build());
        Response response = call.execute();
        ResponseBody body = response.body();
        if (body == null) {
            return null;
        } else {
            String b = new String(body.bytes());
            System.out.println("返回结果:" +b);
            if(Utils.isEmpty(b)){
                return null;
            }

            return JsonParser.parseString(b).getAsJsonObject();
        }
    }

    public static JsonObject put(String serverUrl, Map<String,Object> params,Map<String,Object> header) throws IOException {
        if(Utils.isEmpty(serverUrl)) {
            return null;
        }
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create( MediaType.parse("application/json"),new Gson().toJson(params));

        Request.Builder request = new Request.Builder()
                .url(serverUrl)
                .put(requestBody);

        if(header != null && header.size() != 0) {
            for(Map.Entry<String,Object> entry : header.entrySet()) {
                request.addHeader(entry.getKey(),entry.getValue().toString());
            }
        }

        Call call = client.newCall(request.build());
        Response response = call.execute();
        ResponseBody body = response.body();
        if (body == null) {
            return null;
        } else {
            String b = new String(body.bytes());
            System.out.println("返回结果:" +b);
            if(Utils.isEmpty(b)){
                return null;
            }

            return JsonParser.parseString(b).getAsJsonObject();
        }
    }
}
