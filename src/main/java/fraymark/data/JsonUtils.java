package fraymark.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class JsonUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static List<Map<String, Object>> readList(String path) {
        try (InputStream in = JsonUtils.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new RuntimeException("File not found on classpath: " + path);
            }

            byte[] bytes = in.readAllBytes();
            System.out.println("DEBUG: JSON loaded " + path + " (" + bytes.length + " bytes)");

            if (bytes.length == 0) {
                System.out.println("DEBUG: Empty file → returning empty list for " + path);
                return List.of();
            }

            List<Map<String, Object>> list =
                    MAPPER.readValue(bytes, new TypeReference<List<Map<String, Object>>>() {});
            System.out.println("DEBUG: Parsed " + list.size() + " entries from " + path);
            return list;
        } catch (Exception e) {
            throw new RuntimeException("Failed to read JSON at " + path, e);
        }
    }
}
