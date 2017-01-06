import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.*;


public class Server {
	static final int PORT = 118;

	static ServerSocket server;
	static Socket socket;
	static InputStreamReader input;
	static BufferedReader reader;
	static PrintStream printer;
	static String roomName;
	static ArrayList<Room> rooms = new ArrayList<Room>();
	static ArrayList<User> users = new ArrayList<User>();
	static HashMap<String, Integer> nameMap = new HashMap<String, Integer>();
	static String currUser;
	private final static String SPLITTER = "@&@&";


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		while(true){
			try {
				System.out.println("Server has started...");
				//set up the server socket with default port
				server = new ServerSocket(PORT);

				//Continually look for clients that connect and when they
				while(true){
					socket = server.accept();
					input = new InputStreamReader(socket.getInputStream());
					reader = new BufferedReader(input);
					printer = new PrintStream(socket.getOutputStream());

					//set a user id for this client
					int id = newUser();

					String message = "USERID" + SPLITTER + id;

					//send client their userId
					printer.println(message);
					printer.flush();

					//send client the available list of rooms
					ArrayList<String> rlist = createRoomList(rooms);
					sendRooms(socket, rlist);

					ServerThread serverThread = new ServerThread(socket, id);
					Thread thread = new Thread(serverThread);
					thread.start();
				}
			} 
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	

	public static int newUser(){
		//creates a new user and adds to user list, returns user id
		int size = users.size();
		int userId = size;
		boolean found = false;

		//look for an inactive user to overtake their id/slot
		for(int i = 0; i < size; i++){
			if(!users.get(i).isActive()){
				userId = i;
				found = true;
				break;
			}
		}

		if(!found){
			//case where no open slots - add to end of list
			users.add(new User(userId, socket));
		}
		else{
			//case where inactive id was found - take that id slot
			users.set(userId, new User(userId, socket));
		}
		return userId;		
	}
	
	public static void sendRooms(Socket sock, ArrayList<String> rms){
		//sends a roomlist to the client specified
		try {
			PrintStream roomPrinter = new PrintStream(sock.getOutputStream());
			roomPrinter.println("ROOMS" + SPLITTER + rms);
			roomPrinter.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void sendFriends(Socket sock, int rid, ArrayList<String> friends){
		//sends a user names of all names in a given room
		try {
			PrintStream friendPrinter = new PrintStream(sock.getOutputStream());
			friendPrinter.println("FRIENDS" + SPLITTER + rid + SPLITTER + friends);
			friendPrinter.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static int addRoom(String roomName, Socket socket, int userId, String userName){
		//creates a new room and returns the room id to send the user
		String newName = roomName;
		int size = rooms.size();
		int cnt = 1;
		int roomId = size;
		boolean found = false;

		//check that the name doens't exist and if there is a free slot in rooms list
		for(int i = 0; i < size; i++){
			if(newName.equals(rooms.get(i).getRoomName()) && rooms.get(i).isActive()){
				newName = roomName + "(" + cnt + ")";
				cnt++;
			}

			if(!found && !rooms.get(i).isActive()){
				roomId = i;
				found = true;
			}
		}

		//add the room to the array list, add to hashmap, increment index
		if(!found){
			//case where there are no empty slots
			rooms.add(new Room(newName, roomId, socket, userName, userId));	
		}
		else{
			//case where an inactive room is replaced with a new room
			rooms.set(roomId, new Room(newName, roomId, socket, userName, userId));
		}

		
		nameMap.put(newName, roomId);	

		//add the room id to the user's room list and update name
		users.get(userId).addRoom(roomId);
		users.get(userId).addName(userName);
		return roomId;
	}


	public static ArrayList<String> createRoomList(ArrayList<Room> rms){
		//helper function that creates a list of room names
		ArrayList<String> roomNames = new ArrayList<String>();
		int size = rms.size();
		for(int i = 0; i < size; i++){
			if(rms.get(i).isActive())
				roomNames.add(rms.get(i).room);
		}
		return roomNames;
	}

	public static ArrayList<Socket> createSocketList(ArrayList<User> usersAList){
		//helper function that creates a list of sockets
		ArrayList<Socket> socketList = new ArrayList<Socket>();
		int size = usersAList.size();
		for(int i = 0; i < size; i++){
			if(usersAList.get(i).isActive())
				socketList.add(usersAList.get(i).getSocket());
		}
		return socketList;
	}

	
	
}

