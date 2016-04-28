package edu.scu.eventshare;

/**
 * Created by shilpa on 03/07/16.
 */
public class ChatMessage {
    public boolean left;
    public String message;

    public ChatMessage(boolean left, String message) {
        this.left = left;
        this.message = message;
    }
    public String getMessage(){
        return  this.message;
    }

}
