package ru.ifmo.pools;

@FunctionalInterface
public interface Equals<T> {

    boolean areEquals(T src, T object);
}
