package it.polimi.ingsw.Communication.server.messages;

import it.polimi.ingsw.Player;

/**
 * Used when a player reconnects to inform everyone, giving them the nickname of who reconnected.
 */

public class ReconnectionMessage implements Message{
    private final String message;

    public ReconnectionMessage(Player reconnectedPlayer) {
        message= reconnectedPlayer.getNickname()+ " has just reconnected! He'll now get to play from where he left; wish him good luck!";
    }

    public String getMessage() {
        return message;
    }
}
