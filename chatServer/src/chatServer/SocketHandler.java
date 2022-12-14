package chatServer;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class SocketHandler implements Runnable {

    public static ArrayList<SocketHandler> clientsArray = new ArrayList<>(); // trough this array we'll be able to broadcast messages to the all requested destinations
    private Socket socket;
    private BufferedReader bufferedReader;          // BufferedReader obj read data in buffered block so until we won't fill the buffer he will not send the message
    private BufferedWriter bufferedWriter;
    private String userName;
    private String userPassword;


    public SocketHandler(Socket socket) {
        try {
            this.socket = socket;
            new OutputStreamWriter(socket.getOutputStream());
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream())); //here we recive the user name
          System.out.println("enter user name");
            this.userName = bufferedReader.readLine();
            
            
            //after getting the user name we get the user password
            System.out.println("enter the password");
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            this.userPassword= bufferedReader.readLine();
            
            clientsArray.add(this);
            broadCastMessage("IN From server:" + userName + " has joined the chat!");

        } catch (IOException e) {
            closeStream(socket, bufferedReader, bufferedWriter);

        }
    }

    @Override
    public void run() {
        String message;

        while (socket.isConnected()) {
            try {

                message = bufferedReader.readLine();
                if (message.equals("get clients list")) {
                    String list = getList();
                    bufferedWriter.write(list);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    System.out.println(list + "-----------"+bufferedWriter.toString());
                    continue;
                }
                if(message.contains("<private>")){

                    for (SocketHandler client: clientsArray){

                        if(message.contains("<private><"+client.userName+">")){
                            message = message.replaceAll("<private><"+client.userName+">","");
                            client.bufferedWriter.write(message);
                            client.bufferedWriter.newLine();
                            client.bufferedWriter.flush();
                            break;
                            }
                        }
                    }
                else{
                    broadCastMessage(message);
                }

            } catch (IOException e) {
                closeStream(socket, bufferedReader, bufferedWriter);
                e.printStackTrace();
                break;
            }
        }
    }
// in this method we can get all the users in our chat
    public String getList() {
        String users = "ONLINE USERS: ";
        for (SocketHandler client : clientsArray) {
            if (!client.userName.equals(userName)) {
                users += (client.userName + ",");
            }
        }

        return users;
    }

    public void broadCastMessage(String message) {
        for (SocketHandler client : clientsArray) {
            try {
                if (!client.userName.equals(userName)) {

                    client.bufferedWriter.write(message);
                    client.bufferedWriter.newLine();
                    client.bufferedWriter.flush();

                }
            } catch (IOException e) {

                closeStream(socket, bufferedReader, bufferedWriter);
            }
        }

    }

    public void removeClient() {
        clientsArray.remove(this);
        broadCastMessage("IN From server: " + userName + " has disconnected !!! ");
    }

    public void closeStream(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClient();
        try {
            if (bufferedReader != null) bufferedReader.close();

            if (bufferedWriter != null) bufferedWriter.close();

            if (socket != null) socket.close();


        } catch (IOException e) {
            System.out.println("failed to close connection");
            e.printStackTrace();
        }


    }
}
