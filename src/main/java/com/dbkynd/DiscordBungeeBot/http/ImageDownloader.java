package com.dbkynd.DiscordBungeeBot.http;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;

public class ImageDownloader {
    public static void main(String u) {
        try {
            URL url = new URL(u);
            ImageIO.read(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
