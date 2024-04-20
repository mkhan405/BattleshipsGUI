import java.io.Serializable;

public class Message implements Serializable {
    static final long serialVersionUID = 42L;
    String userMessage;
    String messageType;

    String userName;

    String groupName = null;
    String recipient = null;


    public String getGroupName() {
        return groupName;
    }

    public String getRecipient() {
        return recipient;
    }


    public Message(){}

    public String getuserMessage(){
        return userMessage;
    }

    public String getmessageType(){
        return messageType;
    }

    public String getuserName(){
        return userName;
    }

}
