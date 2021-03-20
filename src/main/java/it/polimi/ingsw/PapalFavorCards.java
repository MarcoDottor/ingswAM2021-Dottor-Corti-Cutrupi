package it.polimi.ingsw;

public class PapalFavorCards {
    private int numberID;
    private PapalCardCondition condition;
    private int victoryPoints;

    //These cards are initialized when the rest of the path is, we only need their number (1st, 2nd, 3rd) and how many VP they're worth; at the start of the game their condition is the same

    public PapalFavorCards(int faithPosition, int victoryPoints) {
        this.numberID = faithPosition;
        this.condition = PapalCardCondition.Inactive;
        this.victoryPoints = victoryPoints;
    }

    public int getNumberID() {
        return numberID;
    }

    public PapalCardCondition getCondition() {
        return this.condition;
    }

    public int getVictoryPoints() {
        return victoryPoints;
    }

    public void setCondition(PapalCardCondition condition) {
        this.condition = condition;
    }

}
