package it.polimi.ingsw.communication.client.actions.secondaryActions;

import it.polimi.ingsw.communication.server.GameHandler;

public class PapalInfoAction implements SecondaryAction {

    @Override
    public void execute(GameHandler gameHandler) {
        gameHandler.papalInfo(gameHandler.activePlayer());
    }

}