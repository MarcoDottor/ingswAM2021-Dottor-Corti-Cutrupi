package it.polimi.ingsw.communication.server.messages.printableMessages;

import it.polimi.ingsw.communication.server.messages.Message;

public class DoubleBlackCrossTokenMessage implements PrintableMessage {
    String string;

    public DoubleBlackCrossTokenMessage(int faithPosition) {
        string = "Lorenzo drew a DoubleBlackCrossToken: now he is at position " + faithPosition;
    }

    @Override
    public String getString() {
        return string;
    }
}
