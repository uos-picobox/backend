package com.uos.picobox.global.converter;

import com.uos.picobox.global.enumClass.AdminRole;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AdminRoleConverter extends EnumBaseConverter<AdminRole> {
    public AdminRoleConverter() { super(AdminRole.class); }
}
