package server;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class ServerController  {

    @FXML
    private ListView<String> serverView;

    @FXML
    private ToggleButton toggler;

    private boolean toggle = false;
    private Server server =null;
    private Thread thread = null;


    @FXML
    public void initialize() {
        //if (toggle == true){
            server = new Server();
//            server.setServerController(this);
            toggle = false;
           // Thread gameSession = new Thread(server);
           // gameSession.interrupt();

       // }
       // else{
          //  toggle = false;
        //}
    }

    public void toggleOn(MouseEvent mouseEvent) {
    	System.out.println(toggle);
        if(toggle){
            thread.interrupt();
            initialize();
//            toggle = false;
        }else{
            thread = new Thread(server);
            thread.start();
            toggle = true;
        }
    }

    public void addToServerView(String incommingSystemMessage) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                serverView.getItems().add(incommingSystemMessage);
            }
        });
    }

}
