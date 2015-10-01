package carcassonneserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CarcassonneServer {

    private static List<Socket> gamers = new ArrayList<>();
    private static boolean timesUp = false;
    private static ServerSocket server = null;
    private static int playerNumber = 2;

    public CarcassonneServer() {
        int portNumber = 8080;
        while (true) {
            try {
                server = new ServerSocket(portNumber);
                break;
            } catch (BindException | IllegalArgumentException e) {
                System.err.println("Az adott porton nem lehet socketet létrehozni. Írj be egy másikat.");
                Scanner in = new Scanner(System.in); //nyilván átírni
                portNumber = in.nextInt();
            } catch (IOException e) {
                System.err.println("I/O hiba a szerver lĂ©trehozĂˇsakor: " + e.getMessage());
                System.exit(0);
            }
        }

        System.out.println("A szerver elindult.");

        Socket client = null;
        while (gamers.size() < playerNumber) {
            System.out.println("itt");
            try {
                client = server.accept();
                ObjectInputStream ois = null;
                try {
                    ois = ois = new ObjectInputStream(client.getInputStream());
                    try {
                        gamers.add(client);
                        Object message = ois.readObject();
                    } catch (ClassNotFoundException ex) {
                        System.err.println("Az ObjectInputStream nem találja a megadott osztályt.");
                        System.exit(0);
                    }
                } catch (IOException e) {
                }
            } catch (SocketTimeoutException e) {
                System.err.println("Nem érkezett csatlakozási kérelem.");
                System.exit(0);
            } catch (IOException e) {
                System.err.println("I/O hiba accept utasításnál: " + e.getMessage());
                System.exit(0);
            }
        }
        System.out.println("EZAZZZZZZZZ, kezdődik a játék!");
        for (Socket gamer : gamers) {
            OutputStream out = null;
            try {
                out = gamer.getOutputStream();
            } catch (IOException ex) {
                Logger.getLogger(CarcassonneServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            PrintWriter pw = new PrintWriter(out);
            pw.println("kesz");
            pw.flush();
        }
    }

    public static void main(String[] args) {
        CarcassonneServer carcassonneServer = new CarcassonneServer();
    }
}
