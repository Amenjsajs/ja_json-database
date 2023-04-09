package server;

public class Main {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 23456;

    public static void main(String[] args) {
        Server server = new Server(SERVER_ADDRESS, SERVER_PORT);
        server.run();
    }
}
