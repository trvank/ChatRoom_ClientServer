
import java.io.IOException;
import java.io.PrintStream;

import javax.swing.JOptionPane;


public class Commands {

	private ClientWindow window;
	private boolean encrypted;

	private final String SPLITTER = "@&@&";

	private PrintStream printer;

	public Commands(ClientWindow win){
		window = win;
		try {
			printer = new PrintStream(window.getSocket().getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendMessage(int rid, int userId, String userName, String msg){
		if(msg.contains(SPLITTER)){
			JOptionPane.showMessageDialog(null, "'@&@&' is an illegal string. \n"
					+ "make sure your message does not contain '@&@&'");
			return;
		}
		if(window.getCurrRoom() < 0){
			JOptionPane.showMessageDialog(null, "You have no active rooms");
		}
		else{
			String message;
			String note = SPLITTER + rid + SPLITTER + userId + SPLITTER;
			if(encrypted){
				message = "ENCRYPTED_NORMAL" + note + encryptMessage(userName + ": " +msg);
				setDecryption();
			}
			else{
				message = "NORMAL" + note + userName + ": " + msg;
			}
			printer.println(message);
			printer.flush();
			window.clearTextAreaMessage();
		}
	}

	public void whisperMessage(int rid, int userId, String userName, String userTo, String msg){
		if(msg.contains(SPLITTER)){
			JOptionPane.showMessageDialog(null, "'@&@&' is an illegal string. \n"
					+ "make sure your message does not contain '@&@&'");
			return;
		}
		if(window.getCurrRoom() < 0){
			JOptionPane.showMessageDialog(null, "You have no active rooms");
		}
		else{
			if(userTo == null){
				JOptionPane.showMessageDialog(null, "To whom are you whispering?");
			}
			else{
				String message;
				String note = SPLITTER + rid + SPLITTER + userId + SPLITTER + userTo + SPLITTER;
				if(encrypted){
					message = "ENCRYPTED_WHISPER" + note + encryptMessage("**whisper**	" + userName + ": " + msg);
					setDecryption();
				}
				else{
					message = "WHISPER" + note + "**whisper**	" + userName + ": " + msg;
				}
				printer.println(message);
				printer.flush();
				window.clearTextAreaMessage();
			}
		}
	}

	public String encryptMessage(String msg){
		//helper function to encrypt the message before sending

		String key = "HereIsAGoodKeyForThisSimpleToyChatApp";

		StringBuffer newMessage = new StringBuffer(msg);

		//XOR each successive character of the message with the next successive
		//character of the key.  If key reaches the end, circle back to the beginning of the key
		//each character will be encrypted, regardless of length
		for (int i = 0, j = 0; i < newMessage.length(); i++, j++) {
			if (j >= key.length()) {
				j = 0;
			}

			if((newMessage.charAt(i) ^ key.charAt(j)) != 10 && (newMessage.charAt(i) ^ key.charAt(j)) != 13)
				newMessage.setCharAt(i, (char) (newMessage.charAt(i) ^ key.charAt(j)));
		}

		return newMessage.toString();



	}

	public void setEncryption(){
		if(encrypted){
			setDecryption();
		}
		else{
			encrypted = true;
			window.setEncryptedBtn();
		}
	}

	public void setDecryption(){
		encrypted = false;
		window.setNotEncryptedBtn();
	}

	public void leave(int rid, int userId, String userName){
		if(window.getCurrRoom() < 0){
			JOptionPane.showMessageDialog(null, "You have no active rooms");
		}
		else{
			String message = "LEAVE" + SPLITTER + rid + SPLITTER + userId + SPLITTER + userName + " has left the room";
			printer.println(message);
			printer.flush();
			window.removeRoom(rid);
			if(encrypted)
				setDecryption();;
		}
	}

	public void joinRoom(String roomName, int userId, String userName){
		//send the info to create a new room
		if(roomName != null){

			if(window.getNameMap().containsKey(roomName)){
				window.setRoom(window.getNameMap().get(roomName));
			}
			else{
				printer.println("JOIN" + SPLITTER + roomName + SPLITTER + userId + SPLITTER + userName);
				printer.flush();
			}
		}
		else{
			JOptionPane.showMessageDialog(null, "You need to select a room");
		}
	}

	public void createRoom(String roomName, int userId, String userName){
		if(roomName.contains(SPLITTER)){
			JOptionPane.showMessageDialog(null, "'@&@&' is an illegal string. \n"
					+ "make sure your room name does not contain '@&@&'");
			return;
		}
		//send the info to create a new room
		if(!(roomName.replace(" ",  "")).equals("")){
			printer.println("NEW" + SPLITTER + roomName + SPLITTER + userId + SPLITTER + userName);
			printer.flush();
		}
		else{
			JOptionPane.showMessageDialog(null, "You need enter a room name");
		}
	}

	public void exit(int userId){
		printer.println("EXIT" + SPLITTER + userId);
		printer.flush();
		window.emptyRoom();
		if(encrypted)
			setDecryption();
	}

}
