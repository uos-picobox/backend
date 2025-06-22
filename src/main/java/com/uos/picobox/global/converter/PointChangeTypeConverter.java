package com.uos.picobox.global.converter;

import com.uos.picobox.global.enumClass.PointChangeType;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PointChangeTypeConverter extends EnumBaseConverter<PointChangeType>{
    public PointChangeTypeConverter() { super(PointChangeType.class); }
}
