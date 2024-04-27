package net.group_29.opoc.model;

import java.util.List;

@SuppressWarnings({"UnusedReturnValue", "SpellCheckingInspection", "unused", "SameParameterValue"})
public interface GsPropertyBackend<TKEY, TTHIS> {
    String getString(TKEY key, String defaultValue);

    int getInt(TKEY key, int defaultValue);

    long getLong(TKEY key, long defaultValue);

    boolean getBool(TKEY key, boolean defaultValue);

    float getFloat(TKEY key, float defaultValue);

    double getDouble(TKEY key, double defaultValue);

    List<Integer> getIntList(TKEY key);

    List<String> getStringList(TKEY key);

    TTHIS setString(TKEY key, String value);

    TTHIS setInt(TKEY key, int value);

    TTHIS setLong(TKEY key, long value);

    TTHIS setBool(TKEY key, boolean value);

    TTHIS setFloat(TKEY key, float value);

    TTHIS setDouble(TKEY key, double value);

    TTHIS setIntList(TKEY key, List<Integer> value);

    TTHIS setStringList(TKEY key, List<String> value);
}
