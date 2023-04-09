package model;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import server.Server;
import utils.Utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Database {
    private static final String name = "db.json";
    private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
    ;;
    private static Lock readLock = lock.readLock();
    private static Lock writeLock = lock.writeLock();
    private static final Gson gson = new Gson();

    private static Map<String, Object> database;

    private static Map<String, Object> load() throws IOException, ClassNotFoundException {
        if (database == null) {
            try {
                final String stringJson = Utils.read(Server.SERVER_DATA_DIR + name);
                Type type = new TypeToken<Map<String, Object>>() {
                }.getType();
                database = gson.fromJson(stringJson, type);
            } catch (FileNotFoundException e) {
                Utils.write(gson.toJson(new HashMap<String, Object>()), Server.SERVER_DATA_DIR + name);
                database = new HashMap<>();
            }
        }
        return database;
    }

    private static Map<String, Object> getNode(String[] keys) throws IOException, ClassNotFoundException {
        final Map<String, Object> db = load();
        Map<String, Object> node = null;
        String key;
        for (int i = 0, len = keys.length; i < len; i++) {
            key = keys[i];
            if (i == len - 1) {
                return node == null ? (Map<String, Object>) db.get(key) : node;
            }
            node = node == null ? (Map<String, Object>) db.get(key) : (Map<String, Object>) node.get(key);
        }
        return node;
    }

    private static Map<String, Object> createAndGetNode(String[] keys) throws IOException, ClassNotFoundException {
        final Map<String, Object> db = load();
        Map<String, Object> node = null;
        String key = null;
        if (!db.containsKey(keys[0])) {
            if (keys.length == 1) {
                key = keys[0];
                node = db;
            } else {
                String lastKey = null;
                for (int i = 0, len = keys.length; i < len; i++) {
                    key = keys[i];
                    if (i == 0) {
                        db.put(key, new HashMap<>());
                    } else {
                        node = ((Map<String, Object>) Objects.requireNonNullElse(node, db).get(lastKey));
                        if (i != len - 1) {
                            node.put(key, new HashMap<>());
                        }
                    }
                    lastKey = key;
                }
            }
        }

        HashMap<String, Object> res = new HashMap<>();
        res.put("node", node);
        return res;
    }

    private static String[] getKeysFromJson(Object json) {
        String[] keys = null;
        try {
            Type type = new TypeToken<String[]>() {
            }.getType();
            keys = gson.fromJson(String.valueOf(json), type);
        } catch (JsonSyntaxException ignored) {
        }
        return keys;
    }

    public static synchronized Object get(Object key) {
        try {
            readLock.lock();
            final Map<String, Object> db = load();
            String[] keys = getKeysFromJson(key);
            if (keys == null) {
                return db.get((String) key);
            }
            return keys.length == 1 ? getNode(keys) : getNode(keys).get(keys[keys.length - 1]);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            readLock.unlock();
        }
        return null;
    }

    public static synchronized boolean set(Object key, Object value) {
        try {
            writeLock.lock();
            final Map<String, Object> db = load();
            String[] keys = getKeysFromJson(key);
            if (keys == null) {
                db.put((String) key, value);
            } else {
                String k = keys[keys.length - 1];
                if (!db.containsKey(keys[0])) {
                    createAndGetNode(keys).put(k, value);
                } else {
                    getNode(keys).put(k, value);
                }
            }
            Utils.write(Server.SERVER_DATA_DIR + name, gson.toJson(db));
            return true;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            writeLock.unlock();
        }
        return false;
    }

    public static synchronized boolean delete(Object key) {
        try {
            writeLock.lock();
            final Map<String, Object> db = load();
            String[] keys = getKeysFromJson(key);

            if (keys != null) {
                getNode(keys).remove(keys[keys.length - 1]);
                Utils.write(Server.SERVER_DATA_DIR + name, gson.toJson(database));
                return true;
            } else {
                if (db.containsKey((String) key)) {
                    db.remove((String) key);
                    Utils.write(Server.SERVER_DATA_DIR + name, gson.toJson(database));
                    return true;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            writeLock.unlock();
        }
        return false;
    }
}
