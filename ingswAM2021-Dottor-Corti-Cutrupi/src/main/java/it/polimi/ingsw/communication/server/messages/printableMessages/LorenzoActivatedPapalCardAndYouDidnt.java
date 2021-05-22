package it.polimi.ingsw.communication.server.messages.printableMessages;

import it.polimi.ingsw.communication.server.messages.Message;

public class LorenzoActivatedPapalCardAndYouDidnt implements PrintableMessage {
    String string;

    public LorenzoActivatedPapalCardAndYouDidnt(int cardIndex) {
        string="Lorenzo activated papal favor card number, "+cardIndex +
                "unfortunately you weren't far enough in the papal to activate it too";
    }

    public String getString() {
        return string;
    }
}
