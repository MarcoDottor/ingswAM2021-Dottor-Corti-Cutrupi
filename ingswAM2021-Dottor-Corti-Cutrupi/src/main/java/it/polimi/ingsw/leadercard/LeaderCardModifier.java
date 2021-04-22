package it.polimi.ingsw.leadercard;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import it.polimi.ingsw.developmentcard.DevelopmentCardForJson;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

public class LeaderCardModifier {

    private ArrayList<LeaderCardForJson> listOfCards;

    public ArrayList<LeaderCardForJson> getListOfCards() {
        return listOfCards;
    }
    public LeaderCardModifier() {
        this.listOfCards = new ArrayList <LeaderCardForJson>();
    }

    /**this method imports all the leader cards from json into the arraylist of this class
     *
     * @throws FileNotFoundException
     */
    public void importCards() throws FileNotFoundException {
        //part where we import all the cards from json
        JsonReader reader = new JsonReader(new FileReader("C:\\Users\\Sam\\Desktop\\Progetto ingegneria del software\\LeaderCardsInstancing.json"));
        JsonParser parser = new JsonParser();
        JsonArray cardsArray = parser.parse(reader).getAsJsonArray();
        for(JsonElement jsonElement : cardsArray) {
            Gson gson = new Gson();
            LeaderCardForJson cardRecreated = gson.fromJson(jsonElement.getAsJsonObject(), LeaderCardForJson.class);
            this.listOfCards.add(cardRecreated);
        }
    }

    /**
     * this method changes the type of requirement of the card from Development Cards to Resources,
     * or the other way around.
     * @param cardIndex
     */
    public void changeRequirementType(int cardIndex){
        if (this.listOfCards.get(cardIndex).getTypeOfRequirement().equals("development")){
            this.listOfCards.get(cardIndex).setTypeOfRequirement("resources");
        }
        if(this.listOfCards.get(cardIndex).getTypeOfRequirement().equals("resources")){
            this.listOfCards.get(cardIndex).setTypeOfRequirement("development");
        }
    }


    /**
     * this method changes the resources needed to activate the leader power
     * @param cardIndex index of the card we want to make the operation on
     * @param requirementIndex
     * @param quantity
     * @param resource
     */
    public void changeResourcesRequirementOfCard(int cardIndex,int requirementIndex,int quantity, String resource){
        this.listOfCards.get(cardIndex).getAmountOfForResourcesRequirement().set(requirementIndex,quantity);
        this.listOfCards.get(cardIndex).getResourcesRequired().set(requirementIndex,resource);
    }

    /**
     * this method adds a requirement to the list of the resources requirements of the card
     * @param cardIndex index of the card we want to make the operation on
     * @param quantity
     * @param resource
     */
    public void addResourcesRequirementOfCard(int cardIndex,int quantity, String resource ) {
        this.listOfCards.get(cardIndex).getAmountOfForResourcesRequirement().add(quantity);
        this.listOfCards.get(cardIndex).getResourcesRequired().add(resource);
    }

    /**
     * this method removes a requirement from the list of the resources requirements of the card
     * @param cardIndex index of the card we want to make the operation on
     * @param requirementIndex
     */
    public void removeResourcesRequirementFromCard(int cardIndex,int requirementIndex){
        this.listOfCards.get(cardIndex).getAmountOfForResourcesRequirement().remove(requirementIndex);
        this.listOfCards.get(cardIndex).getResourcesRequired().remove(requirementIndex);
    }

    /**
     * this method changes a development requirement for activating the leader card
     * @param cardIndex index of the card we want to make the operation on
     * @param requirementIndex
     * @param newAmountOfDevelopmentRequired
     * @param newLevelRequired
     * @param newColorRequired
     */
    public void changeDevelopmentRequirementOfCard(int cardIndex,int requirementIndex, int newAmountOfDevelopmentRequired,int newLevelRequired,String newColorRequired){
        this.listOfCards.get(cardIndex).getAmountOfForDevelopmentRequirement().set(requirementIndex,newAmountOfDevelopmentRequired);
        this.listOfCards.get(cardIndex).getLevelsRequired().set(requirementIndex,newLevelRequired);
        this.listOfCards.get(cardIndex).getColorsRequired().set(requirementIndex,newColorRequired);
    }

    /**
     * this method adds a development requirement for activating the leader card
     * @param cardIndex index of the card we want to make the operation on
     * @param newAmountOfDevelopmentRequired
     * @param newLevelRequired
     * @param newColorRequired
     */
    public void addDevelopmentRequirementOfCard(int cardIndex,int newAmountOfDevelopmentRequired,int newLevelRequired,String newColorRequired){
        this.listOfCards.get(cardIndex).getAmountOfForDevelopmentRequirement().add(newAmountOfDevelopmentRequired);
        this.listOfCards.get(cardIndex).getLevelsRequired().add(newLevelRequired);
        this.listOfCards.get(cardIndex).getColorsRequired().add(newColorRequired);
    }

    /**
     * this method removes a development requirement for activating the leader card
     * @param cardIndex index of the card we want to make the operation on
     * @param requirementIndex index of the requirement we want to remove
     */
    public void removeDevelopmentRequirementOfCard(int cardIndex,int requirementIndex){
        this.listOfCards.get(cardIndex).getAmountOfForDevelopmentRequirement().remove(requirementIndex);
        this.listOfCards.get(cardIndex).getLevelsRequired().remove(requirementIndex);
        this.listOfCards.get(cardIndex).getColorsRequired().remove(requirementIndex);
    }


    /**this method changes the card victory points
     *
     * @param cardIndex index of the card we want to make the operation on
     * @param victoryPointsToSet
     */
    public void changeCardVictoryPoints(int cardIndex,int victoryPointsToSet) {
        this.listOfCards.get(cardIndex).setVictoryPoints(victoryPointsToSet);
    }

    /**this method changes the card special power type
     *
     * @param cardIndex
     * @param specialPowerToSet
     */
    public void changeCardSpecialPowerType(int cardIndex,String specialPowerToSet) {
        this.listOfCards.get(cardIndex).setSpecialPower(specialPowerToSet);
    }

    /**this method changes the card special power resource
     *
     * @param cardIndex
     * @param resourceForSpecialPowerToSet
     */
    public void changeCardSpecialPowerResource(int cardIndex,String resourceForSpecialPowerToSet) {
        this.listOfCards.get(cardIndex).setSpecialPowerResource(resourceForSpecialPowerToSet);
    }


    /**this method is to write the modified arraylist of cards into the json file
     *
     */
    public void writeCardsInJson(){
        //FOR NOW IT JUST PRINTS THE CARD TO SCREEN
        System.out.println("list of cards:");
        Gson listOfCardsGson = new GsonBuilder().setPrettyPrinting().create();
        String listJson = listOfCardsGson.toJson(this.listOfCards);
        System.out.println(listJson);
    }

}
