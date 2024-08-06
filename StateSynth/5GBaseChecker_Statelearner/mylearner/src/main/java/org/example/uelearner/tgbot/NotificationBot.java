package org.example.uelearner.tgbot;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

public class NotificationBot {

    public static void sendToTelegram(String text) {

        String urlString = "https://api.telegram.org/"; //put your apiToken here. This is a fake token.

        //Add Telegram token (given Token is fake)
        String apiToken = "6250867755:AAH6apo"; //put your apiToken here. This is a fake token.

        //Add chatId (given chatId is fake)
        String chatId = "-10219"; //put your chatid here. This is a fake chatId.

        urlString = String.format(urlString, apiToken, chatId, text);

        try {
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();
            InputStream is = new BufferedInputStream(conn.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}