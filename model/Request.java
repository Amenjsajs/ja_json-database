package model;

import com.beust.jcommander.Parameter;

public class Request {
    @Parameter(names = {"--type", "-t"})
    String type;
    @Parameter(names = {"--key", "-k"})
    Object key;
    @Parameter(names = {"--value", "-v"})
    Object value;

    public Request(){}

    public Request(String type, Object key) {
        this.type = type;
        this.key = key;
    }

    public Request(String type, Object key, Object value) {
        this(type, key);
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getKey() {
        return key;
    }

    public void setKey(Object key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

//    @Override
//    public String toString() {
//        return "{" +
//                "\"type\":\"" + type + "\"" +
//                ",\"key\":\"" + key + "\"" +
//                ",\"key\":\"" + value + "\"" +
//                "}";
//    }
}
