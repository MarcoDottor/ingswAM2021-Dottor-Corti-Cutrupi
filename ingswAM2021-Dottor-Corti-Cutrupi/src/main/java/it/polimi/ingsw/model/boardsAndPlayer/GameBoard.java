package it.polimi.ingsw.model.boardsAndPlayer;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import it.polimi.ingsw.model.market.OutOfBoundException;
import it.polimi.ingsw.server.ServerSideSocket;
import it.polimi.ingsw.exception.BothPlayerAndLorenzoActivatePapalCardException;
import it.polimi.ingsw.exception.LorenzoActivatesPapalCardException;
import it.polimi.ingsw.exception.LorenzoWonTheMatch;
import it.polimi.ingsw.exception.PapalCardActivatedException;
import it.polimi.ingsw.exception.warehouseErrors.WarehouseDepotsRegularityError;
import it.polimi.ingsw.model.lorenzoIlMagnifico.LorenzoIlMagnifico;
import it.polimi.ingsw.model.developmentcard.Color;
import it.polimi.ingsw.model.developmentcard.DevelopmentCard;
import it.polimi.ingsw.model.developmentcard.DevelopmentCardDeck;
import it.polimi.ingsw.model.developmentcard.DevelopmentCardForJson;
import it.polimi.ingsw.model.leadercard.LeaderCardDeck;
import it.polimi.ingsw.model.lorenzoIlMagnifico.Token;
import it.polimi.ingsw.model.market.Market;
import it.polimi.ingsw.model.requirements.ResourcesRequirements;
import it.polimi.ingsw.model.requirements.ResourcesRequirementsForAcquisition;
import it.polimi.ingsw.model.resource.*;
import org.javatuples.Pair;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameBoard {

    private Market market;
    private DevelopmentCardDeck[][] developmentCardDecks;
    private transient LeaderCardDeck leaderCardDeck;
    private transient ArrayList <Player> players;
    private LorenzoIlMagnifico lorenzoIlMagnifico;
    private boolean singlePlayer = false;
    private String json1="ingswAM2021-Dottor-Corti-Cutrupi/src/main/resources/leadercardsparameters.json";
    private String json2="ingswAM2021-Dottor-Corti-Cutrupi/src/main/resources/leadercardsparameters.json";
    private String json3="ingswAM2021-Dottor-Corti-Cutrupi/src/main/resources/DevCardInstancing.json";

    public boolean isSinglePlayer() {
        return singlePlayer;
    }

    /**
     *constructor for the multiplayer gameplay
     */
    public GameBoard(ArrayList <ServerSideSocket> players){
        market= new Market();
        leaderCardDeck = new LeaderCardDeck();
        developmentCardDecks = new DevelopmentCardDeck[3][4];
        for(int row=0;row<3;row++){
            this.developmentCardDecks[row][0] = new DevelopmentCardDeck(Color.Green,3-row);
            this.developmentCardDecks[row][1] = new DevelopmentCardDeck(Color.Blue,3-row);
            this.developmentCardDecks[row][2] = new DevelopmentCardDeck(Color.Yellow,3-row);
            this.developmentCardDecks[row][3] = new DevelopmentCardDeck(Color.Purple,3-row);
        }
        decksInitializer();

        this.players = new ArrayList<Player>();
        for (ServerSideSocket player: players) {
            this.players.add(new Player(player.getNickname(),player.getOrder(),this));
        }

        //we import from json the number of leader cards given to each player
        JsonReader reader1 = null;
        try {
            reader1 = new JsonReader(new FileReader(json1));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        JsonParser parser1 = new JsonParser();
        JsonArray favorArray = parser1.parse(reader1).getAsJsonArray();
        Gson gson1 = new Gson();
        int[] arr = gson1.fromJson(favorArray, int[].class);

        for(Player playerToGiveCards: this.players){
            //System.out.println("we're about to give each player "+arr[0]+"cards");
            for(int i=0;i<arr[0];i++){
                playerToGiveCards.giveCard(leaderCardDeck.drawCard());
            }
        }
    }

    /**
     * constructor for the single player gameplay
      */
    public GameBoard(String nickname){
        singlePlayer = true;
        market = new Market();
        leaderCardDeck = new LeaderCardDeck();
        developmentCardDecks = new DevelopmentCardDeck[3][4];
        for(int row=0;row<3;row++){
            this.developmentCardDecks[row][0] = new DevelopmentCardDeck(Color.Green,3-row);
            this.developmentCardDecks[row][1] = new DevelopmentCardDeck(Color.Blue,3-row);
            this.developmentCardDecks[row][2] = new DevelopmentCardDeck(Color.Yellow,3-row);
            this.developmentCardDecks[row][3] = new DevelopmentCardDeck(Color.Purple,3-row);
        }

        decksInitializer();
        this.players = new ArrayList<Player>();
        this.players.add(new Player(nickname,this));
        //we import from json the number of leader cards given to each player
        JsonReader reader1 = null;
        try {
            reader1 = new JsonReader(new FileReader(json2));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        JsonParser parser1 = new JsonParser();
        JsonArray favorArray = parser1.parse(reader1).getAsJsonArray();
        Gson gson1 = new Gson();
        int[] arr = gson1.fromJson(favorArray, int[].class);

        for(Player playerToGiveCards: this.players){
            //System.out.println("we're about to give each player "+arr[0]+"cards");
            for(int i=0;i<arr[0];i++){
                playerToGiveCards.giveCard(leaderCardDeck.drawCard());
            }
        }
        //this command doesnt let the gameboard to be serialized into json
        this.lorenzoIlMagnifico = new LorenzoIlMagnifico(this);
        System.out.println("we've correctly created the single player gameboard");
    }

    public boolean checkGameIsEnded(){
        for(Player player: players){
            if(player.checkGameIsEnded()){
                return true;
            }
        }
        return false;
    }

    public LorenzoIlMagnifico getLorenzoIlMagnifico() {
        return lorenzoIlMagnifico;
    }

    /**
     * Returns the deck of the selected color and level from the gameBoard
     */
    public DevelopmentCardDeck getDeckOfChoice(Color color, int level){
        if(color.equals(Color.Green)) return developmentCardDecks[3-level][0];
        else if(color.equals(Color.Blue)) return developmentCardDecks[3-level][1];
        else if(color.equals(Color.Yellow)) return developmentCardDecks[3-level][2];
        else return developmentCardDecks[3-level][3];
    }

    public Player getPlayerFromNickname(String nickname){
        for(Player player: this.players){
            if (player.getNickname().equals(nickname)){
                return player;
            }
        }
        System.out.println("there was an error: we could not find a player with the requested nickname");
        return null;
    }


    public ArrayList<Player> getPlayers() {
        return players;
    }

    public DevelopmentCardDeck[][] getDevelopmentCardDecks() {
        return developmentCardDecks;
    }

    /**
     * Method used to instantiate cards from JSON file
     */
    public void decksInitializer() {

         // Calling the method to instance the leader cards
        leaderCardDeck.deckInitializer();
        leaderCardDeck.shuffle();

        //creating the method that instances all the development decks with the correct cards
        int i;
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(json3));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        JsonParser parser = new JsonParser();
        JsonArray cardsArray = parser.parse(reader).getAsJsonArray();
        for(JsonElement jsonElement : cardsArray){
            Gson gson = new Gson();
            DevelopmentCardForJson cardRecreated = gson.fromJson(jsonElement.getAsJsonObject(), DevelopmentCardForJson.class);

            i=0;
            //here we convert the card price
            List<ResourcesRequirementsForAcquisition> cardPrice = new ArrayList<ResourcesRequirementsForAcquisition>();
            for(Integer quantity: cardRecreated.getAmountOfForPrice()){
                Resource resourceForPrice;
                    if (cardRecreated.getTypeOfResourceForPrice().get(i).equals("coin")) {
                        resourceForPrice = new CoinResource();
                    }
                    else if (cardRecreated.getTypeOfResourceForPrice().get(i).equals("stone")) {
                        resourceForPrice = new StoneResource();
                    }
                    else if (cardRecreated.getTypeOfResourceForPrice().get(i).equals("shield")) {
                        resourceForPrice = new ShieldResource();
                    } else {
                        resourceForPrice = new ServantResource();
                    }

                ResourcesRequirementsForAcquisition requirement = new ResourcesRequirementsForAcquisition (quantity,resourceForPrice);
                cardPrice.add(requirement);
                i++;
            }



            // here we convert the card stats
            Color cardColor;
            if(cardRecreated.getColor().equals("blue")){
                cardColor= Color.Blue;
            }else if(cardRecreated.getColor().equals("purple")){
                cardColor= Color.Purple;
            }else if(cardRecreated.getColor().equals("green")){
                cardColor= Color.Green;
            }else {
                cardColor= Color.Yellow;
            }
            Pair <Integer,Color> cardStats = new Pair <Integer,Color>(cardRecreated.getLevel(),cardColor);

            //here we convert the prod Requirements
            List<ResourcesRequirements> prodRequirements = new ArrayList<ResourcesRequirements>();
            i=0;
            for(int quantity: cardRecreated.getAmountOfForProdRequirements()){
                Resource resourceForRequirements;
                if(cardRecreated.getTypeOfResourceForProdRequirements().get(i).equals("coin")){
                    resourceForRequirements = new CoinResource();
                }
                else if(cardRecreated.getTypeOfResourceForProdRequirements().get(i).equals("stone")){
                    resourceForRequirements = new StoneResource();
                }
                else if(cardRecreated.getTypeOfResourceForProdRequirements().get(i).equals("shield")){
                    resourceForRequirements = new ShieldResource();
                }
                else{
                    resourceForRequirements = new ServantResource();
                }
                ResourcesRequirements requirement = new ResourcesRequirements (quantity,resourceForRequirements);
                prodRequirements.add(requirement);
                i++;
            }


            //here we convert the prod results
            i=0;
            List<Resource> prodResults = new ArrayList<Resource>();
            for(Integer quantity: cardRecreated.getAmountOfForProdResults()){
                for(int n=0; n<quantity; n++) {
                    Resource resourceForResults;
                    if (cardRecreated.getTypeOfResourceForProdResults().get(i).equals("coin")) {
                        resourceForResults = new CoinResource();
                    } else if (cardRecreated.getTypeOfResourceForProdResults().get(i).equals("stone")) {
                        resourceForResults = new StoneResource();
                    } else if (cardRecreated.getTypeOfResourceForProdResults().get(i).equals("shield")) {
                        resourceForResults = new ShieldResource();
                    } else if (cardRecreated.getTypeOfResourceForProdResults().get(i).equals("faith")){
                        resourceForResults = new FaithResource();
                    } else {
                        resourceForResults = new ServantResource();
                    }
                    prodResults.add(resourceForResults);
                }
                i++;
            }

            DevelopmentCard cardToAdd = new DevelopmentCard(cardPrice,cardStats,prodRequirements,prodResults,cardRecreated.getVictoryPoints());
            //System.out.println(cardToAdd.toString());

            if(cardColor==Color.Green){
                developmentCardDecks[3-cardRecreated.getLevel()][0].addNewCard(cardToAdd);
            }
            if(cardColor==Color.Blue){
                developmentCardDecks[3-cardRecreated.getLevel()][1].addNewCard(cardToAdd);
            }
            if(cardColor==Color.Yellow){
                developmentCardDecks[3-cardRecreated.getLevel()][2].addNewCard(cardToAdd);
            }
            if(cardColor==Color.Purple){
                developmentCardDecks[3-cardRecreated.getLevel()][3].addNewCard(cardToAdd);
            }
        }
        for(int row=0;row<3;row++){
            for(int column=0;column<4;column++){
                developmentCardDecks[row][column].shuffle();
            }
        }
    }

    public DevelopmentCardDeck getDevelopmentCardDeck (int row, int column){
        return this.developmentCardDecks[row][column];
    }

    /**
     * notifies the gameHandler that Lorenzo won by discarding enough development cards
     * STILL HAS TO BE MADE
     */
    public void lorenzoDevelopmentWin(){
        //notifies the gameHandler that Lorenzo won by discarding enough development cards
        System.out.println("You lost, Lorenzo il Magnifico won");
    }

    public Market getMarket(){
        return market;
    }

    public void acquireResourcesFromMarket(boolean isRow, int index, Dashboard dashboard) throws WarehouseDepotsRegularityError, PapalCardActivatedException {
        market.acquireResourcesFromMarket(isRow, index, dashboard);
    }

    @Override
    public String toString() {
        return "GameBoard{" +
                "market=" + market +
                ", developmentCardDecks=" + Arrays.toString(developmentCardDecks) +
                ", leaderCardDeck=" + leaderCardDeck +
                ", players=" + players +
                ", lorenzoIlMagnifico=" + lorenzoIlMagnifico +
                ", singlePlayer=" + singlePlayer +
                '}';
    }

    public void endTurn(String nicìkname){
        getPlayerFromNickname(nicìkname).endTurn();
    }

    public Token playLorenzo() throws LorenzoWonTheMatch, LorenzoActivatesPapalCardException, BothPlayerAndLorenzoActivatePapalCardException {
        return lorenzoIlMagnifico.playTurn();
    }

    public int playersSize(){
        return players.size();
    }

    public int playerVictoryPoints(int index){
        int num=players.get(index).getVictoryPoints();
        return num;
    }

    public String playerName(int index){
        String nickname=players.get(index).getNickname();
        return nickname;
    }

    public int getFaith(int i) {
        return players.get(i).getFaith();
    }

    public String getNickname(int i) {
        return players.get(i).getNickname();
    }

    public ResourceType resourceTypeInMarket(int row, int column){
        return market.reresourceTypeInMarket(row,column);
    }

    public ResourceType floatingMarbleType() {
        return market.getFloatingMarble().getResourceType();
    }

    public String getStringMarket() {
        return market.getStringMarket();
    }

    public int deckSize(Color color, int level) {
        return getDeckOfChoice(color,level).deckSize();
    }

    public DevelopmentCard getFirstCardCopy(Color color, int level) {
        DevelopmentCard card=getDeckOfChoice(color,level).getFirstCard();
        return new DevelopmentCard(card.getCardPrice(), card.getCardStats(),card.getProdRequirements(), card.getProdResults(), card.getVictoryPoints());
    }

    public int checkNumOfBlank(boolean isRow, int index) throws OutOfBoundException {
        return market.checkNumOfBlank(isRow,index);
    }
}
