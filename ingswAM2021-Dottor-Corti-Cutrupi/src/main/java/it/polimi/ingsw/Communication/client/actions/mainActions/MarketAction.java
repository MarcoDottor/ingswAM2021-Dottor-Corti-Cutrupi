package it.polimi.ingsw.Communication.client.actions.mainActions;

/**
 * Action created when the player decides to get resources from market. It has a boolean isRow to represents if the client wants a row or a column,
 * and an int index to represent what row/index specifically he wants.
 */
public class MarketAction implements MainAction {
    private int index;
    private boolean isRow;

    /**
     * @param index: number of the row/column (starting form 0)
     * @param isRow : true if player wants a row, false for column
     */
    public MarketAction(int index, boolean isRow) {
        this.index = index;
        this.isRow = isRow;
    }

    //JUST FOR TESTING


    @Override
    public String toString() {
        return "MarketAction{" +
                "index=" + index +
                ", isRow=" + isRow +
                '}';
    }

    public int getIndex() {
        return index;
    }

    public boolean isRow() {
        return isRow;
    }
}
