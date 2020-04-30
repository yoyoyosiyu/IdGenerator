package com.huayutech.idgenerator.core.persisten;

public interface IdPersistence {

    Object get(String section, String key);

    void set(String section, String key, Object value);

    void persist();

}
