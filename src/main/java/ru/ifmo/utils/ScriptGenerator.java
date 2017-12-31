package ru.ifmo.utils;

import ru.ifmo.entity.AbstractEntity;

public interface ScriptGenerator<T extends AbstractEntity> {

    String merge(T entity);
}
