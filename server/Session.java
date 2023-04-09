package server;

import com.google.gson.Gson;

import model.Database;
import model.Request;
import model.Response;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Session extends Thread {
    private final Socket socket;
    private final Gson gson;

    public Session(Socket socketForClient) {
        this.socket = socketForClient;
        gson = new Gson();
    }

    public synchronized void run() {
        try {
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());

            String query = input.readUTF();
            Request request = gson.fromJson(query, Request.class);

            Response response = null;
            switch (request.getType()) {
                case "get" -> response = get(request.getKey());
                case "set" -> response = set(request.getKey(), request.getValue());
                case "delete" -> response = delete(request.getKey());
                case "exit" -> response = exit();
            }
            output.writeUTF(gson.toJson(response));
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getSocket() {
        return socket;
    }

    private Response errorResponse() {
        Response response = new Response("ERROR");
        response.setReason("No such key");
        return response;
    }

    private synchronized Response exit() {
        Server.exit.set(true);
        return new Response("OK");
    }

    private synchronized Response get(Object key) {
        Object res = Database.get(key);
        System.out.println(res);
        if (res == null) {
            return errorResponse();
        }
        Response response =  new Response("OK", res);
        return response;
    }

    private synchronized Response set(Object key, Object value) {
        Database.set(key, value);
        return new Response("OK");
    }

    private synchronized Response delete(Object key) {
        final boolean res = Database.delete(key);
        if (!res) {
            return errorResponse();
        }

        return new Response("OK");
    }
}
