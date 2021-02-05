package com.webservices;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.HashMap;

public class Connection implements Runnable {

    private Socket connectionSocket;
    private HashMap<String, String> request;
    private HashMap<String, String> redirect;

    public Connection(Socket connectionSocket) {

        this.connectionSocket = connectionSocket;

        this.request = new HashMap<>();
        this.redirect = new HashMap<>();

        redirect.put("/", "/index.html");
        redirect.put("/index.htm", "/index.html");
        redirect.put("/index", "/index.html");
    }

    private void parseRequest() throws IOException {

        BufferedReader connectionReader = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

        String requestLine = connectionReader.readLine();

        if (requestLine != null) {

            String[] requestLineParams = requestLine.split(" ");

            String requestMethod = requestLineParams[0];
            String requestResource = requestLineParams[1];
            String requestProtocol = requestLineParams[2];

            request.put("Method", requestMethod);
            request.put("Resource", requestResource);
            request.put("Protocol", requestProtocol);

            String headerLine = connectionReader.readLine();

            while (!headerLine.isEmpty()) {

                String[] requestParams = headerLine.split(":", 2);
                request.put(requestParams[0], requestParams[1].replaceFirst(" ", ""));
                headerLine = connectionReader.readLine();
            }
        }
    }

    private void sendResponse() throws IOException {

        DataOutputStream outStream = new DataOutputStream(connectionSocket.getOutputStream());
        String resourcePath = request.get("Resource").toString();
        File file = new File("." + resourcePath);
        if (redirect.get(resourcePath) != null) {
            outStream.writeBytes("HTTP/1.1 301 Moved Permanently\n" +
                    "Location: " + redirect.get(resourcePath));
        }

        else if (!file.exists()) {

            String http404Response = "HTTP/1.1 404 Not Found\r\n\r\n" + "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "\n" +
                    "<head>\n" +
                    "    <title>Grupp 5 Lab 1</title>\n" +
                    "</head>\n" +
                    "\n" +
                    "<body><h1>\n" +
                    "404 Error: Page Not Found\n" +
                    "</h1></body>\n" +
                    "\n" +
                    "</html>";

            outStream.write(http404Response.getBytes("UTF-8"));
        }

        else {

            FileInputStream fileStream = new FileInputStream(file);

            String contentType = Files.probeContentType(file.toPath());

            BufferedInputStream bufInputStream = new BufferedInputStream(fileStream);

            byte[] bytes = new byte[(int) file.length()];

            outStream.writeBytes("HTTP/1.1 200 OK\r\nContent-Type: " + contentType + "\r\n\r\n");

            bufInputStream.read(bytes);

            outStream.write(bytes);
            outStream.flush();

            bufInputStream.close();
        }
        outStream.close();
    }

    @Override
    public void run() {
        try {
            parseRequest();

            sendResponse();

            this.connectionSocket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
