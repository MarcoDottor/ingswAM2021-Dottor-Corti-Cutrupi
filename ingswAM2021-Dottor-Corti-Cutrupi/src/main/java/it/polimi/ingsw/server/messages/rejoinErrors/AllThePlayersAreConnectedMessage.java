package it.polimi.ingsw.server.messages.rejoinErrors;

import it.polimi.ingsw.client.shared.ClientSideSocket;

/**
 * Self explanatory name
 */
public class AllThePlayersAreConnectedMessage implements RejoinErrorMessage {
    private final String string = "All the players of the game you want to rejoin are connected, please create or join another game";

    public String getString() {
        return string;
    }

    @Override
    public void execute(ClientSideSocket socket, boolean isGui) {
        if(isGui){
            socket.addErrorAlert("All the players of the game you want to rejoin are connected","please create or join another game");
        }
        else {
            System.out.println(string);
        }
    }
}
