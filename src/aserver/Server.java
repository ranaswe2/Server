package aserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;

public class Server {
    static HashMap<String, DataOutputStream> clients = new HashMap();

    public Server() {
    }

    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(2774);
        String ip = InetAddress.getLocalHost().getHostAddress();
        System.out.println("Server's IP Address: " + ip);

        while(true) {
            Socket s = ss.accept();
            System.out.println("New Client Added: " + String.valueOf(s));
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            dos.writeUTF("Enter Your Name: ");
            String name = dis.readUTF();
            clients.put(name, dos);
            System.out.println("New Client Name: " + name);
            sendActiveClients();
            (new Thread(() -> {
                while(true) {
                    try {
                        String received = dis.readUTF();
                        System.out.println(received);
                        if (received.equals("active clients")) {
                            sendActiveClients();
                        } else if (!received.equals("")) {
                            String[] parts = received.split(":");
                            String recipient = parts[0];
                            String message = parts[1];
                            DataOutputStream recipientDos = (DataOutputStream)clients.get(recipient);
                            if (recipientDos != null) {
                                recipientDos.writeUTF(name + ": " + message);
                            } else {
                                dos.writeUTF("Error: Client not found!");
                            }
                        }
                    } catch (IOException var8) {
                        clients.remove(name);
                        sendActiveClients();
                        return;
                    }
                }
            })).start();
        }
    }

    private static void sendActiveClients() {
        StringBuilder activeClients = new StringBuilder("Active Clients: ");
        Iterator var1 = clients.keySet().iterator();

        while(var1.hasNext()) {
            String client = (String)var1.next();
            activeClients.append(client + " ");
        }

        String activeClientsList = activeClients.toString();
        Iterator var7 = clients.values().iterator();

        while(var7.hasNext()) {
            DataOutputStream dos = (DataOutputStream)var7.next();

            try {
                dos.writeUTF(activeClientsList);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }
}