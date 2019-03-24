package server;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

/**
 * Class responsible for controlling server GUI
 * 
 * @author Group 21
 *
 */
public class ServerController {

	@FXML
	private TextField portField;

	@FXML
	private TextField ipField;

	@FXML
	private Button startButton;

	/**
	 * Sets the text inside the IP TextField
	 * 
	 * @param IP the IP of the server
	 */
	public void setIP(String IP) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				ipField.setDisable(false);
				ipField.setText(IP);
			}
		});
	}

	/**
	 * Starts the server, using the port entered by the user.
	 */
	public void startServer() {
		if (portField.getText().trim().length() > 0 && portField.getText().matches("[0-9]+")
				&& portField.getText().trim().length() < 7) {
			startButton.setDisable(true);
			portField.setDisable(true);
			Runnable runnable = new Server(Integer.parseInt(portField.getText()), this); // A new server thread is
																							// initialized
			Thread serverThread = new Thread(runnable);
			serverThread.start();
		}
	}
}
