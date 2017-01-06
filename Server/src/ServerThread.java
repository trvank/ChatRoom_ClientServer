import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;


public class ServerThread implements Runnable{

	private Socket socket;
	private int userId;
	private InputStreamReader input;
	private BufferedReader reader;
	private PrintStream printer;
	private String message;

	private final String SPLITTER = "@&@&";

	public ServerThread(Socket sock, int uid){
		socket = sock;
		userId = uid;
	}

	public void run(){

		try {
			input = new InputStreamReader(socket.getInputStream());
			reader = new BufferedReader(input);
			printer = new PrintStream(socket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		while(true){
			try {
				//continue to get input from client
				message = reader.readLine();
				String messages[] = message.split(SPLITTER);

				//checking for exit/join/new/message
				if(messages[0].equals("EXIT")){
					int uid = Integer.parseInt(messages[1]);
					exit(uid);
					printer.println("EXIT" + SPLITTER + uid);
					printer.flush();
					break; //exits while loop so thread can be finished
				}

				else if(messages[0].equals("NEW")){
					newRoom(messages[1], Integer.parseInt(messages[2]), messages[3]);
				}

				else if(messages[0].equals("JOIN")){
					joinRoom(messages[1], Integer.parseInt(messages[2]), messages[3]);
				}

				else if(messages[0].equals("NORMAL")){
					blast("MESSAGE" + SPLITTER + messages[1] + SPLITTER + messages[3], Integer.parseInt(messages[1]));
				}

				else if(messages[0].equals("WHISPER")){
					whisper(Integer.parseInt(messages[1]), messages[3], "MESSAGE" + SPLITTER + messages[1] + SPLITTER + messages[4]);
				}

				else if(messages[0].equals("ENCRYPTED_NORMAL")){
					System.out.println("For DEMO ~ Server received encrypted message: " + messages[3]);
					blast("ENCRYPTED" + SPLITTER + messages[1] + SPLITTER + messages[3], Integer.parseInt(messages[1]));
				}
				
				else if(messages[0].equals("ENCRYPTED_WHISPER")){
					System.out.println("For DEMO ~ Server received encrypted message: " + messages[4]);
					whisper(Integer.parseInt(messages[1]), messages[3], "ENCRYPTED" + SPLITTER + messages[1] + SPLITTER + messages[4]);
				}

				else if(messages[0].equals("LEAVE")){
					int rid = Integer.parseInt(messages[1]);
					int uid = Integer.parseInt(messages[2]);
					removeClient(rid, uid);
					blast("MESSAGE" + SPLITTER + messages[1] + SPLITTER + messages[3], Integer.parseInt(messages[1]));
				}
			} 
			catch (SocketException exc){
				try {
					exit(userId);
					socket.close();
					break; //if the connection is lost, break the loop and end the thread
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}



	public void exit(int uid){
		//remove user from all rooms in which they are active
		//romove user from userList
		//close the connection

		ArrayList<Integer> userRooms = Server.users.get(uid).getRooms();
		ArrayList<Integer> removeIds = new ArrayList<Integer>();

		//get the id's to remove
		for(Integer id : userRooms){
			removeIds.add(id);
		}

		//remove them (sep step because error when removing while still in the loop getting ids
		for (Integer id : removeIds){
			removeClient(id, uid);
		}
	}

	public void removeClient(int rid, int uid){
		//remove the user from the room
		//if last user update client room lists
		Server.rooms.get(rid).removeUser(uid);

		if(!Server.rooms.get(rid).isActive()){
			updateClientRooms();//sends new list without this room
		}
		//update all the friendlists for the remaining rooms
		else{
			sendClientFriends(rid);
		}

		//remove the room from the user's room list
		//if last room make user invalid
		Server.users.get(uid).removeRoom(rid);
	}

	public void newRoom(String rname, int uid, String uname){
		//need to add room and add user
		int roomId = Server.addRoom(rname, socket, uid, uname);
//change below to server.rooms.get(roomId).getRoomName()		int roomId = Server.rooms.get(roomIdx).getRoomId();
		String newRoomName = Server.rooms.get(roomId).getRoomName();

		//send the id to the client
		printer.println("NEWROOM" + SPLITTER + roomId + SPLITTER + newRoomName);
		printer.flush();

		sendClientFriends(roomId);
		updateClientRooms();
	}

	public void joinRoom(String rname, int uid, String uname){
		//add the user to the appropriate room in the roomList

		int roomId = Server.nameMap.get(rname);
		Server.rooms.get(roomId).addUser(socket, uname, uid);

		//add the room to the users room list
		Server.users.get(uid).addRoom(roomId);
		Server.users.get(uid).addName(uname);

		//send roomid to the client
		printer.println("JOINROOM" + SPLITTER + roomId + SPLITTER + rname);
		printer.flush();

		sendClientFriends(roomId);
		updateClientRooms();
	}

	public void updateClientRooms(){
		ArrayList<Socket> socketList = Server.createSocketList(Server.users);
		ArrayList<String> roomList = Server.createRoomList(Server.rooms);

		for(Socket sock : socketList){
			Server.sendRooms(sock, roomList);
		}
	}

	public void sendClientFriends(int rid){
		ArrayList<String> friendList = Server.rooms.get(rid).getNames();
		ArrayList<Socket> socketList = Server.rooms.get(rid).getSockets();

		for(Socket sock : socketList){
			Server.sendFriends(sock, rid, friendList);
		}
	}

	public void whisper(int roomId, String toUser, String msg){
		Socket toSocket;
		toSocket = Server.rooms.get(roomId).getSocket(toUser);

		try {
			PrintStream aPrinter = new PrintStream(socket.getOutputStream());
			aPrinter.println(msg);
			aPrinter.flush();

			if(!socket.equals(toSocket)){
				//only send once if user whispers to themselves
				PrintStream bPrinter = new PrintStream(toSocket.getOutputStream());
				bPrinter.println(msg);
				bPrinter.flush();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void blast(String msg, int roomId){
		ArrayList<Socket> sockets;
		sockets = Server.rooms.get(roomId).getSockets();

		for(Socket sock : sockets){
			try {
				PrintStream blastPrinter = new PrintStream(sock.getOutputStream());
				blastPrinter.println(msg);
				blastPrinter.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	

}

