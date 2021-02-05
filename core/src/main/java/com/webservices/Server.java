package com.webservices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static void main(String[] args) {

        //ExecutorService executorService = Executors.newCachedThreadPool();

        try {

            ServerSocket serverSocket = new ServerSocket(3456);
            System.out.println("Listening for connections on port 3456...\r\n");

            while(true) {

                Socket connectionSocket = serverSocket.accept();

                Thread connectionThread = new Thread(new Connection(connectionSocket));

                connectionThread.start();

                //executorService.execute(() -> handleConnection(connectionSocket));

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

        public static void handleConnection(Socket socket){
            try{
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                while(true){
                    String headerLine = input.readLine();
                    System.out.println(headerLine);
                    if(headerLine.isEmpty())
                        break;
                }

                System.out.println("New connection on port 3456...\r\n");

                var output = new PrintWriter(socket.getOutputStream());
                String page = """
                        <html>
                        <head>
                           <title>Hello World!</title>
                        </head>
                         <body>
                         <h1>Hello there</h1>
                         <div>First page</div>
                         </body>                   
                         </html>""";

                output.println("HTTP/1.1 200 OK");
                output.println("Content-Length:" + page.getBytes().length);
                output.println("Content-Type:text/html");  //application/json
                output.println("");
                output.print(page);

                output.flush();
                socket.close();

            }catch (IOException e) {
                e.printStackTrace();
        }
    }
}
