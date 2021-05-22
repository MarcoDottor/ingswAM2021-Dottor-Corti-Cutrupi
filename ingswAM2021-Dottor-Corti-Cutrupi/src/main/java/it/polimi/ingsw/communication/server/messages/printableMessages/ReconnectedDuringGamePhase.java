package it.polimi.ingsw.communication.server.messages.printableMessages;

import it.polimi.ingsw.communication.server.messages.Message;

public class ReconnectedDuringGamePhase implements PrintableMessage {
    String string = "You were in effective game phase, you will be able to make your moves " +
            "once it is your turn";

    public String getString() {
        return string;
    }
}
