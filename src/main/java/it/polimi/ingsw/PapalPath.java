package it.polimi.ingsw;

import com.sun.tools.javac.util.Pair;

public class PapalPath {

    private int faithPosition;
    private PapalFavorCards[] cards;

    //constructor, inizializes 0 as the position, then inizializes the three papal favor cards
    public PapalPath() {
        this.faithPosition = 0;
        PapalFavorCards firstFavorCard = new PapalFavorCards(8,2);
        PapalFavorCards secondFavorCard = new PapalFavorCards(16,3);
        PapalFavorCards ThirdFavorCard = new PapalFavorCards(24,4);
    }

    public void endGame (){
        //chiamo l'end game del game handler

    }
    //immediatly after moving the method checks wheter a meeting with the pope is in place or the papal path is completed.
    public void moveForward(){
        faithPosition+=1;
        int i=faithPosition/8 -1;
        if (faithPosition%8 == 0 && cards[i].getCondition().equals(PapalCardCondition.Inactive))   {
                                        this.popeMeeting (i);
                                        //chiamo la funzione checkPosition(faithPosition, i=pos/8 +3) per gli altri giocatori
                                    }
        if (faithPosition == 24)      this.endGame();
    }
    //gets called after previous controls, sets the card as active
    public void popeMeeting(int cardID){
        cards[cardID].setCondition(PapalCardCondition.Active);
    }

    //each player, except the one who is actually playing the turn, activates this method. All cards with the same number become either active or discarded, so nobody can call a pope meeting
    //  with a card of the same ID
    public void checkPosition(int pos, int delta){
        if (faithPosition>pos-delta) this.popeMeeting(pos/8 -1);
        else this.cards[pos/8 -1].setCondition(PapalCardCondition.Discarded);
    }

    public int getFaithPosition(){
        return faithPosition;
    }

}