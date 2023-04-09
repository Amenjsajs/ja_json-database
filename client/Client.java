package client;

import com.beust.jcommander.Parameter;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import model.Request;
import utils.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

public class Client {
    public static final String CLIENT_DATA_DIR = System.getProperty("user.dir") + "/src/client/data/";
    private final String SERVER_ADDRESS;
    private final int SERVER_PORT;
    @Parameter(names = {"--type", "-t"})
    Object type;
    @Parameter(names = {"--key", "-k"})
    Object key;
    @Parameter(names = {"--value", "-v"})
    Object value;

    @Parameter(names = {"-in"})
    String in;

    Gson gson = new Gson();

    public Client(String SERVER_ADDRESS, int SERVER_PORT) {
        this.SERVER_ADDRESS = SERVER_ADDRESS;
        this.SERVER_PORT = SERVER_PORT;
    }

    private String formatField(Object field){
        if (field != null) {
            return  ((ArrayList<String>) field).get(0);
        }
        return null;
    }

    public void run() {
        try (Socket socket = new Socket(InetAddress.getByName(SERVER_ADDRESS), SERVER_PORT);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {

            System.out.println("Client started!");

            File file = new File(CLIENT_DATA_DIR);
            file.mkdirs();

            if (in != null) {
                if (Files.exists(Paths.get(CLIENT_DATA_DIR + in))) {
                    final String stringJson = Utils.read(CLIENT_DATA_DIR + in);
                    Type typeObj = new TypeToken<Map<String, Object>>() {
                    }.getType();

                    final LinkedTreeMap<String, Object> req = gson.fromJson(stringJson, typeObj);

                    type = req.getOrDefault("type", null);
                    key = req.getOrDefault("key", null);
                    value = req.getOrDefault("value", null);
                }
            } else {
                type = formatField(type);
                key = formatField(key);
                value = formatField(value);
            }

            Request request = new Request((String) type, key, value);

            Gson gson = new Gson();
            String requestToJson = gson.toJson(request);

            output.writeUTF(requestToJson);
            System.out.printf("Sent: %s\n", requestToJson);

            String response = input.readUTF();
            System.out.printf("Received: %s\n", response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}