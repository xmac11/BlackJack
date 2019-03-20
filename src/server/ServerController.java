package server;


import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class ServerController {
	@FXML
	private TextField portField;
	
	@FXML
	private TextField ipField;

	@FXML
	private Button startButton;

	public void setIP(String IP) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				ipField.setText(IP);
			}
		});
	}

	public void startServer() {
		if (portField.getText().trim().length() > 0 && portField.getText().matches("[0-9]+")) {
			startButton.setDisable(true);
			portField.setDisable(true);
			Runnable runnable = new Server(Integer.parseInt(portField.getText()), this);
			Thread serverThread = new Thread(runnable);
			serverThread.start();
		}
	}
}
