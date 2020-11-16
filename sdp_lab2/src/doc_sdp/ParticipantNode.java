package doc_sdp;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;

public class ParticipantNode {

    private static ServerSocket serverSocket;
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 4242;
    private static HashMap<String,String> mapKeyValue = new HashMap<>();//Stores the key that the PrincipalNode sent

    public static void main(String[] args) {

        try {
            Socket socketPN = new Socket(SERVER_IP, SERVER_PORT);//Make a socket for this node
            ObjectInputStream in = new ObjectInputStream(socketPN.getInputStream());//Receive information
            ObjectOutputStream out = new ObjectOutputStream(socketPN.getOutputStream());//Send information in text form

            out.writeObject("Node");

            String portServ = (String) in.readObject();
            int portNodes = Integer.parseInt(portServ);;//int to string

            int id = (int) in.readObject();
            System.out.println("I'm node " + id);
            socketPN.close();

            try {
                serverSocket = new ServerSocket(portNodes);//The port previously generated
                System.out.println("[started]");

                while (!serverSocket.isClosed()) {//while server is open
                    try {
                        Socket socket = serverSocket.accept();//accept it's connections
                        ObjectInputStream inNode = new ObjectInputStream(socket.getInputStream());//Receive information
                        ObjectOutputStream outNode = new ObjectOutputStream(socket.getOutputStream());//Send information in text form
                        System.out.println("[The principalNode has connected]");
                        String command = (String) inNode.readObject();
                        if(command.equalsIgnoreCase("R")){
                            String key = (String) inNode.readObject();//Receives the Payload from PrincipalNode
                            String value = (String) inNode.readObject();
                            if (mapKeyValue.containsKey(key)) {
                                outNode.writeObject("This key already exists.");
                            }else if (!mapKeyValue.containsKey(key)) {
                                mapKeyValue.put(key, value);//Store what was sent by the Principal Node
                                outNode.writeObject("Item registered with success.");
                            }else {
                                outNode.writeObject("An error has occurred");
                            }
                        }else if(command.equalsIgnoreCase("C")){
                            String key = (String) inNode.readObject();
                            if (!mapKeyValue.containsKey(key)) {
                                outNode.writeObject("This key doesn't exist.");
                            } else if (mapKeyValue.containsKey(key)) {
                                String valueOfKey = mapKeyValue.get(key);
                                outNode.writeObject(valueOfKey);
                            } else {
                                outNode.writeObject("An error has occurred");
                            }

                        }else if(command.equalsIgnoreCase("D")){
                            String key = (String) inNode.readObject();
                            if(mapKeyValue.containsKey(key)){
                                mapKeyValue.remove(key);
                                outNode.writeObject("Item removed with success.");
                            }else if (!mapKeyValue.containsKey(key)){
                                outNode.writeObject("This key doesn't exist.");
                            }else{
                                outNode.writeObject("An error has occurred");
                            }
                        }else if (command.equalsIgnoreCase("L")) {

                            if(mapKeyValue.isEmpty()){
                                System.out.println("No items registered");
                            }else{
                                Set entrySet = mapKeyValue.entrySet();// Getting a Set of Key-value pairs
                                Iterator it = entrySet.iterator();// Obtaining an iterator for the entry set

                                // Iterate through HashMap entries(Key-Value pairs)
                                while(it.hasNext()){
                                    Map.Entry me = (Map.Entry)it.next();
                                    System.out.println("Key: " + me.getKey() + " value: "+me.getValue());
                                }
                            }

                        }else{
                            outNode.writeObject("An error has occurred");
                        }

                    } catch (IOException ioe) {
                        if (serverSocket.isClosed()) {
                            System.out.println("[terminated]");
                        } else {
                            ioe.printStackTrace();
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            System.out.println("Cannot open more than 10 nodes");
        }catch (ClassNotFoundException a){
            a.printStackTrace();
        }
    }
}
