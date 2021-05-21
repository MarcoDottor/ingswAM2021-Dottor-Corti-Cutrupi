package it.polimi.ingsw.Communication.client.actions.secondaryActions;
/**
 * Action used to view a dashboard. Contains the index to access the player via gameHandler.
 */
public class ViewDashboardAction implements SecondaryAction{
    int playerOrder;

    public ViewDashboardAction(int playerID) {
        this.playerOrder = playerID;
    }

    public ViewDashboardAction() {
        this.playerOrder = 0;
    }

    public int getPlayerOrder() {
        return playerOrder;
    }

    @Override
    public String toString() {
        return "ViewDashboardAction{" +
                "playerOrder=" + playerOrder +
                '}';
    }
}
