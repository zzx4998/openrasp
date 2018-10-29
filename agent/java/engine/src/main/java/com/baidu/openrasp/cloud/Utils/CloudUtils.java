/*
 * Copyright 2017-2018 Baidu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.baidu.openrasp.cloud.Utils;

import com.baidu.openrasp.cloud.CloudManager;
import com.baidu.openrasp.cloud.model.CloudCacheModel;
import com.baidu.openrasp.cloud.model.GenericResponse;
import com.baidu.openrasp.config.Config;
import com.baidu.openrasp.tool.OSUtil;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @description: 云控工具类
 * @author: anyang
 * @create: 2018/09/17 17:40
 */
public class CloudUtils {
    public static String convertInputStreamToJsonString(InputStream inputStream) {
        String jsonString = null;
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] data = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(data)) != -1) {
                outputStream.write(data, 0, len);
            }
            jsonString = new String(outputStream.toByteArray(), "UTF-8");
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonString;
    }

    public static Gson getResponseGsonObject() {
        return new GsonBuilder().registerTypeAdapter(
                new TypeToken<GenericResponse>() {
                }.getType(),
                new JsonDeserializer<GenericResponse>() {
                    public GenericResponse deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                        GenericResponse response = new GenericResponse();
                        Map<String, Object> map = new HashMap<String, Object>();
                        JsonObject jsonObject = jsonElement.getAsJsonObject();
                        JsonElement status = jsonObject.get("status");
                        JsonElement description = jsonObject.get("description");
                        JsonElement data = jsonObject.get("data");
                        if (status != null) {
                            response.setStatus(jsonObject.get("status").getAsInt());
                        }
                        if (description != null) {
                            response.setDescription(jsonObject.get("description").getAsString());
                        }
                        if (data != null) {
                            Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.get("data").getAsJsonObject().entrySet();
                            for (Map.Entry<String, JsonElement> entry : entrySet) {
                                map.put(entry.getKey(), entry.getValue());
                            }
                            response.setData(map);
                        }
                        return response;

                    }
                }
        ).create();
    }

    public static Gson getMapGsonObject() {
        return new GsonBuilder().registerTypeAdapter(
                new TypeToken<Map<String, Object>>() {
                }.getType(),
                new JsonDeserializer<Map<String, Object>>() {
                    public Map<String, Object> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                        Map<String, Object> map = new HashMap<String, Object>();
                        JsonObject jsonObject = jsonElement.getAsJsonObject();
                        if (jsonObject != null) {
                            Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.getAsJsonObject().entrySet();
                            for (Map.Entry<String, JsonElement> entry : entrySet) {
                                map.put(entry.getKey(), entry.getValue());
                            }
                        }
                        return map;
                    }
                }
        ).create();
    }

    public static Gson getListGsonObject() {
        Gson gson = new GsonBuilder().registerTypeAdapter(
                new TypeToken<ArrayList<String>>() {
                }.getType(),
                new JsonDeserializer<ArrayList<String>>() {
                    public ArrayList<String> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                        ArrayList<String> list = new ArrayList<String>();
                        JsonArray jsonArray = jsonElement.getAsJsonArray();
                        for (JsonElement jsonElement1 : jsonArray) {
                            list.add(jsonElement1.getAsString());
                        }
                        return list;
                    }
                }
        ).create();
        return gson;
    }

    public static boolean checkCloudControlEnter() {
        if (Config.getConfig().getCloudSwitch()) {
            try {
                CloudCacheModel.getInstance().setRaspId(OSUtil.getID());
            } catch (Exception e) {
                CloudManager.LOGGER.warn("get rasp id failed", e);
            }
            String cloudAddress = Config.getConfig().getCloudAddress();
            String cloudAppId = Config.getConfig().getCloudAppId();
            return cloudAddress != null && !cloudAddress.trim().isEmpty() &&
                    cloudAppId != null && !cloudAppId.trim().isEmpty();
        }
        return false;
    }

    public static Map<String, Object> getMapFromData(GenericResponse response, String key) {
        Map<String, Object> data = response.getData();
        if (data != null) {
            Object object = data.get(key);

            if (object != null) {
                JsonObject jsonElement = (JsonObject) object;
                return getMapGsonObject().fromJson(jsonElement, new TypeToken<Map<String, Object>>() {
                }.getType());
            }
        }
        return null;
    }

    public static Object getValueFromData(GenericResponse response, String key) {
        Map<String, Object> data = response.getData();
        if (data != null) {
            return data.get(key);
        }
        return null;
    }
}
