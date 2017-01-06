
public class CRoom {
	
	String[] friends = new String[10];
	String convo;
	String roomName;
	int roomId;
	boolean active;
	
	public CRoom(int rid, String rname){
		roomId = rid;
		roomName = rname;
		convo = "";
		active = true;
		
	}
	
	public void setFriends(String string){
		String friendsList = string.replace("[", "");
		friendsList = friendsList.replace("]", "");
		friends = friendsList.split(", ");
	}
	
	public boolean isActive(){
		if(active){
			return true;
		}
		else{
			return false;
		}
	}
	
	public void deactivate(){
		active = false;
	}
	
	public String getName(){
		return roomName;
	}
	
	public int getId(){
		return roomId;
	}

}
