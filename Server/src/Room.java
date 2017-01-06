import java.net.Socket;
import java.util.ArrayList;


public class Room {
	
	private ArrayList<Socket> sockets;
	private ArrayList<String> names;
	private ArrayList<Integer> userIds;
	private boolean active;
	String room;
	int roomId;
	
	public Room(){
		sockets = new ArrayList<Socket>();
		names = new ArrayList<String>();
		userIds = new ArrayList<Integer>();
		active = true;
		room = "";
	}
	
	public Room(String roomName, int rid, Socket socket, String user, int uid){
		sockets = new ArrayList<Socket>();
		names = new ArrayList<String>();
		userIds = new ArrayList<Integer>();
		active = true;
		room = roomName;
		roomId = rid;
		addUser(socket, user, uid);
	}
	
	public void addUser(Socket socket, String user, int uid){
		sockets.add(socket);
		names.add(user);
		userIds.add(uid);
	}
	
	public void removeUser(int uid){
		int index = 0;
		index = userIds.indexOf(uid);

		sockets.remove(index);
		names.remove(index);
		userIds.remove(index);
		
		//check if there are any more users
		if(sockets.size() < 1)
			active = false;
	}
	
	public void updateSocket(String name, Socket socket){
		int userIndex = names.indexOf(name);
		sockets.set(userIndex, socket);
	}
	
	public boolean isActive(){
		if(active){
			return true;
		}
		else{
			return false;
		}
	}
	
	public Socket getSocket(String userName){
		Socket socket;
		int index; 
		index = names.indexOf(userName);
		socket = sockets.get(index);
		return socket;
	}
	
	public String getUser(Socket socket){
		String userName;
		int index; 
		
		index = sockets.indexOf(socket);
		userName = names.get(index);
		return userName;
	}
	
	public int getRoomId(){
		return roomId;
	}
		
	public String getRoomName(){
		return room;
	}
	
	public ArrayList<Socket> getSockets(){
		return sockets;
	}
	
	public ArrayList<String> getNames(){
		return names;
	}
	
	

}
