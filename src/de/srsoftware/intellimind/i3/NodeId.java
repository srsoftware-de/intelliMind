package de.srsoftware.intellimind.i3;

import java.util.Date;


public class NodeId {
	private String internalId=null;
  public NodeId(){
	  internalId=String.valueOf((new Date()).getTime());
  }
  public String toString(){
	  return internalId;
  }
}
