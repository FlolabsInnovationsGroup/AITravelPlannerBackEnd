package com.springboot.FlomadAIplanner.converter;

/**
 * Converter interface
 *
 * @param <S> source entity
 * @param <D> destination entity
 */
public interface Converter<S, D> {
    /**
     * Convert entity to DTO
     *
     * @param source source entity
     * @return destination entity
     */
    D convert(S source);
}
