package doc_sdp;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;



public class Client {

    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 4242;

    public static void main(String[] args) {
        Object response;
        Scanner sc = new Scanner(System.in);

        try {
            Socket clientSocket = new Socket(SERVER_IP, SERVER_PORT);
            System.out.println("Connection established");
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
            out.writeObject("Client");

            while(true) {

                String[] command = sc.nextLine().split(" ", 3);
                try{

                    if (command[0].equalsIgnoreCase("R")) {
                        try {
                            String key = command[1];
                            String value = command[2];
                            out.writeObject(command[0]);
                            out.writeObject(key);
                            out.writeObject(value);
                            response = in.readObject();
                            System.out.println(response);
                        } catch (ArrayIndexOutOfBoundsException e) {
                            System.out.println("An error has occurred");
                        }

                    } else if (command[0].equalsIgnoreCase("C")) {
                        try{
                            String TriggerError = command[1];
                            out.writeObject(command[0]);
                            out.writeObject(command[1]);
                            String valueOfKey = (String) in.readObject();
                            System.out.println(valueOfKey);
                        }catch (ArrayIndexOutOfBoundsException e) {
                            System.out.println("An error has occurred");
                        }

                    } else if (command[0].equalsIgnoreCase("D")) {
                        try{
                            String TriggerError = command[1];
                            out.writeObject(command[0]);
                            out.writeObject(command[1]);
                            response = in.readObject();
                            System.out.println(response);
                        }catch (ArrayIndexOutOfBoundsException e) {
                            System.out.println("An error has occurred");
                        }

                    } else if (command[0].equalsIgnoreCase("L")) {
                        out.writeObject(command[0]);
                    } else if (command[0].equalsIgnoreCase("Q")){
                        clientSocket.close();
                        in.close();
                        out.close();
                        sc.close();
                        break;
                    }else{
                        System.out.println("Invalid command!");
                    }

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.out.println("An error has occurred");
        }
    }
}
