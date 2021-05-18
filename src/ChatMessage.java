import java.io.Serializable;

/**
 * ChatMessage.java
 *
 * General Message Holder
 *
 * @author abackfis@purdue.edu, hmohite@purdue.edu
 * @version 04/26/2020
 */
final class ChatMessage implements Serializable {
    private static final long serialVersionUID = 6898543889087L;

    private String message;
    private int type;
    private String recipient;

    public ChatMessage(String message, int type) {
        this.message = message;
        this.type = type;
    }

    public ChatMessage(String message, int type, String recipient) {
        this.message = message;
        this.type = type;
        this.recipient = recipient;
    }

    public ChatMessage(String message) {
        this.message = message;
        this.type = 0;
    }

    public ChatMessage() {
        this.message = "";
        this.type = 0;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    @Override
    public String toString() {
        return getMessage();
    }

    // Here is where you should implement the chat message object.
    // Variables, Constructors, Methods, etc.
}
