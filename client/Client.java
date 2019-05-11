package com.javarush.task.task30.task3008.client;

import com.javarush.task.task30.task3008.Connection;
import com.javarush.task.task30.task3008.ConsoleHelper;
import com.javarush.task.task30.task3008.Message;
import com.javarush.task.task30.task3008.MessageType;

import java.io.IOException;
import java.net.Socket;

public class Client {

    public static void main(String args[]){
        Client client = new Client();
        client.run();

    }

    protected Connection connection;
    private volatile boolean clientConnected = false;

    public class SocketThread extends Thread{

        public void run(){
            try(Socket socket = new Socket(getServerAddress(),getServerPort())) {
                Client.this.connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
            }catch (IOException e){
                notifyConnectionStatusChanged(false);
            }catch (ClassNotFoundException e){
                notifyConnectionStatusChanged(false);
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException{
            while (true){
                Message request = connection.receive();
                if (request.getType() == MessageType.NAME_REQUEST) {
                    Message response = new Message(MessageType.USER_NAME, getUserName());
                    connection.send(response);

                }
                else if (request.getType() == MessageType.NAME_ACCEPTED) {
                    notifyConnectionStatusChanged(true);
                    return;
                }
                else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException{
            while (true){
                Message request = connection.receive();
                if (request.getType() == MessageType.TEXT) {
                    processIncomingMessage(request.getData());

                }
                else if (request.getType() == MessageType.USER_ADDED) {
                    informAboutAddingNewUser(request.getData());

                }
                else if (request.getType() == MessageType.USER_REMOVED){
                    informAboutDeletingNewUser(request.getData());
                    
                }
                else {
                    throw new IOException("Unexpected MessageType");
                }

            }
        }

        protected void processIncomingMessage(String message){
            ConsoleHelper.writeMessage(message);
        }
        protected void informAboutAddingNewUser(String userName){
            ConsoleHelper.writeMessage(userName+" присоединился к чату.");
        }
        protected void informAboutDeletingNewUser(String userName){
            ConsoleHelper.writeMessage(userName+" покинул чат.");
        }
        protected void notifyConnectionStatusChanged(boolean clientConnected){
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this) {
                Client.this.notify();
            }
        }

    }

    public void run(){
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        try {
            synchronized (this){
                this.wait();
            }
        } catch (InterruptedException e) {
            ConsoleHelper.writeMessage("Ошибка");
            return;
        }
        if(clientConnected) {
            ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду 'exit'.");
            while (clientConnected){
                String message = ConsoleHelper.readString();
                if("exit".equals(message)) break;
                if(shouldSendTextFromConsole()) sendTextMessage(message);
            }
        }
        else ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");

    }

    protected String getServerAddress(){
        ConsoleHelper.writeMessage("Введите адрес сервера: ");
        return ConsoleHelper.readString();
    }
    protected int getServerPort(){
        ConsoleHelper.writeMessage("Введите порт сервера: ");
        return ConsoleHelper.readInt();
    }
    protected String getUserName(){
        ConsoleHelper.writeMessage("Введите имя пользователя: ");
        return ConsoleHelper.readString();
    }
    protected boolean shouldSendTextFromConsole(){
        return true;
    }
    protected SocketThread getSocketThread(){
        return new SocketThread();
    }
    protected void sendTextMessage(String text){
        try {
            Message message = new Message(MessageType.TEXT, text);
            connection.send(message);
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Ошибка");
            clientConnected = false;
        }
    }

}
