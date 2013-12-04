package us.sosia.magnet.service.libs;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import play.libs.WS;

import java.io.IOException;

/**
 * Author: kerr
 * Mail: pin.he@pekall.com
 */
public class Jsons {
    private static ObjectMapper mapper ;
    private static Gson gson;
    static {
        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        gson = new Gson();
    }

    public static <T> T toBean(final WS.Response response,final Class<T> clazz){
        try {
            return mapper.readValue(response.getBodyAsStream(), clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T toBean(final String jsonStr,final Class<T> clazz){
//        ObjectMapper mapper = new ObjectMapper();
//        try {
//            return mapper.readValue(jsonStr, clazz);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        return gson.fromJson(jsonStr,clazz);
    }

    public static <T> T toBean(final JsonNode jsonNode,final Class<T> clazz){
        try {
            return mapper.treeToValue(jsonNode, clazz);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonNode toJsonNode(final WS.Response response){
        try {
            return mapper.readValue(response.getBodyAsStream(), JsonNode.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonNode toJsonNode(final String jsonStr){
        try {
            return mapper.readValue(jsonStr, JsonNode.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonNode toJsonNode(final Object object){
        try {
            return mapper.valueToTree(object);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String toJsonString(final Object object){
        try {
            return mapper.writeValueAsString(object);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

}
