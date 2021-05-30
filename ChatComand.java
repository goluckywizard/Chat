import java.io.Serializable;
enum CommandType {
    MESSAGE,
    LOGIN,
    LIST,
    SUCCESS,
    CONNECTION_CLOSE,
    ERROR
}

public class ChatComand implements Serializable {
    CommandType command;
    String parameter;
    String date;
    String clientName;

    public ChatComand(String type) {
        if (type.equals("send"))
            command = CommandType.MESSAGE;
        if (type.equals("list"))
            command = CommandType.LIST;
        if (type.equals("login"))
            command = CommandType.LOGIN;
        if (type.equals("success"))
            command = CommandType.SUCCESS;

    }
    public ChatComand(String name, String parameter) {
        if (name.equals("send"))
            command = CommandType.MESSAGE;
        if (name.equals("list"))
            command = CommandType.LIST;
        if (name.equals("login"))
            command = CommandType.LOGIN;
        if (name.equals("success"))
            command = CommandType.SUCCESS;

        this.parameter = parameter;
    }
    public ChatComand(String name, String parameter, String clientName) {
        if (name.equals("send"))
            command = CommandType.MESSAGE;
        if (name.equals("list"))
            command = CommandType.LIST;
        if (name.equals("login"))
            command = CommandType.LOGIN;
        if (name.equals("success"))
            command = CommandType.SUCCESS;

        this.parameter = parameter;
        this.clientName = clientName;
    }

    public void setParameter(String str) {
        parameter = str;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }
}
