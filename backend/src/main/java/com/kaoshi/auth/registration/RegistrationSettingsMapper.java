package com.kaoshi.auth.registration;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface RegistrationSettingsMapper {
    @Select("select config_value from system_configs where config_key = #{key}")
    String findValue(@Param("key") String key);

    @Update("""
            update system_configs
            set config_value = #{value}, updated_at = current_timestamp
            where config_key = #{key}
            """)
    int updateValue(@Param("key") String key, @Param("value") String value);

    @Insert("insert into system_configs (config_key, config_value, description) values (#{key}, #{value}, #{description})")
    void insertValue(@Param("key") String key, @Param("value") String value, @Param("description") String description);
}
