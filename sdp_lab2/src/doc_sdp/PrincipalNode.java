package doc_sdp;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class PrincipalNode extends Thread {
    private final static int PORT = 4242;
    private static int count = 0;
    private static int sumOfCharacters;
    private static int portServNodes;

    private static ServerSocket serverSocket;//Socket for the server
    private final Socket socket;//Socket for those connecting to the server

    private static HashMap<String,String> mapClient = new HashMap<>();//It will register the information of the client (the inputs)
    private static HashMap<Integer, String> mapNodes = new HashMap<>();//It will register the nodes

    private static ArrayList<Integer> nodes = new ArrayList<>();

    public PrincipalNode(Socket socket) {//Constructor of the principal Node
        this.socket = socket;
    }

    private static int Hash(int numChar, int numNodes){//Determine Hash Method
        return (numChar % numNodes);
    }

    private static int asciiTransformation(String key){
        int sumOfCharacters = 0;
        for(int i = 0; i < key.length(); i++) {
            int asciiValue = key.charAt(i);
            sumOfCharacters = sumOfCharacters + asciiValue;
        }
        return sumOfCharacters;
    }


    public void run() {//Thread
        try {

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            Object DistinctionNodeClient = in.readObject();

            if(DistinctionNodeClient.equals("Client")) {
                System.out.println("New client connection - " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort()+"");
                String key;

                while (true) {
                    String command = (String) in.readObject();

                    if (command.equalsIgnoreCase("R")) {
                        key = (String) in.readObject();
                        String value = (String) in.readObject();

                        sumOfCharacters = asciiTransformation(key);

                        int numNodes = nodes.size();

                        String ID = mapNodes.get(Hash(sumOfCharacters, (numNodes)));//Returns the value and uses it as a Key (of a node ID)
                        if(ID.equals("PrincipalNode")){
                            System.out.println("Principal Node");
                            if (mapClient.containsKey(key)) {
                                out.writeObject("This key already exists.");
                            }else if (!mapClient.containsKey(key)) {
                                mapClient.put(key, value);//Store what was sent by the Principal Node
                                out.writeObject("Item registered with success.");
                            }else {
                                out.writeObject("An error has occurred");
                            }
                        }else{
                            String[] IPPort = ID.split(" ");//separates in every space in different strings
                            String IPNode = IPPort[0];//First on the Array is the IP of the participant node
                            int portNode = Integer.parseInt(IPPort[1]);//Turn String into Int

                            Socket nodeSocket = new Socket(IPNode, portNode);//Create a socket with the information of the participant node
                            ObjectOutputStream outToNode = new ObjectOutputStream(nodeSocket.getOutputStream());
                            ObjectInputStream inToNode = new ObjectInputStream(nodeSocket.getInputStream());

                            outToNode.writeObject(command);
                            outToNode.writeObject(key);
                            outToNode.writeObject(value);

                            String response = (String) inToNode.readObject();
                            out.writeObject(response);
                        }

                    } else if (command.equalsIgnoreCase("C")) {
                        key = (String) in.readObject();

                        sumOfCharacters = asciiTransformation(key);

                        int numNodes = nodes.size();
                        String ID = mapNodes.get(Hash(sumOfCharacters, numNodes));
                        if(ID.equals("PrincipalNode")){
                            if (!mapClient.containsKey(key)) {
                                out.writeObject("This key doesn't exist.");
                            } else if (mapClient.containsKey(key)) {
                                String valueOfKey = mapClient.get(key);
                                out.writeObject(valueOfKey);
                            } else {
                                out.writeObject("An error has occurred");
                            }
                        }else{
                            String[] IPPort = ID.split(" ");//separates in every space in different strings
                            String IPNode = IPPort[0];//First on the Array is the IP of the participant node
                            int portNode = Integer.parseInt(IPPort[1]);//Turn String into Int

                            Socket nodeSocket = new Socket(IPNode, portNode);//Create a socket with the information of the participant node
                            ObjectOutputStream outToNode = new ObjectOutputStream(nodeSocket.getOutputStream());
                            ObjectInputStream inToNode = new ObjectInputStream(nodeSocket.getInputStream());

                            outToNode.writeObject(command);
                            outToNode.writeObject(key);

                            String response = (String) inToNode.readObject();
                            out.writeObject(response);
                        }

                    } else if (command.equalsIgnoreCase("D")) {
                        key = (String) in.readObject();

                        sumOfCharacters = asciiTransformation(key);

                        int numNodes = nodes.size();
                        String ID = mapNodes.get(Hash(sumOfCharacters, numNodes));
                        if(ID.equals("PrincipalNode")){
                            if(mapClient.containsKey(key)){
                                mapClient.remove(key);
                                out.writeObject("Item removed with success.");
                            }else if (!mapClient.containsKey(key)){
                                out.writeObject("This key doesn't exist.");
                            }else{
                                out.writeObject("An error has occurred");
                            }
                        }else{
                            String[] IPPort = ID.split(" ");//separates in every space in different strings
                            String IPNode = IPPort[0];//First on the Array is the IP of the participant node
                            int portNode = Integer.parseInt(IPPort[1]);//Turn String into Int

                            Socket nodeSocket = new Socket(IPNode, portNode);//Create a socket with the information of the participant node
                            ObjectOutputStream outToNode = new ObjectOutputStream(nodeSocket.getOutputStream());
                            ObjectInputStream inToNode = new ObjectInputStream(nodeSocket.getInputStream());

                            outToNode.writeObject(command);
                            outToNode.writeObject(key);

                            String response = (String) inToNode.readObject();
                            out.writeObject(response);
                        }

                    } else if (command.equalsIgnoreCase("L")) {

                        String ID;
                        String[] IPPort;
                        String IPNode;
                        int portNode;
                        Socket nodeSocket;
                        ObjectOutputStream outToNode;

                        if(mapClient.isEmpty()){
                            System.out.println("No items registered");
                        }else{
                            Set entrySet = mapClient.entrySet();// Getting a Set of Key-value pairs
                            Iterator it = entrySet.iterator();// Obtaining an iterator for the entry set

                            // Iterate through HashMap entries(Key-Value pairs)
                            while(it.hasNext()){
                                Map.Entry me = (Map.Entry)it.next();
                                System.out.println("Key: " + me.getKey() + " value: "+me.getValue());
                            }
                        }

                        int numNodes = nodes.size();
                        for (int i = 1; i < numNodes; i++){
                            ID = mapNodes.get(Hash(i, numNodes));
                            IPPort = ID.split(" ");//separates in every space in different strings
                            IPNode = IPPort[0];//First on the Array is the IP of the participant node
                            portNode = Integer.parseInt(IPPort[1]);
                            nodeSocket = new Socket(IPNode, portNode);
                            outToNode = new ObjectOutputStream(nodeSocket.getOutputStream());
                            outToNode.writeObject(command);
                        }

                    } else {
                        System.out.println("Invalid command!");
                    }
                }
            } else if(DistinctionNodeClient.equals("Node")){
                if (count < 9){
                    System.out.println("New node connection - " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort()+"");
                    count ++;
                    nodes.add(count);
                    String portNodes = String.valueOf(portServNodes);
                    out.writeObject(portNodes);
                    String IPNode = socket.getInetAddress().getHostAddress();//To get the IP that is connected to this Principal node
                    String IPPortNode = IPNode + " " + portServNodes;//ip + port
                    mapNodes.put(count, IPPortNode);

                    portServNodes++;
                    out.writeObject(count);

                }else{
                    System.out.println("An error has occurred - More then 10 nodes are not permitted");
                }
            }

            socket.close();
            out.close();
            in.close();

        } catch (IOException e) {
            e.printStackTrace();
        }catch (ClassNotFoundException a){
            System.out.println("An error has occurred");
        }
    }

    public static void main(String[] args) {
        portServNodes = 4243;
        mapNodes.put(count, "PrincipalNode");
        nodes.add(count);
        System.out.println("I'm node 0");

        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("[started]");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        while (!serverSocket.isClosed()) {//While server is open
            try {
                Socket socket = serverSocket.accept();//accept the connections
                new PrincipalNode(socket).start();//run thread

            } catch (IOException ioe) {
                if (serverSocket.isClosed()) {
                    System.out.println("[terminated]");
                } else {
                    ioe.printStackTrace();
                }
            }
        }
    }
}
