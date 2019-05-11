package com.javarush.task.task30.task3008.client;

import com.javarush.task.task30.task3008.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BotClient extends Client {

    public static void main(String args[]){
        BotClient botClient = new BotClient();
        botClient.run();
    }

    public class BotSocketThread extends SocketThread{
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            if(message != null) {
                ConsoleHelper.writeMessage(message);
                if(message.contains(": ")) {
                    String[] nameText = message.split(": ");
                    String userName = nameText[0];
                    String text = nameText[1];
                    if (userName != null && text != null) {
                        SimpleDateFormat format = null;
                        if ("дата".equals(text)) format = new SimpleDateFormat("d.MM.YYYY");
                        else if ("день".equals(text)) format = new SimpleDateFormat("d");
                        else if ("месяц".equals(text)) format = new SimpleDateFormat("MMMM");
                        else if ("год".equals(text)) format = new SimpleDateFormat("YYYY");
                        else if ("время".equals(text)) format = new SimpleDateFormat("H:mm:ss");
                        else if ("час".equals(text)) format = new SimpleDateFormat("H");
                        else if ("минуты".equals(text)) format = new SimpleDateFormat("m");
                        else if ("секунды".equals(text)) format = new SimpleDateFormat("s");


                        if (format != null)
                            sendTextMessage("Информация для " + userName + ": " + format.format(Calendar.getInstance().getTime()));
                    }
                }
            }
        }
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() {
        return "date_bot_" + (int) (Math.random()*100);
    }
}
