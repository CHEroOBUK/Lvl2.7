package GB.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.Vector;

public class Server {
    private Vector<ClientHandler> clients;

    public Server() {
        ServerSocket server = null;
        Socket socket = null;

        try {
            AuthService.connect();
            server = new ServerSocket(8189);
            System.out.println("Сервер запущен!");
            clients = new Vector<>();

            while (true) {
                socket = server.accept();
                System.out.println("Клиент подключился");
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            AuthService.disconnect();
        }
    }

    public void subscribe(ClientHandler client) {
        clients.add(client);
    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client);
    }

    public void broadcastMsg(String msg) {
        for (ClientHandler o: clients) {
            o.sendMsg(msg);
        }
    }

    public void tryToSendPrivateMSG(String str, String senderName) {
        boolean hasReceiver = false;
        String[] tokens = str.split(" ");
        String receiverName = tokens[1];
        StringBuilder msg = new StringBuilder();
        for (int i = 2; i < tokens.length; i++){
            msg.append(tokens[i]);
            msg.append(" ");
        }
        for (ClientHandler o: clients) {
            if (o.getNick().equals(receiverName)) {
                hasReceiver = true;
                o.sendMsg(senderName + ": " + msg.toString());
            }
        }
        if (!hasReceiver) {
            for (ClientHandler c : clients) {
                if (c.getNick().equals(senderName)) {
                    c.sendMsg("Доставка сообщения невозможна");
                }
            }
        } else {
            for (ClientHandler c : clients) {
                if (c.getNick().equals(senderName)) {
                    c.sendMsg(senderName + ": " + str);
                }
            }
        }
    }

    public boolean isBusyNickname(String nick){
        for (ClientHandler o: clients) {
            if (o.getNick().equals(nick)){
                return true;
            }
        }
        return false;
    }
}
