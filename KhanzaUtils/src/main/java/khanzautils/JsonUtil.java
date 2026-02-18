/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package khanzautils;

import khanzautils.logger.SystemLogger;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.ArrayNode;

/**
 *
 * @author malifnasrulloh
 */
public class JsonUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static JsonObjectBuilder createObject() {
        return new JsonObjectBuilder(mapper.createObjectNode());
    }

    public static JsonArrayBuilder createArray() {
        return new JsonArrayBuilder(mapper.createArrayNode());
    }

    public static boolean isValidJson(String json) {
        try {
            mapper.readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static class JsonObjectBuilder {

        private final ObjectNode node;

        private JsonObjectBuilder(ObjectNode node) {
            this.node = node;
        }

        public JsonObjectBuilder put(String field, String value) {
            node.put(field, value);
            return this;
        }

        public JsonObjectBuilder put(String field, int value) {
            node.put(field, value);
            return this;
        }

        public JsonObjectBuilder put(String field, long value) {
            node.put(field, value);
            return this;
        }

        public JsonObjectBuilder put(String field, boolean value) {
            node.put(field, value);
            return this;
        }

        public JsonObjectBuilder put(String field, double value) {
            node.put(field, value);
            return this;
        }

        public JsonObjectBuilder put(String field, JsonObjectBuilder value) {
            node.set(field, value.getNode());
            return this;
        }

        public JsonObjectBuilder put(String field, ObjectNode value) {
            node.set(field, value);
            return this;
        }

        public JsonObjectBuilder put(String field, JsonArrayBuilder value) {
            node.set(field, value.getNode());
            return this;
        }

        public JsonObjectBuilder put(String field, ArrayNode value) {
            node.set(field, value);
            return this;
        }

        public JsonObjectBuilder putNull(String field) {
            node.putNull(field);
            return this;
        }

        public String build() throws RuntimeException {
            try {
                return mapper.writeValueAsString(node);
            } catch (Exception e) {
                System.out.println("Failed to serialize JSON" + e);
                SystemLogger.error(e);
            }
            return null;
        }

        public ObjectNode getNode() {
            return node.deepCopy();
        }
    }

    public static class JsonArrayBuilder {

        private final ArrayNode array;

        private JsonArrayBuilder(ArrayNode array) {
            this.array = array;
        }

        public JsonArrayBuilder add(String value) {
            array.add(value);
            return this;
        }

        public JsonArrayBuilder add(int value) {
            array.add(value);
            return this;
        }

        public JsonArrayBuilder add(long value) {
            array.add(value);
            return this;
        }

        public JsonArrayBuilder add(double value) {
            array.add(value);
            return this;
        }

        public JsonArrayBuilder add(boolean value) {
            array.add(value);
            return this;
        }

        public JsonArrayBuilder addNull() {
            array.addNull();
            return this;
        }

        public JsonArrayBuilder add(JsonObjectBuilder object) {
            array.add(object.getNode());
            return this;
        }

        public JsonArrayBuilder add(ObjectNode node) {
            array.add(node);
            return this;
        }

        public ArrayNode getNode() {
            return array.deepCopy();
        }

        public String build() throws RuntimeException {
            try {
                return mapper.writeValueAsString(array);
            } catch (Exception e) {
                System.out.println("Failed to serialize JSON array " + e);
                SystemLogger.error(e);
            }
            return null;
        }
    }
}
