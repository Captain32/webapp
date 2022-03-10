package webapp;

public class Message {
    private static String domain = System.getenv("DOMAIN");
    private String recipient;
    private String token;


    public Message(String recipient, String token) {
        this.recipient = recipient;
        this.token = token;
    }

    @Override
    public String toString() {
        return recipient + " " + domain + " " + token;
    }
}
