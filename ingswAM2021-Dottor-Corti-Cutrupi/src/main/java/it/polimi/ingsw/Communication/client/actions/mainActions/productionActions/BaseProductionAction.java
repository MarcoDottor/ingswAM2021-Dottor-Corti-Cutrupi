package it.polimi.ingsw.Communication.client.actions.mainActions.productionActions;

import it.polimi.ingsw.Communication.client.actions.mainActions.ProductionAction;
import it.polimi.ingsw.resource.Resource;

import java.util.ArrayList;

/**
 * Created when the client decides to activate the basic production
 */
public class BaseProductionAction implements ProductionAction {
    private ArrayList<Resource> resourcesUsed;
    private ArrayList<Resource> resourcesWanted;

    public BaseProductionAction(ArrayList<Resource> resourcesUsed, ArrayList<Resource> resourcesWanted) {
        this.resourcesUsed = resourcesUsed;
        this.resourcesWanted = resourcesWanted;
    }

    @Override
    public String toString() {
        return "BaseProductionAction{" +
                "resourcesUsed=" + resourcesUsed +
                ", resourcesWanted=" + resourcesWanted +
                '}';
    }

    public ArrayList<Resource> getResourcesUsed() {
        return resourcesUsed;
    }

    public ArrayList<Resource> getResourcesWanted() {
        return resourcesWanted;
    }
}
