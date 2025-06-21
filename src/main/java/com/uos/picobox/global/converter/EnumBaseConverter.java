package com.uos.picobox.global.converter;

import com.uos.picobox.global.enumClass.BaseEnum;
import com.uos.picobox.global.utils.EnumUtils;
import jakarta.persistence.AttributeConverter;

public abstract class EnumBaseConverter<E extends Enum<E> & BaseEnum> implements AttributeConverter<E, String> {
    private final Class<E> enumClass;

    protected EnumBaseConverter(Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public String convertToDatabaseColumn(E attribute) {
        return attribute.getValue();
    }

    @Override
    public E convertToEntityAttribute(String dbData) {
        return EnumUtils.fromValue(enumClass, dbData);
    }
}