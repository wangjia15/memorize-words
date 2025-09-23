package com.memorizewords.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for JSON-related test operations.
 * Provides helper methods for JSON serialization/deserialization and MVC testing.
 */
public class JsonTestUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Converts an object to JSON string.
     */
    public static String toJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    /**
     * Converts JSON string to object of specified type.
     */
    public static <T> T fromJson(String json, Class<T> clazz) throws JsonProcessingException {
        return objectMapper.readValue(json, clazz);
    }

    /**
     * Extracts response content as string from MvcResult.
     */
    public static String getResponseContent(MvcResult result) throws UnsupportedEncodingException {
        return result.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    /**
     * Performs a POST request with JSON content and returns the result.
     */
    public static MvcResult performPostRequest(MockMvc mockMvc, String url, Object requestBody) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(requestBody)))
                .andReturn();
    }

    /**
     * Performs a PUT request with JSON content and returns the result.
     */
    public static MvcResult performPutRequest(MockMvc mockMvc, String url, Object requestBody) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(requestBody)))
                .andReturn();
    }

    /**
     * Performs a GET request and returns the result.
     */
    public static MvcResult performGetRequest(MockMvc mockMvc, String url) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andReturn();
    }

    /**
     * Performs a DELETE request and returns the result.
     */
    public static MvcResult performDeleteRequest(MockMvc mockMvc, String url) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.delete(url))
                .andReturn();
    }

    /**
     * Performs a POST request with form data and returns the result.
     */
    public static MvcResult performFormPost(MockMvc mockMvc, String url, String formData) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(formData))
                .andReturn();
    }

    /**
     * Creates form data string from key-value pairs.
     */
    public static String createFormData(String... keyValuePairs) {
        if (keyValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("Key-value pairs must be provided in pairs");
        }

        StringBuilder formData = new StringBuilder();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            if (i > 0) {
                formData.append("&");
            }
            formData.append(keyValuePairs[i])
                   .append("=")
                   .append(keyValuePairs[i + 1]);
        }
        return formData.toString();
    }
}