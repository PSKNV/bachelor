package ru.ifmo.mailru.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.Charset;

/**
 * @author Anastasia Lebedeva
 */
public class ContentLoader {
    private BufferedReader reader;
    public static int count = 0;
    private StringBuilder builder = new StringBuilder();

    public ContentLoader(URI uri, int attemptsNumber) throws IOException {
        int n = 0;
        boolean done = false;
        URLConnection connection = uri.toURL().openConnection();
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(30000);
        while (!done)
        {
            try {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.forName("utf-8")));
                done = true;
            } catch (IOException e) {
                if (e instanceof SocketTimeoutException) {
                    throw e;
                }
                if (++n >= attemptsNumber) {
                    throw e;
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e1) {
                    System.err.println(e1.getMessage());
                }
            }
        }
    }

    public boolean isWebPage() throws IOException {
        String firstLine;
        int k = 0;
        do {
            char[] firstSymbols = new char[20];
            reader.read(firstSymbols);
            int i = 0;
            while (i < firstSymbols.length && (Character.codePointAt(firstSymbols, i) == 65279 ||
                    Character.codePointAt(firstSymbols, i) == 31)) {
                firstSymbols[i] = ' ';
                i++;
            }
            firstLine = new String(firstSymbols);
            firstLine = firstLine.toLowerCase().trim();
        } while (firstLine.equals("") && ++k < 5);
        if (firstLine.startsWith("<")) {
            builder.append(firstLine);
            return true;
        }
        return false;
    }

    public String loadRobotsTxt() throws IOException {
        return loadContent("\n");
    }

    private String loadContent(String separator) throws IOException{
        String s;
        while ((s = nextLine()) != null) {
            builder.append(s);
            builder.append(separator);
        }
        reader.close();
        return builder.toString();
    }

    public String loadWebPage() throws IOException {
        if (!isWebPage()) {
            reader.close();
            throw new IOException("Is not web page");
        }
        return loadContent("");
    }

    private String nextLine() throws IOException {
        return reader.readLine();
    }
}
