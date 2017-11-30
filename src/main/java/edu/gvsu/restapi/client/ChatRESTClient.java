package edu.gvsu.restapi.client;

import java.io.IOException;
import java.util.Vector;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import edu.gvsu.restapi.shared.RegistrationInfo;

public class ChatRESTClient
{
	private static Logger logger = Logger.getLogger("edu.gvsu.restapi.client.ChatRESTClient");
	private static boolean DEBUG = true;
	// The base URL for all requests.
    private static final String APPLICATION_URI = "https://upheld-clone-186300.appspot.com";
    // The base URL for all requests.
    private static final String API_VERSION = "v1";
    
    public ChatRESTClient(String host, int port){
    	//APPLICATION_URI =  "http://"+host+":"+port;
    	
    }
    
    public boolean register(RegistrationInfo reg){
    	boolean success = false;
		// This is how you create a www form encoded entity for the HTTP POST request.
	    Form form = new Form();
	    form.add("name",reg.getName());
	    form.add("host",reg.getHost());
	    form.add("port",String.valueOf(reg.getPort()));
	    form.add("status",String.valueOf(reg.getStatus()));

	    // construct request to create a new widget resource
	    String usersResourceURL = APPLICATION_URI + "/" + API_VERSION + "/users";
	    Request request = new Request(Method.POST, usersResourceURL);
	    // We need to ask specifically for JSON
        request.getClientInfo().getAcceptedMediaTypes().
        add(new Preference<MediaType>(MediaType.APPLICATION_JSON));

	    // set the body of the HTTP POST command with form data.
	    request.setEntity(form.getWebRepresentation());

	    // Invoke the client HTTP connector to send the POST request to the server.
	    if(DEBUG)
	    	System.out.println("Sending an HTTP POST to " + usersResourceURL + ".");
	    Response resp = new Client(Protocol.HTTPS).handle(request);

	    // now, let's check what we got in response.
	    if(resp.getStatus().equals(Status.SUCCESS_OK)) {
	    	success = true;
	    }

    	return success;
    }
    
    public boolean updateRegistrationInfo(RegistrationInfo reg){
    	boolean success = false;
		// This is how you create a www form encoded entity for the HTTP POST request.
	    Form form = new Form();
	    form.add("status",String.valueOf(reg.getStatus()));

	    // construct request to create a new widget resource
	    String usersResourceURL = APPLICATION_URI + "/" + API_VERSION + "/users/"+reg.getName();
	    Request request = new Request(Method.PUT, usersResourceURL);
	    // We need to ask specifically for JSON
//        request.getClientInfo().getAcceptedMediaTypes().
//        add(new Preference<MediaType>(MediaType.APPLICATION_JSON));

	    // set the body of the HTTP POST command with form data.
	    request.setEntity(form.getWebRepresentation());

	    // Invoke the client HTTP connector to send the POST request to the server.
	    if(DEBUG)
	    	System.out.println("Sending an HTTP PUT to " + usersResourceURL + ".");
	    Response resp = new Client(Protocol.HTTPS).handle(request);

	    // now, let's check what we got in response.
	    if(resp.getStatus().equals(Status.SUCCESS_OK)) {
	    	success = true;
	    }

    	return success;
    }
    
    public void unregister(String name){
    	// construct request to create a new widget resource
	    String userResourceURL = APPLICATION_URI + "/" + API_VERSION + "/users/"+name;
	    Request request = new Request(Method.DELETE, userResourceURL);
	    // We need to ask specifically for JSON
        request.getClientInfo().getAcceptedMediaTypes().
        add(new Preference<MediaType>(MediaType.APPLICATION_JSON));
	    
        // Invoke the client HTTP connector to send the POST request to the server.
        if(DEBUG)
        	System.out.println("Sending an HTTP DELETE to " + userResourceURL + ".");
	    Response resp = new Client(Protocol.HTTPS).handle(request);
	    if(DEBUG)
	    	System.out.println(resp.getStatus());
    }
    
    public RegistrationInfo lookup(String name){
    	RegistrationInfo user = null;
    	// construct request to create a new widget resource
	    String userResourceURL = APPLICATION_URI + "/" + API_VERSION + "/users/"+name;
	    Request request = new Request(Method.GET, userResourceURL);
	    // We need to ask specifically for JSON
        request.getClientInfo().getAcceptedMediaTypes().
        add(new Preference<MediaType>(MediaType.APPLICATION_JSON));
        
	    // Invoke the client HTTP connector to send the POST request to the server.
        if(DEBUG)
        	System.out.println("Sending an HTTP GET to " + userResourceURL + ".");
	    Response resp = new Client(Protocol.HTTPS).handle(request);
	    if(DEBUG)
	    	System.out.println(resp.getStatus());
	    // Let's see what we got!
 		if(resp.getStatus().equals(Status.SUCCESS_OK)) {
 			try{
	 			Representation responseData = resp.getEntity();
	 			String jsonString = responseData.getText().toString();
	 			if(DEBUG)
	 				System.out.println("result text=" + jsonString);
				JSONObject jObj = new JSONObject(jsonString);
				user = new RegistrationInfo(jObj.getString("name"),jObj.getString("host"),jObj.getInt("port"),jObj.getBoolean("status"));
 			}catch(IOException ioe){
 				ioe.printStackTrace();
 			}
 		}
    	return user;
    }
    
    public Vector<RegistrationInfo> listRegisteredUsers(){
    	Vector<RegistrationInfo> registeredUsers = new  Vector<>();
    	// construct request to create a new widget resource
	    String usersResourceURL = APPLICATION_URI + "/" + API_VERSION + "/users";
	    Request request = new Request(Method.GET,usersResourceURL);

	    // We need to ask specifically for JSON
        request.getClientInfo().getAcceptedMediaTypes().
        add(new Preference<MediaType>(MediaType.APPLICATION_JSON));

	    // Now we do the HTTP GET
        if(DEBUG)
        	System.out.println("Sending an HTTP GET to " + usersResourceURL + ".");
	    Response resp = new Client(Protocol.HTTPS).handle(request);

		// Let's see what we got!
		if(resp.getStatus().equals(Status.SUCCESS_OK)) {
			Representation responseData = resp.getEntity();
			if(DEBUG)
				System.out.println("Status = " + resp.getStatus());
			try {
				String jsonString = responseData.getText().toString();
				if(DEBUG)
					System.out.println("result text=" + jsonString);
				JSONArray jArray = new JSONArray(jsonString);
				for(Object obj:jArray){
					JSONObject jObj = (JSONObject)obj;
					RegistrationInfo user = new RegistrationInfo(jObj.getString("name"),jObj.getString("host"),jObj.getInt("port"),jObj.getBoolean("status"));
					registeredUsers.addElement(user);
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException je) {
				je.printStackTrace();
			}
		}
    	return registeredUsers;
    }

}
