package it.polimi.ingsw.resource;

import it.polimi.ingsw.Warehouse;
import it.polimi.ingsw.papalpath.PapalPath;

public class ShieldResource implements Resource {
    boolean isNew = true;

    public void notNewAnymore(){
        isNew = false;
    }

    public boolean getIsNew(){
        return isNew;
    }

    @Override
    public String getResourceType() {
        return "shield";
    }

    @Override
    public void effectFromMarket(Warehouse warehouse, PapalPath papalPath) {
        warehouse.addResource(this       );
    }
}
