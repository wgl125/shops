package com.shop.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import com.shop.model.Result;

public class JsonUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void writeJson(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.write(MAPPER.writeValueAsString(data));
            writer.flush();
        }
    }

    public static <T> void writeJsonResponse(HttpServletResponse response, Result<T> result) throws IOException {
        writeJson(response, result);
    }

    public static <T> T parseJson(String json, Class<T> clazz) throws IOException {
        return MAPPER.readValue(json, clazz);
    }

    public static String toJson(Object obj) throws IOException {
        return MAPPER.writeValueAsString(obj);
    }
}
