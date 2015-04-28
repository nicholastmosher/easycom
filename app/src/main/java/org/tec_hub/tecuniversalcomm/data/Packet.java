package org.tec_hub.tecuniversalcomm.data;

import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;

/**
 * Created by Nick Mosher on 4/24/15.
 */
public class Packet {

    private String mName;
    private Object mData;

    private Packet(String name, Object data) {
        mName = Preconditions.checkNotNull(name);
        mData = Preconditions.checkNotNull(data);
    }

    public static Packet asBoolean(String name, boolean value) {
        return new Packet(name, value);
    }

    public static Packet asBooleanArray(String name, boolean[] value) {
        return new Packet(name, value);
    }

    public static Packet asInt(String name, int value) {
        return new Packet(name, value);
    }

    public static Packet asIntArray(String name, int[] value) {
        return new Packet(name, value);
    }

    public static Packet asLong(String name, long value) {
        return new Packet(name, value);
    }

    public static Packet asLongArray(String name, long[] value) {
        return new Packet(name, value);
    }

    public static Packet asDouble(String name, double value) {
        return new Packet(name, value);
    }

    public static Packet asDoubleArray(String name, double[] value) {
        return new Packet(name, value);
    }

    public static Packet asString(String name, String value) {
        return new Packet(name, value);
    }

    public static Packet asStringArray(String name, String[] value) {
        return new Packet(name, value);
    }

    public String getName() {
        return mName;
    }

    public boolean getBoolean() throws IllegalStateException {
        if(mData instanceof Boolean) {
            return (Boolean) mData;
        } else {
            throw new IllegalStateException("Command is not a boolean!");
        }
    }

    public boolean getBooleanArray() throws IllegalStateException {
        if(mData instanceof Boolean[]) {
            return (Boolean) mData;
        } else {
            throw new IllegalStateException("Command is not a boolean!");
        }
    }

    public int getInt() throws IllegalStateException {
        if(mData instanceof Integer) {
            return (Integer) mData;
        } else {
            throw new IllegalStateException("Command is not an int!");
        }
    }

    public boolean getIntArray() throws IllegalStateException {
        if(mData instanceof Integer[]) {
            return (Boolean) mData;
        } else {
            throw new IllegalStateException("Command is not a boolean!");
        }
    }

    public long getLong() throws IllegalStateException {
        if(mData instanceof Long) {
            return (Long) mData;
        } else {
            throw new IllegalStateException("Command is not a long!");
        }
    }

    public boolean getLongArray() throws IllegalStateException {
        if(mData instanceof Long[]) {
            return (Boolean) mData;
        } else {
            throw new IllegalStateException("Command is not a boolean!");
        }
    }

    public double getDouble() throws IllegalStateException {
        if(mData instanceof Double) {
            return (Double) mData;
        } else {
            throw new IllegalStateException("Command is not a double!");
        }
    }

    public boolean getDoubleArray() throws IllegalStateException {
        if(mData instanceof Double[]) {
            return (Boolean) mData;
        } else {
            throw new IllegalStateException("Command is not a boolean!");
        }
    }

    public String getString() throws IllegalStateException {
        if(mData instanceof String) {
            return (String) mData;
        } else {
            throw new IllegalStateException("Command is not a String!");
        }
    }

    public boolean getStringArray() throws IllegalStateException {
        if(mData instanceof String[]) {
            return (Boolean) mData;
        } else {
            throw new IllegalStateException("Command is not a boolean!");
        }
    }

    public static Type getTypeToken() {
        return new TypeToken<Packet>(){}.getType();
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static String toJson(Packet packet) {
        return new Gson().toJson(packet);
    }

    public static Packet fromJson(String json) {
        return new Gson().fromJson(json, getTypeToken());
    }
}
