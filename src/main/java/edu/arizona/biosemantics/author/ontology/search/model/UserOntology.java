package edu.arizona.biosemantics.author.ontology.search.model;

import java.util.ArrayList;
import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserOntology {
	public String userId;
	public ArrayList<String> ontos;
	@JsonCreator
	public UserOntology(@JsonProperty(value="user", required=false)  String userId, 
			@JsonProperty(value="ontologies",required=false) String ontos){
		this.userId = userId;		
		this.ontos = new ArrayList<String>(Arrays.asList(ontos.split(",")));
	}
	
	@JsonCreator
	public UserOntology(){
	}
	
	public String getUserId(){
		return userId;
	}
	
	public ArrayList<String> getUserOntologies(){
		return ontos;
	}
}
