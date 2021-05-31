package it.polimi.ingsw.server.messages.printableMessages;

import it.polimi.ingsw.client.actions.secondaryActions.ViewDashboardAction;
import it.polimi.ingsw.client.shared.ClientSideSocket;
import javafx.application.Platform;

public class DoubleBlackCrossTokenMessage implements PrintableMessage {
    private String string;
    private String string2;

    public DoubleBlackCrossTokenMessage(int faithPosition) {
        string = "Lorenzo drew a DoubleBlackCrossToken: now he is at position " + faithPosition;
        string2 = "Now he is at position " + faithPosition;
    }

    @Override
    public String getString() {
        return string;
    }

    public void execute(ClientSideSocket socket){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                socket.addLorenzoAlert("He drew a Double Black Cross Token",string2);
                socket.send(new ViewDashboardAction());
            }
        });
    }
}
