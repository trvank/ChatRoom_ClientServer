Table of Contents
1.	Introduction	3
1.1	Server	3
1.2	Rooms	3
1.3	Client	3
2.	My IRC Specification	4
2.1	Character Codes	4
2.2	Messages	4
3.	Server Messages	4
3.1	Initial startup message	4
3.2	NEW	5
3.3	JOIN	5
3.4	NORMAL	6
3.5	ENCRYPTED_NORMAL	6
3.6	WHISPER	7
3.7	WHISPER_ENCRYPTED	7
3.8	LEAVE	8
3.9	EXIT	9
4.	Client Messages	9
4.1	Initial Startup Message	9
4.2	NEWROOM	10
4.3	JOINROOM	10
4.4	ROOMS	11
4.5	FRIENDS	11
4.6	MESSAGE	11
4.7	ENCRYPTED	12
4.8	EXIT	12
5.	Error Handling	12
5.1	Client Crash	12
5.2	Server Crash	13
5.3	NULL data errors	13
6.	“Extra Features”	13
7.	Future Work	13

1.	Introduction
The My IRC is designed using Java along with swing and windowbuilder APIs.  This is a Graphical User Interface (GUI) window application.  Additionally, the system was built using the TCP/IP network protocol through Java socket calls.

The My IRC application is a text communication system with a client/server base.  The server must be running for clients to utilize the system.  Clients will start the application and will connect to the server.  The client can join an existing room (displayed on the opening screen once connected) or they may start a new room.  Data from the client will be delivered to the Server which will send the message to all users connected to the room.  Special features include “whisper” and “encryption” functionality.  A user may choose to “whisper” a message to a select user in the room so that other users will not see the information.  Also, a user may “encrypt” a message so that a secure message is sent to/from the server and is decrypted when it reaches the end user(s).

1.1	Server
The server is the controller of the information.  All clients will be connected to each other and communicate through this point.  The server connection will be done through port 118 at the server IP address.  
1.2	Rooms
Server will maintain the rooms and deliver the messages to the IP addresses signed in to the room.  A list of rooms will be stored in the server.  The ‘room’ will contain a room id number, a room name, a list of user names, a list of connection sockets, and an active flag.  The active flag will indicate if the room has any users and if that slot/id is ok to assign to a new room.  Rooms will be set up within the Server via room id numbers.  When clients connect to the server port, the server will communicate current rooms.  The client will communicate back with an existing room (or a new room).  The server will respond with a port number for an existing room or it will set up and communicate a new room id for a new room.  
1.3	Client
Clients will connect to the server via the main port specified in the application.  Client ‘user’ list will be maintained by the server.  A ‘user’ contains a user id, a user name, a list of rooms that the user is connected to, and an active flag.  When adding rooms and/or users, the active flag will indicate a free slot in the list which can be reused. Client will have a user name that will be their identifier for the server to send their message to all others in the room communicated by the user.  Client may join or start multiple rooms in a session.
2.	My IRC Specification
2.1	Character Codes
Clients will use ASCII text for both the message and user name text fields.  There is one restriction on character strings that the user may use.  Because some communication over the socket will be commands and the client server applications will need to separate some text strings, the string “@&@&” will be used as a delimiter and must not be input from the user.  The application checks to ensure the splitter is not input by the user.

2.2	Messages

With the exception of the initial setup message, messages will be sent in the following format:

COMMAND@&@&@<info>.........@&@&<message>

where <info> may contain:
•	User Id
•	Room Id
•	Room Name
•	User Name (for user to send secret message to)
•	User Name (for user sending the message)
•	Message

 and where the COMMAND tells the client or the server what course of action should be taken
3.	Server Messages
This section will describe the messages that the Server RECEIVS from the client and what actions the Server takes when parsing the command
3.1	Initial startup message
No commands are received in the initial startup message.  This is the first communication that the Client makes with the Server to establish a connection.
3.1.1	Usage
On connection, a new socket is created through which all communication with this client will take place.   The server creates a user id for the socket connection.  An empty slot in the user list is searched for as an index.  If no empty slot is available, a new user slot is added to the end of the list.
The server also adds the room to a list of active rooms in the rooms list.  The server immediately (without command from the Client) sends the user Id and the list of active rooms to the Client through the socket.  
Finally, the server starts a new thread using the socket which will listen for commands from the Client.  With the new thread running, the Server is now free to listen for new incoming client connections.
3.2	NEW
NEW@&@&<roomName>@&@&<userId>@&@&<userName>
3.2.1	Usage
This command is received when a user creates a new room.  The Server ensures that the name is not already in use; if a room with that name already exists, the Server appends the room name with a (#) where the “#” is the number of rooms of the same name. For example, if the rooms ‘Movies’ and ‘Movies(1)’ already exist, the new room will be ‘Movies(2)’.  
The Server also looks for an open slot in the rooms list so that any “inactive” room id can be reused for this new room.  If no open slot (id) is found, then new room as added to the end of the rooms list.  
After the room id (slot number) is found, the room id is added to the user’s personal room list in the Server’s list of User information.
3.2.2	Response
The Server sends a message back to the Client that created the new room informing the client of the room Id and the room name, which is necessary in the event that the requested room name already exists.
The Server sends a list of updated active users in the room to each of the active user’s socket so all Clients in the room have visibility to who they are chatting with. Note: in the case of the New Room, the creator is the only active client in the room.
The Server sends a list of updated available rooms to all active users connected to the server so that all Clients have visibility to active rooms that they can join.
3.2.3	Definitions
•	roomName  – String that names the room that the user creates
•	userId – Client’s id allows Server to update Client’s room information at the Server 
•	userName – Client’s user name that will be added to the room information stored on the Server.  This is necessary to send active users to all Clients connected to the room
3.3	JOIN
JOIN@&@&<roomName>@&@&<userId>@&@&<userName>
3.3.1 	Usage
This command is received when a user joins an existing room.  The Server retrieves the room Id from its data structures.  The Server then adds the user information to the room and adds the room information to the user slot in the user list.  
3.2.2	Response
The Server sends a message back to the Client that joined the new room informing the client of the room Id.
The Server sends a list of updated active users in the room to each of the active user’s socket so all Clients in the room have visibility to who they are chatting with.
The Server sends a list of updated available rooms to all active users connected to the server so that all Clients have visibility to active rooms that they can join.
3.3.3	Definitions
•	roomName – the name of the room the client wishes to join.  This name is used in a hashmap to obtain the room id, which also acts as an index to the room in the room list.  The index allows the Server to update the room information.
•	userId – the Client’s Id, which also acts as an index to the user’s information in the user list.  The index allows the Server to update the user information.
•	userName – Client’s user name that will be added to the room information stored on the Server.  This is necessary to send updated active user lists to all sockets in the room.
3.4	NORMAL
NORMAL@&@&<roomId>@&@&<userId>@&@&<message>
3.4.1	Usage
This command is received by the Server when a Client is sending a ‘normal’ message.  A ‘normal’ message is not encrypted and is sent to all Clients active in the room.  
3.4.2	Response
The Server uses the room Id to get the sockets for all Clients active in the room.  Then, the message is sent to all active Clients via each client’s socket.  The Server replies to the Clients letting them know that this is a ‘normal’ message and does not need to be decrypted
3.4.3	Definitions
•	roomId – the room id for the room that the client wants his/her message sent.  The room id is an index into the room list where the room’s information is stored 
•	userId – the Client’s Id, which also acts as an index to the user’s information in the user list.  The index allows the Server to update the user information.
•	message – the message that should be sent to each Client active in the specified room, including the Client that originally sent the message
3.5	ENCRYPTED_NORMAL
ENCRYPTED_NORMAL@&@&<roomId>@&@&<userId>@&@&<message>
3.5.1	Usage
This command is received by the Server when a Client is sending an ‘encrypted’ message.  An ‘encrypted’ message has been encrypted so that the message is secure when traveling to/from the server and while the message is at the server.  The encryption makes the message appear as gibberish if it is somehow hijacked between when it is sent from the sender and when it is received by the receiving Client(s).
3.5.2	Response
The Server uses the room Id to get the sockets for all Clients active in the room.  Then, the message is sent to all active Clients via each client’s socket.  The Server replies to the Clients letting them know that this is an ‘encrypted’ message and does need to be decrypted before it is displayed to the Client.
3.5.3	Definitions
•	roomId – the room id for the room that the client wants his/her message sent.  The room id is an index into the room list where the room’s information is stored 
•	userId – the Client’s Id, which also acts as an index to the user’s information in the user list.  The index allows the Server to update the user information.
•	message – the encrypted message that should be sent to each Client active in the specified room, including the Client that originally sent the message
3.6	WHISPER
WHISPER@&@&<roomId>@&@&<fromUserId>@&@&<toUserName>@&@&<message>
3.6.1	Usage
This command is received by the Server when a Client is sending a secret unencrypted message to a single user from the rooms active Client’s.  Only the selected user, which is communicated from the Client sending the message, and the sender will receive the message from the Server.  All other Client’s will receive nothing from the Server and will not be aware that this message was sent.
3.6.2	Response
The Server uses the room Id to get the socket for the single Client that the sender wishes to send the secret message to.  Then, the message is sent to the Client via the socket.  The Server also sends the message back to the sender via the sender’s Client socket.  The Server message sent both Clients includes information letting them know that this is not an ‘encrypted’ message and does not need to be decrypted before it is displayed to the Client.
3.6.3	Definitions
•	roomId – the room id for the room that the client wants his/her message sent.  The room id is an index into the room list where the room’s information is stored 
•	fromUserId – the Client’s Id, which also acts as an index to the user’s information in the user list.  The index allows the Server to update the user information.
•	toUserName – the Client name of the single user the sender wishes to send the message to.  This is used to get a specific socket from the list of all active sockets in the room
•	message – the message that should be sent a single receiving Client active and to the sending Client in the specified room
3.7	WHISPER_ENCRYPTED
WHISPER_ENCRYPTED@&@&<roomId>@&@&<fromUserId>@&@&<toUserName>@&@&<message>
3.7.1	Usage
This command is received by the Server when a Client is sending a secret ‘encrypted’ message to a single user from the rooms active Client’s.  Only the selected user, which is communicated from the Client sending the message, and the sender will receive the ‘encrypted’ message from the Server.  All other Client’s will receive nothing from the Server and will not be aware that this message was sent.
3.7.2	Response
The Server uses the room Id to get the socket for the single Client that the sender wishes to send the secret message to.  Then, the message is sent to the Client via the socket.  The Server also sends the message back to the sender via the sender’s Client socket.  The Server message sent both Clients includes information letting them know that this is an ‘encrypted’ message and does need to be decrypted before it is displayed to the Client.
3.7.3	Definitions
•	roomId – the room id for the room that the client wants his/her message sent.  The room id is an index into the room list where the room’s information is stored 
•	fromUserId – the Client’s Id, which also acts as an index to the user’s information in the user list.  The index allows the Server to update the user information.
•	toUserName – the Client name of the single user the sender wishes to send the message to.  This is used to get a specific socket from the list of all active sockets in the room
•	message – the ‘encrypted’ message that should be sent a single receiving Client active and to the sending Client in the specified room
3.8	LEAVE
LEAVE@&@&<roomId>@&@&<userId>@&@&<message>
3.8.1	Usage
This message is received by the Server when a Client leaves one room.  The Client remains connected to the Server and remains active in all other room in which they were already active.
3.8.2	Response
The Server accesses the room from its room list using the room Id as an index.  Then the Server removes the user from the room using the user Id.  Removing the user from the room means removing the Client’s socket, user name, and user id from the lists maintained in the room.  The Server must also make the room ‘inactive’ if there are no more users in the room.
If the room is no longer active, the Server sends a new list of active rooms to all Clients connected to the Server so that Clients no longer have visibility to an inactive room.
If the room is still active with other Clients, the Server sends an updated friends list to all Clients active in the room so that the Clients are aware that the user that “left” is no longer seeing messages in the room.  Additionally, a ‘normal’ message is sent to the remaining active Clients in the room so that a “___ has left the room” message will display to inform them of the change.
Finally, the Client’s user Id is used to access the user information in the Server and the room is removed from the Server’s view of the Client’s active rooms.
3.8.3	Definitions
•	roomId – the room Id of the room that the Client is leaving.  This is used as an index to the room list where the Client information is removed from the room.  This is also used to gather the updated friends list that is sent to remaining active Clients
•	userId – the Client’s user Id that is used as an index to access the user information and update the Client’s active rooms
•	message – the message that is sent and displayed to the remaining users in the room that informs them who has left the room
3.9	EXIT
LEAVE@&@&<userId>
3.9.1	Usage
This message is received by the Server when a Client disconnects from the Server.  This obviously means that the Client must be removed from any room in which they were active.
3.9.2	Response
The Server uses the Client’s user Id to access a list of all room Ids for which the user is active.
The Server accesses each the rooms from its room list using the room Id as an index.  Then the Server removes the user from the room using the user Id.  Removing the user from the room means removing the Client’s socket, user name, and user id from the lists maintained in the room.  The Server must also make the room ‘inactive’ if there are no more users in the room.
If the room is no longer active, the Server sends a new list of active rooms to all Clients connected to the Server so that Clients no longer have visibility to an inactive room.
If the room is still active with other Clients, the Server sends an updated friends list to all Clients active in the room so that the Clients are aware that the user that “left” is no longer seeing messages in the room.  
Finally, the Client’s user Id is used to access the user information in the Server and the room is removed from the Server’s view of the Client’s active rooms.  The Client is also marked as ‘inactive’ so that the user Id can be reused.
3.9.3	Definitions
•	roomId – the room Id of the room that the Client is leaving.  This is used as an index to the room list where the Client information is removed from the room.  This is also used to gather the updated friends list that is sent to remaining active Clients
•	userId – the Client’s user Id that is used as an index to access the user information and update the Client’s active rooms
4.	Client Messages
This section will describe the messages that the Client RECEIVS from the Server and what actions the Client takes when parsing the command
4.1	Initial Startup Message
No Commands are received in the startup message.  This first message assigns the Client a user Id which the Client saves.  A list of currently active rooms is also sent.
4.1.1	Usage
The client hits the ‘Connect’ button which establishes a connection with the Server.  The Client then receives a user Id from the Server which sets the user Id on the Client side and is used for communication with the Server.  
The Client also receives in this initial ‘connection’ a list of rooms that are currently available to join at the Server.  
4.2	NEWROOM
NEWROOM@&@&<roomId>@&@&<roomName>
4.2.1	Usage
This message is received when the Client has chosen to start a new room.  The Server sets up the room and assigns the room an Id and, if the requested name is already used, a new name (the original name with a “(#)” appended to the end).  These two identifiers are communicated to the Client from the server.
4.2.2	Response
When the Client receives this message, a new room is created and stored in the Client’s room list.  The Client also adds the name to a list of room names that they are currently active in.  Hashmaps are updated to keep track of the index into the room list and the room name list.  The list of rooms is important so that the state of the room can be maintained when the Client is viewing a different room.
Finally, the Client view is set to the freshly created room so they can begin chatting.
4.2.3	Definitions
•	roomId – the room Id that the Server has assigned to the room
•	roomName – the name of the room that the Client has created.  This must be communicated back from the Server because if the name is already in use, the server appends an identifier to the room so that there are not two rooms with the same name.
4.3	JOINROOM
JOINROOM@&@&<roomId>@&@&<roomName>
4.3.1	Usage
This message is received when the Client has chosen to join an existing room.  The Server verifies the room based on the name selected from the available rooms list.  These two identifiers are communicated to the Client from the server.
4.3.2	Response
When the Client receives this message, a new room is created and stored in the Client’s room list.  The Client also adds the name to a list of room names that they are currently active in.  Hashmaps are updated to keep track of the index into the room list and the room name list.  The list of rooms is important so that the state of the room can be maintained when the Client is viewing a different room.
Finally, the Client view is set to the joined room so they can begin chatting.
4.3.3	Definitions
•	roomId – the room Id that the Server has assigned to the room
•	roomName – the name of the room that the Client has joined
4.4	ROOMS
NEWROOM@&@&<rooms>
4.4.1	Usage
This message is received when any Client that is connected to the Server has chosen to start a new room.  In this case, the new room needs to be communicated to all other Clients as a new available room to join.
This message will also be received when a room has no active users.  In this case, the room needs to be removed from all Clients’ room list.  
4.4.2	Response
When the Client receives this message, a new room is available or a room has been closed.  Client simply updates the available room list with the new list
4.4.3	Definitions
•	rooms – list of rooms that should appear in the Client’s room list
4.5	FRIENDS
FRIENDS@&@&<roomId>@&@&<friends>
4.5.1	Usage
This message is received when the friends list in a room changes.  If a Client creates a new room or joins and existing room, the Server sends a list of friends to all Clients in that room to populate the friends list.  Also, if a Client leaves a room, the Server sends a list of the updated friends to all remaining Clients in the room.
4.5.2	Response
When the Client receives this message, there has been a change in the friends list for one of the rooms in which that Client is active.  The Client uses the roomId to get an index into their room list and then updates the friends list in that Room.  If the room id is the Client’s current view, the new list is populated to reflect the change.  
4.5.3	Definitions
•	roomId – the room Id for the room that has a change in active users
•	friends – the list of friends that needs to be displayed for the Client in the appropriate room
4.6	MESSAGE
MESSAGE@&@&<roomId>@&@&<message>
4.6.1	Usage
Another Client has sent a message that is being received by this Client.  The message needs to be appended to the conversation and displayed if the room is the Client’s current view.
4.6.2	Response
When the Client receives this message, they are receiving a new piece of the conversation in the room specified.  The Client uses the roomId to get the index of the room in their room list.  Then they append the message to the end of the conversation in that room.  If the room Id is the current view, the message is appended to the end of the conversation textbox.    
4.6.3	Definitions
•	roomId – the room Id for the room that is receiving the message
•	message – the text that is being communicated from one Client to the other(s)
4.7	ENCRYPTED
ENCRYPTED@&@&<roomId>@&@&<message>
4.7.1	Usage
Another Client has sent an ‘encrypted’ message that is being received by this Client.  The message needs to be decrypted and then appended to the conversation and displayed if the room is the Client’s current view.
4.7.2	Response
When the Client receives this message, they are receiving a new piece of the conversation in the room specified, however the piece of the conversation has been encrypted and is gibberish.  The message is decrypted.  Then the Client uses the roomId to get the index of the room in their room list.  Then they append the message to the end of the conversation in that room.  If the room Id is the current view, the message is appended to the end of the conversation textbox.    
4.7.3	Definitions
•	roomId – the room Id for the room that has a change in active users
•	message – the ‘encrypted’ text that is being communicated from one Client to the other(s)
4.8	EXIT
EXIT@&@&<userId>
4.8.1	Usage
The Client has chosen to disconnect from the server and close all active rooms.
4.8.2	Response
When the Client receives this message, the Server has closed the socket connection and is letting the Client know that they can close their connection.  The Client closes the connection and removes all rooms.  The Client also sets the view to blank, emptying all lists and text boxes.    
4.8.3	Definitions
•	userId – user Id that has been disconnected from the server
5.	Error Handling
5.1	Client Crash
The Server continues to listen for messages from the Client.  In the event that the Client crashes, the readLine function throws a SocketException.  The exception is caught and lets the server perform its “exit” function in removing the Client from all rooms in which they were active and informing the remaining Clients of any changes to friends or available rooms.  The listening loop is broken and the Server listening thread is finished for that Client.
5.2	Server Crash
The Client continues to listen for messages from the Server.  In the event that the Server crashes, a SocketException is caught.  This exception lets the Client know to stop listening for incoming messages and close the socket.  The socket can be closed and the Client clears all of the rooms and sets their view to blank.
5.3	NULL data errors
The Client cannot be allowed to send null required information in the Commands.  This will cause the Server to encounter a null pointer error.  For example, if the Client sends a message without being active in a room, the Server will encounter an error because it cannot access the room list with a null index.  The following error causing scenarios are handled on the Client side and will not be allowed to be sent to the Server:
1.	Joining a room when no room is selected
2.	Creating a room when the new room text box is blank
3.	Sending a message when there is no active room
4.	Whispering a message when there is no “to user” selected
In the event that the Client tries to send an error resulting message, a prompt for a fix is issued in the form of a pop-up.
6.	“Extra Features”
In addition to the required elements of the project, this application also allows the Client to:
1.	Send encrypted messages
2.	Send messages only to a select user in the friends list
3.	Graphical User Interface
7.	Future Work
This application can be enhanced with the addition of allowing file transfers.  The structure should not need a lot of modification, but would make for extra functionality.  Also, it would be interesting to incorporate sending pictures through this application.
The messages and commands can be enhanced for more uniformity.  If I were to modify this or make this application a second time, I would put the message in a class of its own so that it would be easier to access and so that each command would not have its own form of extras.  This would also eliminate the need for the “SPLITTER” string.
