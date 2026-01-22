/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fungsi;

import fungsi.logger.SystemLogger;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 *
 * @author malifnasrulloh
 */
public class JsonUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static JsonObjectBuilder createObject() {
        return new JsonObjectBuilder(mapper.createObjectNode());
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
}
