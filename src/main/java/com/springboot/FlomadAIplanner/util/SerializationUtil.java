package com.springboot.FlomadAIplanner.util;

import java.nio.ByteBuffer;

public class SerializationUtil {
    public static byte[] serializeEmbedding(double[] embedding) {
        ByteBuffer buffer = ByteBuffer.allocate(8 * embedding.length);
        for (double val : embedding) {
            buffer.putDouble(val);
        }
        return buffer.array();
    }

    // Other utility methods
}