package edu.gvsu.restapi.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.Vector;

import edu.gvsu.restapi.client.service.MessageListenerService;
import edu.gvsu.restapi.client.service.MessageSenderService;
import edu.gvsu.restapi.client.utils.Utils;
import edu.gvsu.restapi.shared.RegistrationInfo;

public class App {
    
    
	public static void main(String args[]) {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
        	if(args.length < 1){
        		System.out.println("Wrong # of arguments");
        		System.exit(-1);
        	}
        	BufferedReader is = new BufferedReader(new InputStreamReader(System.in) );
            String chatServerHost = "localhost";
            int chatServerPort = 8080;
            
            
        	String userName = args[0];
            if(args.length == 2){
            	String[] hostAndPort =  args[1].split(":");
            	if(hostAndPort.length == 1){
            		chatServerHost = hostAndPort[0];
            	} else if(hostAndPort.length == 2){
            		chatServerHost = hostAndPort[0];
            		try{
                		chatServerPort = Integer.parseInt(hostAndPort[1]);
                	}catch(NumberFormatException nfe){
                		System.out.println("Port param must be a number");
                		System.exit(-1);
                	}
            	}else if(hostAndPort.length != 0){
            		System.out.println("Wrong # of arguments");
            		System.exit(-1);
            	}
            }
        	
            String line;
            String message;
            boolean finished = false;
            Vector<RegistrationInfo> registeredUsers;
            RegistrationInfo friend;
            ServerSocket listenerServer = null;
            
            listenerServer = new ServerSocket(0);
            
            Thread listenerService = new Thread(new MessageListenerService(listenerServer));
            listenerService.start();
            String localIP = Utils.getIPAddress();
            
            ChatRESTClient chatRESTClient = new ChatRESTClient(chatServerHost, chatServerPort);
            
            RegistrationInfo myself = new RegistrationInfo(userName, localIP, listenerServer.getLocalPort(), true);
            
            if(!chatRESTClient.register(myself)){
            	System.out.println("User " + userName + " already connected, please use another name");
            	listenerService.interrupt();
            	System.exit(-1);
            }
            
            System.out.println("Chat App for " + myself.getName());
            
            while(true) {
        		System.out.println("Please type a command, or 'help' to see the list of available commands ");	
                line = is.readLine();
                String[] lineArray = line.split(" ");
                
                switch(lineArray[0]){
                case "friends":
                	System.out.println("Connected Friends:");
                	registeredUsers = chatRESTClient.listRegisteredUsers();
                	registeredUsers.remove(myself);
                	Utils.printVector(registeredUsers);
                	break;
                case "talk":
                	if(lineArray.length > 2){
	                	System.out.println("Sending Message to " + lineArray[1]);
	                	friend = chatRESTClient.lookup(lineArray[1]);
	                	if(friend != null && friend.getStatus() == true){
	                		message = Utils.stringFromArray(2, lineArray);
	                		message = "(DM) " + myself.getName() + " says: " + message;
	                		new Thread(new MessageSenderService(friend.getHost(), friend.getPort(), message)).start();
	                	}else{
	                		System.out.println("Friend unavailable");
	                	}
                	}else{
                		System.out.println("Invalid number of arguments for talk. Correct syntax is: \ntalk {name} {message}");
                	}
                	
                	break;
                case "broadcast":
                	if(lineArray.length > 1){
                		registeredUsers = chatRESTClient.listRegisteredUsers();
                    	registeredUsers.remove(myself);
                    	for(RegistrationInfo currentFriend: registeredUsers){
                    		if(currentFriend.getStatus() == true){
                    			message = Utils.stringFromArray(1, lineArray);
                    			message = "(Broadcast) " + myself.getName() + " says: " + message;
    	                		new Thread(new MessageSenderService(currentFriend.getHost(), currentFriend.getPort(), message)).start();
                    		}
                    	}
                	}else{
                		System.out.println("Invalid number of arguments for broadcast. Correct syntax is: \nbroadcast {message}");
                	}
                	break;
                case "busy":
                	myself.setStatus(false);
                    chatRESTClient.updateRegistrationInfo(myself);
                    System.out.println("Your status is now busy");
                	break;
                case "available":
                	myself.setStatus(true);
                    chatRESTClient.updateRegistrationInfo(myself);
                    System.out.println("Your status is now available");
                	break;
                case "help":
                    System.out.println("Here's a list of commands:");
                    System.out.println("friends: List all your connected friends");
                    System.out.println("talk {name} {message}: Sends a message to your friend if available");
                    System.out.println("broadcast {message}: Sends a message to all your available friends");
                    System.out.println("busy: Sets your status to busy");
                    System.out.println("available: Sets your status to available");
                    System.out.println("exit: Close the Chat App");
                	break;
                case "exit":
                	listenerService.interrupt();
                	chatRESTClient.unregister(userName);
                	finished = true;
                	break;
                default:
                	System.out.println("Not a recognized command. Type 'help' to see the list of available commands");
                	break;
                	
                }
                if(finished){
                	break;
                }
            }
        	System.out.println("Bye");
        	System.exit(0);
             
        } catch (IOException e) {
        	e.printStackTrace();
        	System.exit(-1);
        } catch (Exception e) {
            System.err.println("Chat App exception:");
            e.printStackTrace();
        }
    }
}
