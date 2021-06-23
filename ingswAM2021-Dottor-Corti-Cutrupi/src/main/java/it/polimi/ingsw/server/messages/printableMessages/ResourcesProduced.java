package it.polimi.ingsw.server.messages.printableMessages;

import it.polimi.ingsw.client.shared.ClientSideSocket;

public class ResourcesProduced implements PrintableMessage {
    private String string;

    public ResourcesProduced(String s) {
        string = "Resources produced: "+s;
    }

    public String getString() {
        return string;
    }

    @Override
    public void execute(ClientSideSocket socket, boolean isGui) {
        if(!isGui) System.out.println(string);
    }
}
