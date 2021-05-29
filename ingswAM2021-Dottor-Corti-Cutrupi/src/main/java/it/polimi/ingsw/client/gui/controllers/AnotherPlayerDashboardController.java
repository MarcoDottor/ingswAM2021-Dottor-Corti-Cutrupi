package it.polimi.ingsw.client.gui.controllers;

import it.polimi.ingsw.client.gui.GUI;
import it.polimi.ingsw.client.gui.utility.ImageSearcher;
import it.polimi.ingsw.server.messages.jsonMessages.DevelopmentCardMessage;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;

public class AnotherPlayerDashboardController implements GUIController{

    @FXML private ImageView DevCardZone11;
    @FXML private ImageView DevCardZone12;
    @FXML private ImageView DevCardZone13;
    @FXML private ImageView DevCardZone21;
    @FXML private ImageView DevCardZone22;
    @FXML private ImageView DevCardZone23;
    @FXML private ImageView DevCardZone31;
    @FXML private ImageView DevCardZone32;
    @FXML private ImageView DevCardZone33;

    @FXML private Button goBackButton;
    ArrayList<ImageView> devCardZones;

    private GUI gui;

    @Override
    public void setGui(GUI gui) {
        this.gui=gui;
        devCardZones=new ArrayList<>();
        devCardZones.add(DevCardZone11);    devCardZones.add(DevCardZone12);    devCardZones.add(DevCardZone13);
        devCardZones.add(DevCardZone21);    devCardZones.add(DevCardZone22);    devCardZones.add(DevCardZone23);
        devCardZones.add(DevCardZone31);    devCardZones.add(DevCardZone32);    devCardZones.add(DevCardZone33);
    }


    public void goBackToDashboard(MouseEvent mouseEvent) {
        gui.setFalseShowingOtherPlayerDashboard();
        gui.changeStage("dashboard.fxml");
    }

    public void addCardToDevCardZone(DevelopmentCardMessage message) {
        ImageSearcher imageSearcher = new ImageSearcher();
        int position= message.getLevel()+ (message.getDevCardZone()-1)*3 -1;
        String devCard= imageSearcher.getImageFromColorVictoryPoints(message.getColor(), message.getVictoryPoints());
        Image image= new Image(getClass().getResourceAsStream(devCard));
        devCardZones.get(position).setImage(image);
    }

    public void resetDashboard() {

        //todo: reset this dashboard

    }

    public void goToLeaderCards(MouseEvent mouseEvent) {
        gui.changeStage("anotherPlayerLeadercards.fxml");
    }

}
