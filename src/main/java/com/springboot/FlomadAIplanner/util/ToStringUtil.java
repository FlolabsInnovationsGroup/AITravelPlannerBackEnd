package com.springboot.FlomadAIplanner.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serial;

public class ToStringUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    /**
     * Returns a string representation of the given object, with only non-null fields.
     * Both inherited and declared fields are included and checked for null.
     * <p> This method uses the Jackson ObjectMapper class to generate the string.
     *
     * @param obj the object to convert to a string
     * @return a string representation of the object
     */
    public static String toStringNonNullOnly(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            System.err.println("Error converting object to JSON string: " + e.getMessage());
            return toString(obj);
        }
    }


    /**
     * Returns a string representation of the given object, with subclasses null fields.
     * <p> This method uses the Apache Commons Lang ToStringBuilder class to generate the string.
     *
     * @param obj the object to convert to a string
     * @return a string representation of the object
     */
    public static String toString(Object obj) {
        return ToStringBuilder.reflectionToString(obj, new CustomToStringStyle());
    }

    private static class CustomToStringStyle extends ToStringStyle {
        @Serial
        private static final long serialVersionUID = 1L;

        CustomToStringStyle() {
            this.setUseClassName(true);
            this.setUseIdentityHashCode(false);
            this.setFieldSeparator(", ");
            this.setContentStart("{");
            this.setContentEnd("}");
            this.setNullText("");
            this.setUseShortClassName(true);
        }

        @Override
        protected void appendDetail(StringBuffer buffer, String fieldName, Object value) {
            if (value != null) {
                super.appendDetail(buffer, fieldName, value);
            }
        }
    }
}
