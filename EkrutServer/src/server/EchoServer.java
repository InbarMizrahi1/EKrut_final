// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 
package server;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import common.ClientConnected;
import entities.Device;
import entities.Message;
import entities.User;
import enums.Request;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;

/**
 * This class overrides some of the methods in the abstract superclass in order
 * to give more functionality to the server.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;re
 * @author Fran&ccedil;ois B&eacute;langer
 * @author Paul Holden
 * @version July 2000
 */

public class EchoServer extends AbstractServer {
	// Class variables *************************************************

	/**
	 * The default port to listen on.
	 */
	final public static int DEFAULT_PORT = 5555;

	// Constructors ****************************************************

	/**
	 * Constructs an instance of the echo server.
	 *
	 * @param port The port number to connect on.
	 * 
	 */
	private static ObservableList<ClientConnected> clientList = FXCollections.observableArrayList();

	public ObservableList<ClientConnected> getClientList() {

		return clientList;
	}

	public EchoServer(int port) {
		super(port);
	}

	// Instance methods ************************************************

	/**
	 * This method handles any messages received from the client.
	 *
	 * @param msg    The message received from the client.
	 * @param client The connection from which the message originated.
	 * @param
	 */
	@SuppressWarnings("unchecked")
	public void handleMessageFromClient(Object msg, ConnectionToClient client) {
		Message messageFromClient = (Message) msg;
		System.out.println("Message received: " + ((Message) msg).getRequest().toString() + " from " + client);

		switch (messageFromClient.getRequest()) {
		case Connect_request:
			updateClientList(client, "Connected");
			try {
				client.sendToClient(new Message(Request.Connected, null));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Could not send message to client.");
			}
			break;
		case Disconnect_request:
			updateClientList(client, "Disconnected");
			try {
				client.sendToClient(new Message(Request.Disconnected, null));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Could not send message to client.");
			}
			break;
		case Login_Request:
			
			ArrayList<String> userANDpassword = (ArrayList<String>) messageFromClient.getObject();
			User user = MySqlController.LoginCheckAndUpdateLoggedIn(userANDpassword);
			Message msgToClient;
			if (user != null) {
				if (user.isLoggedIn() == false) {
					System.out.println("Username: " + user.getUsername() + " Password: " + user.getPassword());
					System.out.println("User details were imported successfully");
					msgToClient = new Message(Request.LoggedIn_Succses, user);
				} else {
					System.out.println("User already logged in");
					msgToClient = new Message(Request.LoggedIn_UnsuccsesAlreadyLoggedIn, user);
				}
			} 
			else {
				System.out.println("User login failed");
				msgToClient = new Message(Request.Unsuccsesful_LogIn, null);
			}
			// Send message to the client that everything went well
			try {
				client.sendToClient(msgToClient);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case Logout_request:
			User userLogout = (User)messageFromClient.getObject();
			try {
				MySqlController.UserLogoutAndUpdateDB(userLogout);
				try {
					client.sendToClient(new Message(Request.LoggedOut,null));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		case Get_Devices_By_Area:
			String area = (String)messageFromClient.getObject();
			try {
				client.sendToClient(new Message(Request.Devices_Imported,MySqlController.getAllDevicesByArea(area)));
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		case GetOrdersData:
			ArrayList<String> fields = (ArrayList<String>)messageFromClient.getObject();
			try {
				client.sendToClient(new Message(Request.OrdersData_Imported,MySqlController.getOrdersReportData(fields)));
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		case Threshold_Update_Request:
			MySqlController.updateDeviceThreshold((ArrayList<Device>)messageFromClient.getObject());
			try {
				client.sendToClient(new Message(Request.Threshold_Updated,null));
			} catch (IOException e) {
				System.out.println("Could not send message to client.");
			}
		default:
			break;
		}
	}

	private void updateClientList(ConnectionToClient client, String connectionStatus) {
		// if (connectionStatus.equals("Disconnect")) {
		for (int i = 0; i < clientList.size(); i++) {
			if (clientList.get(i).getIP().equals(client.getInetAddress().getHostAddress()))
				clientList.remove(i);
		}

		clientList.add(new ClientConnected(client.getInetAddress().getHostAddress(),
				client.getInetAddress().getHostAddress(), connectionStatus));
		// System.out.println(clientList);
	}

	/**
	 * This method overrides the one in the superclass. Called when the server
	 * starts listening for connections.
	 */
	protected void serverStarted() {
		System.out.println("Server listening for connections on port " + getPort());
	}

	/**
	 * This method overrides the one in the superclass. Called when the server stops
	 * listening for connections.
	 */
	protected void serverStopped() {
		System.out.println("Server has stopped listening for connections.");
	}
}
//End of EchoServer class
