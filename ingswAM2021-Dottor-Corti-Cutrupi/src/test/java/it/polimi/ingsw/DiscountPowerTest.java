package it.polimi.ingsw;

import it.polimi.ingsw.Exceptions.NotEnoughResourcesToActivateProductionException;
import it.polimi.ingsw.Model.boardsAndPlayer.Dashboard;
import it.polimi.ingsw.Model.resource.CoinResource;
import it.polimi.ingsw.Model.resource.Resource;
import it.polimi.ingsw.Model.resource.ServantResource;
import it.polimi.ingsw.Model.resource.StoneResource;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

public class DiscountPowerTest {

    @Test
    public void availableResourcesForDevelopmentTest() throws FileNotFoundException {
        Resource stone= new StoneResource();
        Resource coin= new CoinResource();
        Dashboard dashboard= new Dashboard(1);
        dashboard.activateDiscountCard(new StoneResource());
        dashboard.activateDiscountCard(new ServantResource());
        dashboard.getStrongbox().addResource(new StoneResource());
        dashboard.getStrongbox().addResource(new StoneResource());
        dashboard.getWarehouse().addResource(new StoneResource());
        dashboard.getStrongbox().addResource(new ServantResource());
        dashboard.getWarehouse().addResource(new ServantResource());
        dashboard.getWarehouse().addResource(new CoinResource());
        assertEquals(1,dashboard.getWarehouse().amountOfResource(stone));
        assertEquals(2,dashboard.getStrongbox().amountOfResource(stone));
        assertEquals(4,dashboard.availableResourcesForDevelopment(stone));
        assertEquals(dashboard.availableResourcesForProduction(stone),3);
        assertEquals(dashboard.availableResourcesForProduction(coin),1);
        assertEquals(dashboard.availableResourcesForDevelopment(coin),1);
        assertEquals(2,dashboard.availableResourcesForProduction(new ServantResource()));
        assertEquals(3,dashboard.availableResourcesForDevelopment(new ServantResource()));
        dashboard.removeResourcesFromDashboard(2, new ServantResource());
        assertEquals(0,dashboard.availableResourcesForProduction(new ServantResource()));
        assertEquals(1,dashboard.availableResourcesForDevelopment(new ServantResource()));
    }



}
