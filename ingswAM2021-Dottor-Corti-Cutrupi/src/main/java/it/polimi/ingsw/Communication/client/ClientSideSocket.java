package it.polimi.ingsw.Communication.client;


import it.polimi.ingsw.Communication.client.actions.Action;
import it.polimi.ingsw.Communication.client.actions.CreateMatchAction;
import it.polimi.ingsw.Communication.client.actions.JoinMatchAction;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Locale;

public class ClientSideSocket {
    private final String serverAddress;
    private int gameID;
    private final int serverPort;
    SocketObjectListener objectListener;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
    private ActionParser actionParser;


    /** Constructor ConnectionSocket creates a new ConnectionSocket instance. */
    public ClientSideSocket(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.actionParser = new ActionParser();
    }

    /**
     * Method setup initializes a new socket connection and handles the nickname-choice response. It
     * loops until the server confirms the successful connection (with no nickname duplication and
     * with a correctly configured match lobby).
     *
     * @return boolean true if connection is successful, false otherwise.
     */
    public boolean setup(){
        try {
            System.out.println("Configuring socket connection...");
            System.out.println("Opening a socket server communication on port "+ serverPort+ "...");
            Socket socket;
            try {
                socket = new Socket(serverAddress, serverPort);
            } catch (SocketException | UnknownHostException e) {
                return false;
            }

            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());

            //creating listeners for string and object messages from server
            //object messages
            objectListener = new SocketObjectListener(this ,inputStream);
            Thread thread1 = new Thread(objectListener);
            thread1.start();

            createOrJoinMatchChoice();
            loopRequest();
            return true;
        } catch (IOException e) {
            System.err.println("Error during socket configuration! Application will now close.");
            System.exit(0);
            return false;
        }
    }

    private void loopRequest() {
        while (true){
            try {
                //out.println(stdIn.readLine()); PREVIOUS COMMAND
                System.out.println("we are now reading from keyboard!");
                String keyboardInput = stdIn.readLine();
                System.out.println("you have typed " + keyboardInput);

                Action actionToSend = this.actionParser.parseInput(keyboardInput);
                if(!actionToSend.equals(null)) {
                    System.out.println("we are now trying to send the message");
                    send(actionToSend);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void createOrJoinMatchChoice(){
        try {
            String line;
            do {
                System.out.println("Do you want to create a new match or join/rejoin an already created one?");
                line = stdIn.readLine().toLowerCase(Locale.ROOT);
                if(!line.equals("create") && !line.equals("join") && !line.equals("rejoin")) System.out.println("Please select either join, rejoin, or create");
            }while (!line.equals("create") && !line.equals("join") && !line.equals("rejoin"));

            //dividing the possible choices in their respective method
            switch (line){
                case "create":
                    createMatch();
                    break;
                case "join":
                    joinMatch();
                    break;
                case "rejoin":
                    rejoinMatch();
                    break;
            }

        } catch (IOException e) {
            System.err.println("Error during the choice between Create and Join! Application will now close. ");
            System.exit(0);
        }
    }

    private void joinMatch() {
        try {
            System.out.println("Insert Nickname");
            String nickname = stdIn.readLine();
            outputStream.writeObject(new JoinMatchAction(nickname));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void rejoinMatch() {
        //client receives a message saying: "What's the ID of the game you want to rejoin?"
        //need to test when client inserts a string and not an int
        try {
            String id = stdIn.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void createMatch(){
        int gameSize=0;
        String nickname="";
        try {
            do {
                System.out.println("How many players do you want this game to have? [1-4] ");
                gameSize = Integer.parseInt(stdIn.readLine());
                if(gameSize<1 || gameSize>4) System.out.println("Please select a number between 1 and 4");
            }while (gameSize<1 || gameSize>4);
            do {
                System.out.println("Select a nickname: ");
                nickname = stdIn.readLine();
                if(nickname==null || nickname.equals(""))   System.out.println("Your nickname is invalid");
            }while (nickname==null || nickname.equals(""));

        //TODO: Json file
        ArrayList<String> jsonSetting= new ArrayList<>();
        CreateMatchAction createMatchAction= new CreateMatchAction(gameSize, nickname, "JSON");
            outputStream.writeObject(createMatchAction);
        } catch (NumberFormatException e) {
            System.out.println("You must insert a number!!!");
            createMatch();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method send sends a new message to the server
     *
     */
    public void send(Action action) {
        try {
            outputStream.reset();
            outputStream.writeObject(action);
            outputStream.flush();
            System.out.println("we've sent the action to the server");
        } catch (IOException e) {
            System.err.println("Error during send process.");
            System.err.println(e.getMessage());
        }
    }

    public void setGameID(int gameID) {
        this.gameID = gameID;
    }

    public void sout(String line){
        System.out.println(line);
    }
}
