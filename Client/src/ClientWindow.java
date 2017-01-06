import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JTextArea;
import javax.swing.JScrollPane;

import java.awt.Font;
import java.awt.Color;


public class ClientWindow {

	private JFrame frame;

	private JLabel lblMyRooms;
	private JLabel lblMyFriends;
	private JLabel lblName;
	private JLabel lblRooms;

	private JTextField txtUserName;
	private JTextField txtNewRoom;

	private JTextArea textAreaConvo;
	private JTextArea textAreaMessage;

	private JList<String> listRooms;
	private JList<String> listMyRooms;
	private JList<String> listMyFriends;

	private JButton btnCreate;
	private JButton btnSend;
	private JButton btnWhisper;
	private JButton btnLeave;
	private JButton btnJoin;
	private JButton btnExit;
	private JButton btnConnect;
	private JButton btnEncrypt;

	private final int PORT = 118;
	private final String HOST = "localhost";
	private Socket socket;
	private InputStreamReader input;
	private BufferedReader reader;

	private final static String SPLITTER = "@&@&";

	public String userName;
	public int userId;
	private static int currRoomId;
	private static int roomIndex = 0;

	public static ArrayList<String> roomList = new ArrayList<String>();
	private static ArrayList<CRoom> rooms = new ArrayList<CRoom>();
	private static HashMap<Integer, Integer> roomMap = new HashMap<Integer, Integer>();//id -> index
	private static HashMap<String, Integer> roomNameMap = new HashMap<String, Integer>();//name -> id

	public Commands command;
	private JScrollPane scrollPaneRooms;
	private JScrollPane scrollPane;
	private JLabel lblBig;
	private JScrollPane scrollPaneMyRooms;
	private JScrollPane scrollPaneMyFriends;



	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientWindow window = new ClientWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ClientWindow() {
		initialize();
		add_actions();


	}

	private void connect(){
		try {
			//set user name
			if(txtUserName.getText().contains("@&@&")){
				JOptionPane.showMessageDialog(null, "'@&@&' is an illegal string. \n"
						+ "make sure your user name does not contain '@&@&'");
				return;
			}
			userName = txtUserName.getText();

			if(userName.equals("")){
				JOptionPane.showMessageDialog(null, "You need a user name");
			}
			else{
				//connect to server, create reader and command instance
				socket = new Socket(HOST, PORT);
				input = new InputStreamReader(socket.getInputStream());
				reader = new BufferedReader(input);
				command = new Commands(this);
				currRoomId = -1;

				//enable the buttons
				btnExit.setEnabled(true);
				btnConnect.setEnabled(false);
				btnEncrypt.setEnabled(true);
				btnCreate.setEnabled(true);
				btnSend.setEnabled(true);
				btnWhisper.setEnabled(true);
				btnLeave.setEnabled(true);
				btnJoin.setEnabled(true);
				txtUserName.setEditable(false);;

				//get initial message from server
				String message = reader.readLine();
				String[] messages = message.split(SPLITTER);

				//use message to set userId and available rooms
				userId = Integer.parseInt(messages[1]);

				message = reader.readLine();
				messages = message.split(SPLITTER);
				message = messages[1];
				message = message.replace("[", "");
				message = message.replace("]", "");
				String[] availableRooms = message.split(", ");
				listRooms.setListData(availableRooms);

				//thread to start listening for commands!!!!!!!
				ChatThread listen = new ChatThread(this);
				Thread thread = new Thread(listen);
				thread.start();

			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void addRoom(int rid, String rname){
		//create a new room and add to the room list
		//update active rooms window
		//set current view to the new room

		CRoom chat = new CRoom(rid, rname);
		rooms.add(chat);//add to my array list of CRooms

		roomList.add(rname);//add to my array list of roomNames
		String temp = ClientWindow.roomList.toString();
		temp = temp.replace("[", "");
		temp = temp.replace("]", "");
		String[] temps = temp.split(", ");
		listMyRooms.setListData(temps);

		roomMap.put(rid, roomIndex);
		roomNameMap.put(rname, roomIndex);
		roomIndex++;
		setRoom(rid);
		
		txtNewRoom.setText("");
	}

	public void removeRoom(int rid){
		//remove a room from the room list
		//update view to a new room (or blank if no active rooms)
		int index = roomMap.get(rid);

		roomList.remove(rooms.get(index).getName());
		roomNameMap.remove(rooms.get(index).getName());
		rooms.get(index).deactivate();
		roomMap.remove(rid);

		setList(roomList.toString(), listMyRooms);

		for(int i = 0; i < rooms.size(); i ++){
			if(rooms.get(i).active){
				setRoom(rooms.get(i).getId());
				break;
			}

			//if get here, there are no active rooms
			listMyFriends.setListData(new String[0]);
			textAreaConvo.setText("");
			textAreaMessage.setText("");
			lblBig.setText("");
			currRoomId = -1;
		}
	}

	public void message(int rid, String msg){
		//adds the message received to the appropriate
		//room convo and updates screen if room currently selected
		int idx = roomMap.get(rid);
		rooms.get(idx).convo = rooms.get(idx).convo + msg + "\n";
		if(rid == currRoomId)
			textAreaConvo.append(msg + "\n");
	}

	public void setRoom(int rid){
		//sets the display to the room parameter
		//used when a room created/joined/removed
		//and when switching between active rooms
		currRoomId = rid;
		int index = roomMap.get(currRoomId);
		lblBig.setText(rooms.get(index).roomName);
		textAreaConvo.setText(rooms.get(index).convo);
		listMyFriends.setListData(rooms.get(index).friends);
		textAreaMessage.setText("");
	}

	public void emptyRoom(){
		//get rid of all rooms and set to blank on exit
		listRooms.setListData(new String[0]);
		listMyRooms.setListData(new String[0]);
		listMyFriends.setListData(new String[0]);
		textAreaConvo.setText("");
		textAreaMessage.setText("");
		lblBig.setText("");
		roomIndex = 0;
		roomList.clear();
		rooms.clear();
		roomMap.clear();
		roomNameMap.clear();
		currRoomId = -1;
	}

	public void closeSocket(){
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void clearTextAreaMessage(){
		//called from commands class
		textAreaMessage.setText("");
	}

	public void setList(String stringList, JList<String> list){
		//helper function to set a JList to a given string list
		String string = stringList.replace("[", "");
		string = string.replace("]", "");
		String[] listContents = string.split(", ");
		list.setListData(listContents);
	}

	public void setFriendList(int rid, String fList){
		//used to specifically set the friend list when
		//updating view to different room
		int index = roomMap.get(rid);
		rooms.get(index).setFriends(fList);
		if(rid == currRoomId){
			setList(fList, listMyFriends);
		}
	}

	public Socket getSocket(){
		return socket;
	}

	public JList<String> getRoomList(){
		return listRooms;
	}
	
	public int getCurrRoom(){
		return currRoomId;
	}
	
	public HashMap<String, Integer> getNameMap(){
		return roomNameMap;
	}
	
	public void setEncryptedBtn(){
		btnEncrypt.setBackground(new Color(0, 0, 0));
	}
	
	public void setNotEncryptedBtn(){
		btnEncrypt.setBackground(new Color(139, 69, 19));
	}


	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.getContentPane().setBackground(new Color(153, 102, 51));
		frame.setBounds(100, 100, 721, 361);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		btnJoin = new JButton("Join");
		btnJoin.setForeground(new Color(255, 0, 255));
		btnJoin.setBackground(new Color(139, 69, 19));
		btnJoin.setFont(new Font("Kristen ITC", Font.PLAIN, 11));
		btnJoin.setEnabled(false);
		btnJoin.setBounds(60, 246, 81, 23);
		frame.getContentPane().add(btnJoin);

		txtUserName = new JTextField();
		txtUserName.setForeground(new Color(31, 191, 189));
		txtUserName.setBackground(new Color(245, 222, 179));
		txtUserName.setBounds(49, 11, 119, 25);
		frame.getContentPane().add(txtUserName);
		txtUserName.setColumns(10);

		lblName = new JLabel("Name:");
		lblName.setForeground(new Color(0, 255, 255));
		lblName.setFont(new Font("Kristen ITC", Font.PLAIN, 11));
		lblName.setHorizontalAlignment(SwingConstants.TRAILING);
		lblName.setBounds(10, 11, 34, 24);
		frame.getContentPane().add(lblName);

		lblRooms = new JLabel("Rooms");
		lblRooms.setForeground(Color.CYAN);
		lblRooms.setFont(new Font("Kristen ITC", Font.PLAIN, 11));
		lblRooms.setHorizontalAlignment(SwingConstants.CENTER);
		lblRooms.setBounds(20, 75, 158, 19);
		frame.getContentPane().add(lblRooms);

		btnExit = new JButton("Exit");
		btnExit.setForeground(new Color(255, 0, 255));
		btnExit.setBackground(new Color(139, 69, 19));
		btnExit.setFont(new Font("Kristen ITC", Font.PLAIN, 11));
		btnExit.setEnabled(false);
		btnExit.setBounds(97, 47, 85, 23);
		frame.getContentPane().add(btnExit);

		btnCreate = new JButton("Create");
		btnCreate.setForeground(new Color(255, 0, 255));
		btnCreate.setBackground(new Color(139, 69, 19));
		btnCreate.setFont(new Font("Kristen ITC", Font.PLAIN, 11));
		btnCreate.setEnabled(false);
		btnCreate.setBounds(60, 295, 81, 23);
		frame.getContentPane().add(btnCreate);

		txtNewRoom = new JTextField();
		txtNewRoom.setForeground(new Color(31, 191, 189));
		txtNewRoom.setBackground(new Color(245, 222, 179));
		txtNewRoom.setFont(new Font("Kristen ITC", Font.PLAIN, 11));
		txtNewRoom.setText("Enter new room to create...");
		txtNewRoom.setBounds(20, 271, 158, 23);
		frame.getContentPane().add(txtNewRoom);
		txtNewRoom.setColumns(10);

		btnConnect = new JButton("Connect");
		btnConnect.setForeground(new Color(255, 0, 255));
		btnConnect.setBackground(new Color(139, 69, 19));
		btnConnect.setFont(new Font("Kristen ITC", Font.PLAIN, 11));
		btnConnect.setBounds(8, 47, 85, 23);
		frame.getContentPane().add(btnConnect);

		lblMyRooms = new JLabel("My Rooms");
		lblMyRooms.setForeground(new Color(0, 255, 255));
		lblMyRooms.setFont(new Font("Kristen ITC", Font.PLAIN, 11));
		lblMyRooms.setHorizontalAlignment(SwingConstants.CENTER);
		lblMyRooms.setBounds(540, 3, 158, 23);
		frame.getContentPane().add(lblMyRooms);

		lblMyFriends = new JLabel("My Friends");
		lblMyFriends.setForeground(new Color(0, 255, 255));
		lblMyFriends.setFont(new Font("Kristen ITC", Font.PLAIN, 11));
		lblMyFriends.setHorizontalAlignment(SwingConstants.CENTER);
		lblMyFriends.setBounds(540, 163, 158, 25);
		frame.getContentPane().add(lblMyFriends);

		textAreaMessage = new JTextArea();
		textAreaMessage.setLineWrap(true);
		textAreaMessage.setForeground(new Color(31, 191, 189));
		textAreaMessage.setBackground(new Color(245, 222, 179));
		textAreaMessage.setFont(new Font("Kristen ITC", Font.BOLD, 13));
		textAreaMessage.setBounds(200, 261, 334, 23);
		frame.getContentPane().add(textAreaMessage);

		btnSend = new JButton("Send");
		btnSend.setForeground(new Color(255, 0, 255));
		btnSend.setBackground(new Color(139, 69, 19));
		btnSend.setFont(new Font("Kristen ITC", Font.PLAIN, 11));
		btnSend.setEnabled(false);
		btnSend.setBounds(200, 295, 81, 23);
		frame.getContentPane().add(btnSend);

		btnWhisper = new JButton("Whisper");
		btnWhisper.setForeground(new Color(255, 0, 255));
		btnWhisper.setBackground(new Color(139, 69, 19));
		btnWhisper.setFont(new Font("Kristen ITC", Font.PLAIN, 11));
		btnWhisper.setEnabled(false);
		btnWhisper.setBounds(282, 295, 81, 23);
		frame.getContentPane().add(btnWhisper);

		btnLeave = new JButton("Leave");
		btnLeave.setForeground(new Color(255, 0, 255));
		btnLeave.setBackground(new Color(139, 69, 19));
		btnLeave.setFont(new Font("Kristen ITC", Font.PLAIN, 11));
		btnLeave.setEnabled(false);
		btnLeave.setBounds(447, 295, 89, 23);
		frame.getContentPane().add(btnLeave);

		btnEncrypt = new JButton("Encrypt");
		btnEncrypt.setForeground(new Color(255, 0, 255));
		btnEncrypt.setBackground(new Color(139, 69, 19));
		btnEncrypt.setFont(new Font("Kristen ITC", Font.PLAIN, 11));
		btnEncrypt.setEnabled(false);
		btnEncrypt.setBounds(364, 295, 81, 23);
		frame.getContentPane().add(btnEncrypt);

		scrollPaneRooms = new JScrollPane();
		scrollPaneRooms.setBounds(20, 99, 158, 144);
		frame.getContentPane().add(scrollPaneRooms);

		listRooms = new JList<String>();
		listRooms.setForeground(new Color(31, 191, 189));
		listRooms.setBackground(new Color(245, 222, 179));
		listRooms.setFont(new Font("Kristen ITC", Font.PLAIN, 11));
		scrollPaneRooms.setViewportView(listRooms);

		scrollPane = new JScrollPane();
		scrollPane.setBounds(200, 47, 334, 202);
		frame.getContentPane().add(scrollPane);

		textAreaConvo = new JTextArea();
		textAreaConvo.setLineWrap(true);
		textAreaConvo.setEditable(false);
		textAreaConvo.setForeground(new Color(31, 191, 189));
		textAreaConvo.setBackground(new Color(245, 222, 179));
		scrollPane.setViewportView(textAreaConvo);
		textAreaConvo.setFont(new Font("Kristen ITC", Font.BOLD, 13));

		lblBig = new JLabel("");
		lblBig.setForeground(new Color(0, 255, 255));
		lblBig.setFont(new Font("Lucida Handwriting", Font.PLAIN, 18));
		lblBig.setHorizontalAlignment(SwingConstants.CENTER);
		lblBig.setBounds(200, 3, 334, 36);
		frame.getContentPane().add(lblBig);

		scrollPaneMyRooms = new JScrollPane();
		scrollPaneMyRooms.setBounds(540, 26, 158, 126);
		frame.getContentPane().add(scrollPaneMyRooms);

		listMyRooms = new JList<String>();
		listMyRooms.setForeground(new Color(31, 191, 189));
		listMyRooms.setBackground(new Color(245, 222, 179));
		scrollPaneMyRooms.setViewportView(listMyRooms);
		listMyRooms.setFont(new Font("Kristen ITC", Font.PLAIN, 11));

		scrollPaneMyFriends = new JScrollPane();
		scrollPaneMyFriends.setBounds(540, 192, 158, 126);
		frame.getContentPane().add(scrollPaneMyFriends);

		listMyFriends = new JList<String>();
		listMyFriends.setForeground(new Color(31, 191, 189));
		listMyFriends.setBackground(new Color(245, 222, 179));
		scrollPaneMyFriends.setViewportView(listMyFriends);
		listMyFriends.setFont(new Font("Kristen ITC", Font.PLAIN, 11));

	}
	
	public void setExitState(){
		btnExit.setEnabled(false);
		btnConnect.setEnabled(true);
		btnEncrypt.setEnabled(false);
		btnCreate.setEnabled(false);
		btnSend.setEnabled(false);
		btnWhisper.setEnabled(false);
		btnLeave.setEnabled(false);
		btnJoin.setEnabled(false);
	}

	public void add_actions(){
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				connect();
			}
		});

		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				command.exit(userId);
				setExitState();
				btnExit.setEnabled(false);
				btnConnect.setEnabled(true);
				btnEncrypt.setEnabled(false);
				btnCreate.setEnabled(false);
				btnSend.setEnabled(false);
				btnWhisper.setEnabled(false);
				btnLeave.setEnabled(false);
				btnJoin.setEnabled(false);
			}
		});

		btnJoin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				command.joinRoom(listRooms.getSelectedValue(), userId, userName);
			}
		});

		btnCreate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				command.createRoom(txtNewRoom.getText(), userId, userName);
			}
		});

		txtUserName.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) { 
				if (txtUserName.getText().length() >= 10 ) 
					e.consume(); 
			}  
		});

		txtNewRoom.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) { 
				if (txtNewRoom.getText().length() >= 15 )
					e.consume(); 
			}  
		});

		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				command.sendMessage(currRoomId, userId, userName, textAreaMessage.getText());
			}
		});

		btnLeave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				command.leave(currRoomId, userId, userName);
			}
		});

		btnWhisper.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				command.whisperMessage(currRoomId, userId, userName, listMyFriends.getSelectedValue(), textAreaMessage.getText());
			}
		});

		btnEncrypt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				command.setEncryption();
			}
		});

		MouseListener mouseListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 1) {

					String selectedItem = (String) listMyRooms.getSelectedValue();
					int id = rooms.get(roomNameMap.get(selectedItem)).roomId;
					command.setDecryption();

					setRoom(id);
				}
			}
		};

		listMyRooms.addMouseListener(mouseListener);


	}
}


