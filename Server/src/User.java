import java.net.Socket;
import java.util.ArrayList;


public class User {
	int userId;
	Socket socket;
	String userName;
	ArrayList<Integer> roomIds = new ArrayList<Integer>();
	boolean active;
	
	public User(int uid, Socket sock){
		userId = uid;
		socket = sock;
		active = true;
	}
	
	public void addRoom(int id){
		roomIds.add(id);
		active = true;  //fixing error when user w/ 1 room left a room
	}
	
	public void addName(String name){
		userName = name;
	}
	
	public void removeRoom(int rid){
		roomIds.remove((Integer) rid);
		if(roomIds.size() < 1){
			active = false;
		}
	}
	
	public Socket getSocket(){
		return socket;
	}
	
	public ArrayList<Integer> getRooms(){
		return roomIds;
	}
	
	public boolean isActive(){
		if(active){
			return true;
		}
		else{
			return false;
		}
	}

}
