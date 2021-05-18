package it.polimi.ingsw.Communication.server;

import it.polimi.ingsw.Communication.client.actions.Action;
import it.polimi.ingsw.Communication.client.actions.InitializationActions.BonusResourcesAction;
import it.polimi.ingsw.Communication.client.actions.mainActions.DevelopmentAction;
import it.polimi.ingsw.Communication.client.actions.mainActions.MarketAction;
import it.polimi.ingsw.Communication.client.actions.mainActions.WhiteToColorAction;
import it.polimi.ingsw.Communication.client.actions.mainActions.productionActions.BaseProductionAction;
import it.polimi.ingsw.Communication.client.actions.mainActions.productionActions.DevelopmentProductionAction;
import it.polimi.ingsw.Communication.client.actions.mainActions.productionActions.LeaderProductionAction;
import it.polimi.ingsw.Communication.client.actions.secondaryActions.ActivateLeaderCardAction;
import it.polimi.ingsw.Communication.client.actions.secondaryActions.DiscardExcedingDepotAction;
import it.polimi.ingsw.Communication.client.actions.secondaryActions.DiscardLeaderCard;
import it.polimi.ingsw.Communication.client.actions.secondaryActions.ViewDashboardAction;
import it.polimi.ingsw.Communication.server.messages.*;
import it.polimi.ingsw.Communication.server.messages.ConnectionRelatedMessages.DisconnectionMessage;
import it.polimi.ingsw.Communication.server.messages.ConnectionRelatedMessages.RejoinAckMessage;
import it.polimi.ingsw.Communication.server.messages.GameCreationPhaseMessages.GameStartingMessage;
import it.polimi.ingsw.Communication.server.messages.InitializationMessages.GameInitializationFinishedMessage;
import it.polimi.ingsw.Communication.server.messages.InitializationMessages.InitializationMessage;
import it.polimi.ingsw.Communication.server.messages.JsonMessages.DashboardMessage;
import it.polimi.ingsw.Communication.server.messages.JsonMessages.GameBoardMessage;
import it.polimi.ingsw.Communication.server.messages.Notificatios.MarketNotification;
import it.polimi.ingsw.Exceptions.*;
import it.polimi.ingsw.Exceptions.WarehouseErrors.FourthDepotWarehouseError;
import it.polimi.ingsw.Exceptions.WarehouseErrors.TooManyResourcesInADepot;
import it.polimi.ingsw.Model.Game;
import it.polimi.ingsw.Model.boardsAndPlayer.Player;
import it.polimi.ingsw.Model.leadercard.LeaderCard;
import it.polimi.ingsw.Model.market.OutOfBoundException;
import it.polimi.ingsw.Model.resource.*;
import it.polimi.ingsw.Exceptions.WarehouseErrors.WarehouseDepotsRegularityError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GameHandler {
    /** Server that contains this GameHandler */
    private final Server server;

    /** Unique game associated to this GameHandler. It contains the model that will be modified by the players connected */
    private Game game;

    /** Boolean is true if the game has started, false if it is still in lobby */
    private boolean isStarted;

    /** Contains the number of players allowed to play this game, decided by the game creator */
    private int totalPlayers;

    /** List of the clientsID CONNECTED to the game */
    private final ArrayList<Integer> clientsIDs;

    /** Unique identifier of the game associated to this gameHandler */
    private final int gameID;

    /** Unique socket related to the host of the game */
    private ServerSideSocket hostConnection;

    /** List of the nicknames of the players who joined the game */
    private final ArrayList<String> clientsNicknames;

    /** List of the connection of the player connected to the game*/
    private final ArrayList<ServerSideSocket> clientsInGameConnections;

    /**
     * This hashmap permits identifying a nickname relying on his game order
     */
    private final Map<Integer, String> orderToNickname;

    /**
     * This hashmap permits identifying a ServerSideSocket relying on his clientID
     * The client has to be connected to the game and to the server.
     */
    private final Map<Integer, ServerSideSocket> clientIDToConnection;

    /**
     * This hashmap permits identifying a nickname relying on his clientID
     * The client has to be connected to the game and to the server.
     */
    private final Map<Integer, String> clientIDToNickname;

    /**
     * This hashmap permits identifying a clientID relying on his nickname
     * If the player related to that nickname is disconnected, the value is null
     */
    private final Map<String,Integer> nicknameToClientID;

    /**
     * This hashmap permits identifying the gamePhase of a player relying on his nickname
     * If the player related to that nickname is disconnected, the value is null
     */
    private final Map<String,Integer> nicknameToHisGamePhase;

    private final Map<String,Integer> nicknameToOrder;

    private int gamePhase=0;

    private int numOfInitializedClients=0;

    private final Turn turn;



    /**
     * Constructor GameHandler creates a new GameHandler instance.
     *
     * @param server of type Server - the main server class.
     */
    public GameHandler(Server server, int totalPlayers) {
        this.server = server;
        this.totalPlayers = totalPlayers;
        isStarted = false;
        clientsIDs = new ArrayList<>();
        clientsInGameConnections = new ArrayList<>();
        orderToNickname = new HashMap<>();
        clientIDToConnection = new HashMap<>();
        clientIDToNickname = new HashMap<>();
        nicknameToClientID = new HashMap<>();
        nicknameToOrder= new HashMap<>();
        clientsNicknames = new ArrayList<>();
        nicknameToHisGamePhase = new HashMap<>();
        gameID = generateNewGameID();
        turn= new Turn();
    }

    /**
     * Method used when a new player connects to the gameHandler, firstly adds the player's attributes to the gameHandler (by
     * using method @addNewPlayer) then checks if the new player was the last one required to full the match or not. If he was,
     * method notifies all the player that the match is starting; else notifies all the player that a new player has connected
     *
     * @param clientID int provided by the server that identifies uniquely a client
     * @param connection socket used by that client
     * @param nickname string used to identify a player in the room (players in the same room can't have the same name)
     * @throws InterruptedException when TimeUnit throws it
     */
    public void lobby(int clientID, ServerSideSocket connection, String nickname) throws InterruptedException {
        //gameHandler is updated with the new client values
        addNewPlayer(clientID,connection, nickname);

        //case game is full, match is ready to start and all the players are notified of the event
        if(clientsInGameConnections.size()==totalPlayers){
            System.err.println("Number of players required for the gameID=" +gameID+" reached. The match is starting.");
            for (int i = 3; i > 0; i--) {
                TimeUnit.MILLISECONDS.sleep(500);
                sendAll(new GenericMessage("Match starting in " + i));
            }
            sendAll(new GameStartingMessage());
            setup();
        }

        //room is not full yet, all the player are notified that there is one less empty spot in the room
        else {
            sendAll(new GenericMessage((totalPlayers - clientsInGameConnections.size()) + " slots left."));
        }
    }

    /**
     * @return the gameID generated by the server for this game
     */
    public int generateNewGameID(){
        return server.createGameID();
    }


    /**
     * Method used to send a message to all the clients connected to the game
     * @param message contains the message to send
     */
    public void sendAll(Message message){
        for(int clientId: clientsIDs){
            sendMessage(message,clientId);
        }
    }

    public void sendAllExceptActivePlayer(Message message){
        for(int clientId: clientsIDs){
            if(!(game.getActivePlayer().getNickname()).equals(clientIDToNickname.get(clientId)))    sendMessage(message,clientId);
        }
    }

    /**
     * @return gameID
     */
    public int getGameID() {
        return gameID;
    }

    /**
     * Method isStarted returns true if the game has started (the started attribute becomes true after the challenger
     * selection phase).
     */
    public boolean isStarted() {
        return isStarted;
    }

    /**
     * Method used to set a connection as the connection of the host of the game
     * @param serverSideSocket is the connection to set as host
     */
    public void setHost(ServerSideSocket serverSideSocket){
        this.hostConnection = serverSideSocket;
    }

    /**
     * Method sends a message to every client connected to the game except one
     * @param s is the message to send
     * @param clientID the ID of the player not to send the message of
     */
    private void sendAllExcept(Message s, int clientID) {
        for (int id:clientsIDs) {
            if(id!=clientID) sendMessage(s,id);
        }
    }


    /**
     * Method used to send a message to a client relying on his ID
     * @param message is the message to send
     * @param id is the clientID of the player the method must send the message of
     */
    public void sendMessage(Message message, int id){
        clientIDToConnection.get(id).sendSocketMessage(message);
    }


    /**
     * Method setup handles the preliminary actions
     */
    public void setup() {
        //Since the game has started, we must update the lists of the server
        server.getMatchesInLobby().remove(this);
        server.getMatchesInGame().add(this);
        //With this command we create a game class and its model
        game = new Game(clientsInGameConnections, gameID);

        for(int i = 0; i< game.getPlayers().size(); i++){
            nicknameToOrder.put(game.getPlayers().get(i).getNickname(), i+1);
            orderToNickname.put(i+1, game.getPlayers().get(i).getNickname());
        }

        for (String nickname:clientsNicknames) {
            //We set each player to a new phase of the game (initialization phase)
            nicknameToHisGamePhase.replace(nickname,1);
        }

        gamePhase++;
        for (int id: clientsIDs) {
            InitializationMessage messageToSend = new InitializationMessage(clientIDToConnection.get(id).getOrder(),
                    game.getGameBoard().getPlayerFromNickname(clientIDToNickname.get(id)).getLeaderCardZone().getLeaderCards());
            sendMessage(messageToSend, id);
        }

      //  sendAll(new OrderMessage(game));
        if(!isStarted) isStarted=true;
    }


    /**
     * @return the server class.
     */
    public Server getServer() {
        return server;
    }


    /**
     * Method used when a new player connects to the room, so the gameHandler saves his attributes locally and assigns
     * the order in the game to the new player
     *
     * @param clientID int provided by the server that identifies uniquely a client
     * @param clientSingleConnection socket used by that client
     * @param nickname string used to identify a player in the room (players in the same room can't have the same name)
     */
    public void addNewPlayer(int clientID, ServerSideSocket clientSingleConnection, String nickname){
        //the new player's client ID is added to the list
        clientsIDs.add(clientID);

        //the new player's nickname is added to the list
        clientsNicknames.add(nickname);

        //the player's connection is added to list
        clientsInGameConnections.add(clientSingleConnection);

        //updating maps with new player's values
        nicknameToHisGamePhase.put(nickname,0);
        clientIDToNickname.put(clientID,nickname);
        nicknameToClientID.put(nickname,clientID);
        clientIDToConnection.put(clientID,clientSingleConnection);

        //sending a message notifying that a new player has joined the lobby to all the players already in lobby
        sendAllExcept(new GenericMessage("Player "+ nickname+" joined the game"), clientID);
        System.err.println("Player "+nickname+" joined gameID="+gameID);
    }


    /**
     * Method used to remove an ID from clientsIDs
     * @param idToRemove is the ID to remove from the list
     */
    public void removeID(int idToRemove){
        for(int i=0;i<clientsIDs.size();i++){
            if(clientsIDs.get(i)==idToRemove){
                clientsIDs.remove(i);
                return;
            }
        }
    }


    /**
     * Method used to remove an ID from clientsIDs connection from clientsInGameConnections
     * @param connectionToRemove is the connection to remove from the list
     */
    public void removeConnection(ServerSideSocket connectionToRemove){
        for(int i=0;i<clientsInGameConnections.size();i++){
            if(clientsInGameConnections.get(i)==connectionToRemove){
                clientsInGameConnections.remove(i);
                return;
            }
        }
    }


    /**
     * Method unregisterPlayer unregisters a player identified by his unique ID after a disconnection event or message.
     * @param id is the ID of the player to disconnect
     */
    public void unregisterPlayer(int id) {

        //All the lists and maps are updated removing the client who disconnected

        removeID(id);

        //If the room is empty, game ends
        if(clientsIDs.size()==0){
            System.out.println("Not anymore players");
            removeGameHandler();
        }

        sendAll(new DisconnectionMessage(clientIDToNickname.get(id)));
        nicknameToClientID.replace(clientIDToNickname.get(id),null);

        if(gamePhase==1 && nicknameToHisGamePhase.get(clientIDToNickname.get(id))==2){
            numOfInitializedClients--;
        }

        clientIDToNickname.remove(id);
        removeConnection(clientIDToConnection.get(id));
        clientIDToNickname.remove(id);

        ServerSideSocket connectionToRemove = clientIDToConnection.get(id);

        //If the player was the host, another player is set as new host.
        if(connectionToRemove.isHost()){
            setHost(clientIDToConnection.get(clientsIDs.get(0)));
            hostConnection.setHost(true);
            sendAll(new GenericMessage(clientIDToNickname.get(clientsIDs.get(0)) + " is the new host."));
        }

        //the player was the active player
        if(connectionToRemove.equals(game.getActivePlayer())){
            game.nextTurn();
        }

        game.removeConnection(connectionToRemove);
        clientIDToConnection.remove(id);
    }

    private void removeGameHandler() {
    }

    public void sendMessageToActivePlayer(GenericMessage message){
        sendMessage(message,getGame().getActivePlayer().getClientID() );
    }

    public void endGame() {
    }


    /**
     * Method used to update the nickname of the player who was previously disconnected with all the new attributes related to it
     * (since the same player reconnected using a different ServerSideSocket and a different clientID)
     *
     * @param newServerSideSocket of type ServerSideSocket: the new ServerSideSocket of the player reconnected
     * @param nickname of type String: the nickname of the player who has reconnected
     */
    public void reconnectPlayer(ServerSideSocket newServerSideSocket, String nickname) {
        clientsIDs.add(newServerSideSocket.getClientID());
        clientsInGameConnections.add(newServerSideSocket);
        clientIDToConnection.put(newServerSideSocket.getClientID(),newServerSideSocket);
        clientIDToNickname.put(newServerSideSocket.getClientID(),nickname);
        nicknameToClientID.replace(nickname,newServerSideSocket.getClientID());


        sendMessage(new RejoinAckMessage(nicknameToHisGamePhase.get(nickname)),newServerSideSocket.getClientID());

        int order= nicknameToOrder.get(nickname);
        newServerSideSocket.setOrder(order);

        //TODO: we will have to add the other messages for the next game phases
        switch (nicknameToHisGamePhase.get(nickname)){
            case 1:
                sendMessage(new GameStartingMessage(),newServerSideSocket.getClientID());
                sendMessage(new InitializationMessage(newServerSideSocket.getOrder(), game.getGameBoard().getPlayerFromNickname(nickname).getLeaderCardZone().getLeaderCards()),
                        newServerSideSocket.getClientID());
                break;
            case 2: sendMessage(new GenericMessage("You were in effective game phase, you will be able to make your moves " +
                    "once it is your turn"),newServerSideSocket.getClientID());
            default: break;
        }

        sendAllExcept(new GenericMessage("Player "+nickname+" has reconnected to the game"),newServerSideSocket.getClientID());
    }

    /**
     *
     * @param nickname of type String: name to look for
     * @return true if there is already a player with the same name as the parameter nickname
     */
    public boolean isNicknameAlreadyTaken(String nickname){
        return clientsNicknames.contains(nickname);
    }

    /**
     * @return number of totalPlayer set by the host at the match creation
     */
    public int getTotalPlayers() {
        return totalPlayers;
    }

    /**
     * Method used to get resources from market. Sets actionPerformed in turn to 1 if all goes well.
     * @param action: see {@link MarketAction}
     */
    public void marketAction(MarketAction action, String nickname){
        Player player = game.getGameBoard().getPlayerFromNickname(nickname);
        try {
            player.getResourcesFromMarket(getGame().getGameBoard(), action.isRow(), action.getIndex());
            turn.setActionPerformed(1);
        } catch (OutOfBoundException e) {
            e.printStackTrace();
        } catch (WarehouseDepotsRegularityError e){
            //TODO: server must wait for the user to delete exceeding depot/resources to end his turn
            if(e instanceof FourthDepotWarehouseError){
                turn.setActionPerformed(3);
                sendMessage(new GenericMessage("There's a fourth depot in the warehouse, you must delete one")
                        ,nicknameToClientID.get(nickname));
                sendMessageToActivePlayer(new GenericMessage("You now have to perform a delete action [e.g. deletedepot 4]"));
            }
            else if(e instanceof TooManyResourcesInADepot){
                turn.setActionPerformed(4);
                sendMessage(new GenericMessage("There's an exceeding amount of resources in one depot in the warehouse," +
                                " you must delete one"),nicknameToClientID.get(nickname));
            }
        }
        sendAllExceptActivePlayer(new MarketNotification(action.getIndex(), action.isRow(),player.getNickname()));
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sendAllExceptActivePlayer(new GenericMessage(game.getGameBoard().getMarket().getStringMarket()));
    }

    /**
     * Method used to buy the player the requested development card. Sets actionPerformed in turn to 1 if all goes well.
     * @param action: see {@link DevelopmentAction}
     */
    public boolean developmentAction (DevelopmentAction action, Player player){
        try {
            player.buyDevelopmentCard(action.getColor(), action.getCardLevel(), action.getIndex(), this.game.getGameBoard());
            turn.setActionPerformed(1);
            sendMessage(new GenericMessage("you've correctly bought the card!"),game.getActivePlayer().getClientID());
            return true;
        } catch (NotCoherentLevelException e) {
            sendMessage(new GenericMessage("You cant buy a card of that level in that developmentCardZone"),game.getActivePlayer().getClientID());
        }
        catch(NotEnoughResourcesException e){
            e.printStackTrace();
            sendMessage(new GenericMessage("you dont have enough resources to buy the card"),game.getActivePlayer().getClientID());
        }
        return false;
    }

    /**
     *
     */
    public void productionAction(Action action, String nickname){
        Player player = game.getGameBoard().getPlayerFromNickname(nickname);
        boolean productionMade = false;
        boolean[] productions= turn.getProductions();


        if (action instanceof BaseProductionAction) {

            //CORRECT PATH: USER DIDN'T ACTIVATE BASE PRODUCTION IN THIS TURN
            if(!productions[0]){
                if (baseProduction((BaseProductionAction) action, nickname)) {
                    productionMade=true;
                    sendMessage(new GenericMessage("Base production activated successfully")
                            , nicknameToClientID.get(nickname));
                }
            }

            //WRONG PATH: USER ALREADY ACTIVATED BASE PRODUCTION IN THIS TURN
            else {
                sendMessage(new GenericMessage("You already used base production in this turn, please try something else")
                        ,nicknameToClientID.get(nickname));
            }
        }

        else if (action instanceof LeaderProductionAction){
            int leaderCardZoneIndex= ((LeaderProductionAction) action).getLeaderCardZoneIndex();

            //CORRECT PATH: USER DIDN'T ACTIVATE THE LEADER CARD PRODUCTION OF THE SELECTED CARD IN THIS TURN
            if (!productions[leaderCardZoneIndex]){
                if(leaderProduction((LeaderProductionAction) action, nickname)) {
                    productionMade=true;
                    sendMessage(new GenericMessage("Production from leader card n. "+leaderCardZoneIndex+
                            " has been made successfully"), nicknameToClientID.get(nickname));
                }
            }

            //WRONG PATH: USER ASKED FOR A PRODUCTION HE ALREADY ACTIVATED IN THIS TURN
            else sendMessage(new GenericMessage("You already activated this production in this turn"),
                    nicknameToClientID.get(nickname));
        }

        else if (action instanceof DevelopmentProductionAction){
            int devCardZoneIndex= ((DevelopmentProductionAction) action).getDevelopmentCardZone();

            //CORRECT PATH: USER ASKED FOR A PRODUCTION HE DIDN'T ACTIVATE IN THIS TURN YET
            if (!productions[devCardZoneIndex + 2]){

                //CORRECT PATH: USER HAS GOT ENOUGH RESOURCES TO ACTIVATE THE PRODUCTION
                if(devCardProduction(devCardZoneIndex, player)) {
                    productionMade=true;
                    sendMessage(new GenericMessage("Production from development zone "+devCardZoneIndex+
                                    " has been made successfully"), nicknameToClientID.get(nickname));
                }

                //WRONG PATH: USER HASN'T GOT ENOUGH RESOURCES TO ACTIVATE THE PRODUCTION
                else sendMessage(new GenericMessage("You don't have enough resources to activate this production"),
                        nicknameToClientID.get(nickname));
            }

            //WRONG PATH: USER ALREADY ACTIVATED THIS LEADER CARD PRODUCTION IN THIS TURN
            else sendMessage(new GenericMessage("You already activated this production in this turn"),
                    nicknameToClientID.get(nickname));
        }

        //IF THE PRODUCTION HAS BEEN ACTIVATED WITHOUT ERRORS, SERVER SENDS CLIENT AN TEMPORARY VERSION OF THE DEPOTS
        //AND OF THE RESOURCES PRODUCED
        if(productionMade){
            sendMessage(new GenericMessage("Resources available for more productions: "
                    +parseListOfResources(player.getDashboard().getResourcesUsableForProd())),nicknameToClientID.get(nickname));
            sendMessage(new GenericMessage("Resources produced: "
                    +parseListOfResources(player.getDashboard().getResourcesProduced())),nicknameToClientID.get(nickname));
            turn.setActionPerformed(2);
        }
    }

    /**
     * Called when the client selects a BaseProductionAction
     * @param action: see {@link BaseProductionAction}
     * @return  true if the action has been performed correctly, false otherwise
     */
    public boolean baseProduction(BaseProductionAction action,String nickname){
        ArrayList<Resource> used = new ArrayList<>();
        for(ResourceType resourceEnum: action.getResourcesToUse()){
            used.add(parseResourceFromEnum(resourceEnum));
        }
        ArrayList<Resource> created = new ArrayList<>();
        for(ResourceType resourceEnum: action.getResourcesWanted()){
            created.add(parseResourceFromEnum(resourceEnum));
        }
        int resultOfActivation = game.getGameBoard().getPlayerFromNickname(nickname).activateBaseProduction(used, created);
        switch (resultOfActivation){
            case 0: //CASE ACTIVATE WORKED PERFECTLY
                return true;
            case 1: //CASE PLAYER DIDN'T HAVE ENOUGH RESOURCES TO ACTIVATE PROD
                sendMessage(new GenericMessage("You don't have enough of the selected resources to activate the base prod. "
                +"Please try using different resources or try activating another type of production"),nicknameToClientID.get(nickname));
                return false;
            case 2: //CASE PLAYER DIDN'T SELECT A CORRECT AMOUNT OF RESOURCES
                sendMessage(new GenericMessage("You insert an incorrect amount of resources, you must select "
                +game.getGameBoard().getPlayerFromNickname(nickname).getDashboard().getNumOfStandardProdRequirements()+
                        " resources to use and "+game.getGameBoard().getPlayerFromNickname(nickname).getDashboard()
                        .getNumOfStandardProdResults()+" resources to produce!"),nicknameToClientID.get(nickname));
                return false;
            default: return false;
        }
    }

    /**
     * Called when the client selects a LeaderProductionAction
     * @param action: see {@link LeaderProductionAction}
     * @return  true if the action has been performed correctly, false otherwise
     */
    public boolean leaderProduction(LeaderProductionAction action, String nickname){
        Player player = game.getGameBoard().getPlayerFromNickname(nickname);
        Resource resourceWanted = parseResourceFromEnum(action.getResourceWanted());
        int index= action.getLeaderCardZoneIndex();
        try {
            player.checkLeaderProduction(index);
            player.getDashboard().leaderProd(index,resourceWanted);
            turn.setProductionPerformed(index);
            return true;
        } catch (LeaderCardNotActiveException e) {
            sendMessage(new GenericMessage("The card you selected is not active")
                    ,nicknameToClientID.get(nickname));
            return false;
        } catch (WrongTypeOfLeaderPowerException e) {
            sendMessage(new GenericMessage("The card you selected is not a production card, please try again")
                    ,nicknameToClientID.get(nickname));
            return false;
        } catch (NotEnoughResourcesToActivateProductionException e) {
            sendMessage(new GenericMessage("You don't have enough resources to activate this production")
                    ,nicknameToClientID.get(nickname));
            return false;
        }
    }

    /**
     * Called when the client selects a DevelopmentProductionAction
     * @param index: represents the development card zone that contains the wanted production
     * @return  true if the action has been performed correctly, false otherwise
     */
    public boolean devCardProduction(int index, Player player){
        try {
            player.activateDevelopmentProduction(index);
            turn.setProductionPerformed(2+index);
        } catch (NotEnoughResourcesToActivateProductionException e) {
            return false;
        }
        return true;
    }

    /**
     * Used when the player wants to activate a leader card. This method can get called anytime during the turn, before or after doing a main action, and doesn't
     * influence in any way the player's ability to perform any other action.
     * @param action: see {@link ActivateLeaderCardAction}
     */
    public void activateLeaderCard(Action action, Player player){
        int index= ((ActivateLeaderCardAction) action).getIndex();
        try {
            player.activateLeaderCard(index);
        } catch (NotInactiveException e) {
            e.printStackTrace();
            game.getActivePlayer().sendSocketMessage(new GenericMessage("The leader card you selected is not inactive!"));
        } catch (RequirementsUnfulfilledException e) {
            game.getActivePlayer().sendSocketMessage(new GenericMessage("You dont have the requirements to activate this leader card"));
            e.printStackTrace();
        }
    }

    /**
     * Used when the player wants to take at a dashboard. This method can get called anytime during the turn, before or after doing a main action, and doesn't
     * influence in any way the player's ability to perform any other action.
     * @param action: see {@link ActivateLeaderCardAction}
     */
    public void viewDashboard(Action action){
        System.out.println("we've received a dashboard request");
        int order= ((ViewDashboardAction) action).getPlayerID();
        if(order<1 || order>totalPlayers){
            game.getActivePlayer().sendSocketMessage(new GenericMessage("There's no player associated to the index you insert"));
        }
        else {
            Message dashboardAnswer = new DashboardMessage(game.getGameBoard().getPlayerFromNickname(orderToNickname.get(order)).getDashboard());
            game.getActivePlayer().sendSocketMessage(dashboardAnswer);
        }
        System.out.println("we've sent the dashboard back to the client");
    }

    public void viewGameBoard() {
        System.out.println("we've received a gameBoard request");
        Message gameBoardAnswer = new GameBoardMessage(game.getGameBoard());
        System.out.println("we've created a gameBoard answer");
        game.getActivePlayer().sendSocketMessage(gameBoardAnswer);
        System.out.println("we've sent it to client");
    }

    public Game getGame() {
        return game;
    }

    /**
     * @return map containing ServerSideSocket related to their clientID
     */
    public Map<Integer, ServerSideSocket> getClientIDToConnection() {
        return clientIDToConnection;
    }

    /**
     * @return map containing ClientID related to their nickname
     */
    public Map<String, Integer> getNicknameToClientID() {
        return nicknameToClientID;
    }

    /**
     * @return true if all the players are online and connected to the game
     */
    public boolean allThePlayersAreConnected() {
        return totalPlayers == clientsInGameConnections.size();
    }

    /**
     * @return: {@link Turn}
     */
    public Turn getTurn() {
        return turn;
    }

    /**
     * @param resourceEnum: type of resource
     * @return: instance of a resource of the type selected
     */
    public Resource parseResourceFromEnum(ResourceType resourceEnum){
        switch (resourceEnum){
            case Coin: return new CoinResource();
            case Stone: return new StoneResource();
            case Servant: return new ServantResource();
            case Shield: return new ShieldResource();
        }
        return null;
    }

    public String parseListOfResources(ArrayList<Resource> list){
        StringBuilder string = new StringBuilder();
        for (Resource resource:list) {
            string.append(parseTypeFromResource(resource));
        }
        return string.toString();
    }

    public String parseTypeFromResource(Resource resourceToParse){
        if(resourceToParse instanceof CoinResource) return "coin ";
        if(resourceToParse instanceof StoneResource) return "stone ";
        if(resourceToParse instanceof ShieldResource) return "shield ";
        else return "servant ";
    }

    public void endTurn() {
        //case the turn can end
        if(turn.getActionPerformed()!=0){
            turn.resetProductions();
            turn.setActionPerformed(0);
            game.nextTurn();
        }
        else{
            sendMessage(new GenericMessage("You can't end your turn until you make a main action"), game.getActivePlayer().getClientID());
        }

    }

    public void newInitialization(String nickname) {
        numOfInitializedClients++;
        nicknameToHisGamePhase.replace(nickname,2);
        if(numOfInitializedClients==clientsInGameConnections.size()){
            isStarted=true;
            gamePhase++;
            sendAll(new GameInitializationFinishedMessage());
            sendAll(new GenericMessage("It's "+game.getActivePlayer().getNickname()+"'s turn"));
        }
    }
    public void marketSpecialAction(WhiteToColorAction message, Player player){
        for(int i=0;i<message.getResourceTypes().size();i++){
            player.getDashboard().getWarehouse().addResource(parseResourceFromEnum(message.getResourceTypes().get(i)));}
    }

    public void startingResources(BonusResourcesAction action, Player player){
        ResourceType resourceType= action.getResourceType1();
        Resource resource= parseResourceFromEnum(resourceType);
        System.out.println("Il player ha: "+ player.getDashboard().getWarehouse().amountOfResource(resource));
        if(action.getResourceType1()!=null) player.getDashboard().getWarehouse().addResource(parseResourceFromEnum(action.getResourceType1()));
        if(action.getResourceType2()!=null) player.getDashboard().getWarehouse().addResource(parseResourceFromEnum(action.getResourceType2()));
        System.out.println("Il player ha: "+ player.getDashboard().getWarehouse().amountOfResource(resource));
    }

    public boolean twoWhiteToColorCheck(Player player) {
        return player.getDashboard().getWhiteToColorResources() != null && player.getDashboard().getWhiteToColorResources().size() == 2;
    }

    public void test(Player player) {
        for (LeaderCard card:player.getLeaderCardZone().getLeaderCards()) {
            card.activateCardPower(player.getDashboard());
        }
        if(player.getDashboard().getWhiteToColorResources()!=null && player.getDashboard().getWhiteToColorResources().size()==2) System.out.println("Activated 2 wtc leaders");
        if(player.getDashboard().getResourcesForExtraProd()!=null && player.getDashboard().getResourcesForExtraProd().size()==2) System.out.println("Activated 2 extraProd leaders");
        if(player.getDashboard().getDiscountedResources()!=null && player.getDashboard().getDiscountedResources().size()==2) System.out.println("Activated 2 discount leaders");
        if(player.getDashboard().getExtraDepots()!=null && player.getDashboard().getExtraDepots().size()==2) System.out.println("Activated 2 depot leaders");
    }

    public void printMarket() {
        game.getGameBoard().getMarket().printMarket();
    }

    public void addInfiniteResources() {
        for(int i=0;i<5;i++){
            game.getGameBoard().getPlayerFromNickname(game.getActivePlayer().getNickname()).getDashboard().getStrongbox().addResource(new CoinResource());
            game.getGameBoard().getPlayerFromNickname(game.getActivePlayer().getNickname()).getDashboard().getStrongbox().addResource(new StoneResource());
            game.getGameBoard().getPlayerFromNickname(game.getActivePlayer().getNickname()).getDashboard().getStrongbox().addResource(new ShieldResource());
            game.getGameBoard().getPlayerFromNickname(game.getActivePlayer().getNickname()).getDashboard().getStrongbox().addResource(new ServantResource());
        }
    }

    public void discardLeaderCard(DiscardLeaderCard action, String nickname) {
        Player player = game.getGameBoard().getPlayerFromNickname(nickname);
        int index = action.getIndex();
        if(player.getLeaderCardZone().getLeaderCards()==null || player.getLeaderCardZone().getLeaderCards().size()<index+1){
            sendMessage(new GenericMessage("There's no card at the index you insert"),nicknameToClientID.get(nickname));
        }
        else {
            player.getLeaderCardZone().getLeaderCards().remove(index);
            player.getDashboard().getPapalPath().moveForward();
            sendMessage(new GenericMessage("You have successfully removed card at index "+index),nicknameToClientID.get(nickname));
            if(index==0 && player.getLeaderCardZone().getLeaderCards().size()>0){
                sendMessage(new GenericMessage("Now card at index 0 is the card that previously was at index 1"),nicknameToClientID.get(nickname));
            }
        }
    }


    public void discardDepot(DiscardExcedingDepotAction action, Player player) {
        try {
            player.getDashboard().getWarehouse().removeExceedingDepot(action.getIndex());
            turn.setActionPerformed(1);
        } catch (WarehouseDepotsRegularityError warehouseDepotsRegularityError) {
            sendMessageToActivePlayer(new GenericMessage("There was a problem, please check if you've written everything correctly and try again. (0<index<5)"));
        }
    }
}
