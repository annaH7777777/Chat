package com.javarush.task.task30.task3008;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(ConsoleHelper.readInt())) {
            ConsoleHelper.writeMessage("Сервер запущен.");
            while (true) {
                Socket client = server.accept();
                Handler handler = new Handler(client);
                handler.start();
            }
        }catch(IOException e){
            ConsoleHelper.writeMessage(e.getMessage());
            }

        }

    private static class Handler extends Thread{
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }
        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException{
            Message request = new Message(MessageType.NAME_REQUEST);
            Message response;
            String name;
            do{
                connection.send(request);
                response = connection.receive();
                name = response.getData();
            }
            while ((response.getType()!=MessageType.USER_NAME) || (name.isEmpty()) || (connectionMap.containsKey(name)));

            connectionMap.put(name, connection);
            connection.send(new Message(MessageType.NAME_ACCEPTED));
            return name;
        }

        private void sendListOfUsers(Connection connection, String userName) throws IOException{
            for (Map.Entry<String, Connection> pair : connectionMap.entrySet()) {
                Message message = new Message(MessageType.USER_ADDED, pair.getKey());

                if(!pair.getKey().equals(userName))
                    connection.send(message);

            }

        }
        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException{
            while (true) {
                Message message = connection.receive();
                if(message.getType()== MessageType.TEXT){
                    sendBroadcastMessage(new Message(MessageType.TEXT, userName+": " + message.getData()));
                }
                else ConsoleHelper.writeMessage("Error");
            }
        }
        public void run(){
            ConsoleHelper.writeMessage("установлено новое соединение с адресом " + socket.getRemoteSocketAddress());
            String username = null;
            try(Connection connection = new Connection(socket)) {
                username = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, username));
                sendListOfUsers(connection, username);
                serverMainLoop(connection, username);
                connectionMap.remove(username);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, username));
                ConsoleHelper.writeMessage("Соединение с удаленным адресом закрыто");
            } catch (IOException e){
                ConsoleHelper.writeMessage("Произошла ошибка при обмене данными с удаленным адресом");
            } catch (ClassNotFoundException e){
                ConsoleHelper.writeMessage("Произошла ошибка при обмене данными с удаленным адресом");
            }

        }


    }
    public static void sendBroadcastMessage(Message message){
        for (Map.Entry<String, Connection> pair : connectionMap.entrySet()) {
            try {
                pair.getValue().send(message);
            } catch (IOException e) {
                ConsoleHelper.writeMessage("Сообщение не отправлено");
            }
        }
    }


}
