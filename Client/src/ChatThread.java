import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;


public class ChatThread implements Runnable{

	private InputStreamReader input;
	private BufferedReader reader;
	private final String SPLITTER = "@&@&";
	private boolean connected;

	ClientWindow window;


	public ChatThread(ClientWindow win){
		window = win;
		connected = true;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

		try {
			input = new InputStreamReader(window.getSocket().getInputStream());
			reader = new BufferedReader(input);

			while(connected){
				try{
					getMessage();
				}
				catch (IllegalStateException exc){
					//if an IllegalStateException is caught
					//not connected to server so break the loop and close the socket
					break;
				}
			}

			window.closeSocket();
			window.emptyRoom();
			window.setExitState();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void getMessage() {
		try {

			String message = reader.readLine();
			String[] messages = message.split(SPLITTER);

			if(messages[0].equals("MESSAGE")){
				window.message(Integer.parseInt(messages[1]), messages[2]);
			}
			
			else if(messages[0].equals("ENCRYPTED")){
				window.message(Integer.parseInt(messages[1]), window.command.encryptMessage(messages[2]));
			}

			else if(messages[0].equals("FRIENDS")){
				int rid = Integer.parseInt(messages[1]);
				String string = messages[2];
				window.setFriendList(rid, string);
			}

			else if(messages[0].equals("ROOMS")){
				String string = messages[1];
				window.setList(string, window.getRoomList());
			}

			else if(messages[0].equals("NEWROOM")){

				String rid = messages[1];
				String rname = messages[2];
				window.addRoom(Integer.parseInt(rid), rname);
			}

			else if(messages[0].equals("JOINROOM")){
				String rid = messages[1];
				String rname = messages[2];
				window.addRoom(Integer.parseInt(rid), rname);
			}

			else if(messages[0].equals("EXIT")){
				connected = false;
				window.closeSocket();
			}

		} 
		catch (SocketException exc){
			throw new IllegalStateException("not connected!");
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
