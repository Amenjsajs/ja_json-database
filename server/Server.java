package server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server {
    private final String SERVER_ADDRESS;
    private final int SERVER_PORT;
    public static final String SERVER_DATA_DIR = System.getProperty("user.dir") + "/src/server/data/";
    private static final Map<String, String> database = new HashMap<>();
    private static final List<Session> sessions = Collections.synchronizedList(new ArrayList<>());

    public static final AtomicBoolean exit = new AtomicBoolean(false);

    public Server(String SERVER_ADDRESS, int SERVER_PORT) {
        this.SERVER_ADDRESS = SERVER_ADDRESS;
        this.SERVER_PORT = SERVER_PORT;
        if (!Files.exists(Paths.get(SERVER_DATA_DIR + "db.json"))) {
            File file = new File(SERVER_DATA_DIR);
            file.mkdirs();

            Utils.write(SERVER_DATA_DIR + "db.json", "{}");
        }
    }

    public synchronized void run() {
        try (ServerSocket server = new ServerSocket(SERVER_PORT, 50, InetAddress.getByName(SERVER_ADDRESS))) {
            System.out.println("Server started!");

            do {
                Session session = new Session(server.accept());
                sessions.add(session);
                session.start();

                TimeUnit.MILLISECONDS.sleep(100);
            } while (!exit.get());

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, String> getDatabase() {
        return database;
    }
}
