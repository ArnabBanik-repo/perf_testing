package messaging;

public class Message {

    private int id;
    private String message;

    private long enqueueTime;
    private long dequeueTime;

    public Message() {
        id = -1;
        message = null;
    }

    public Message(int id, String message) {
        this.id = id;
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getEnqueueTime() {
        return enqueueTime;
    }

    public void setEnqueueTime(long enqueueTime) {
        this.enqueueTime = enqueueTime;
    }

    public long getDequeueTime() {
        return dequeueTime;
    }

    public void setDequeueTime(long dequeueTime) {
        this.dequeueTime = dequeueTime;
    }

    public void reset() {
        this.id = -1;
        this.message = null;
        this.enqueueTime = 0;
        this.dequeueTime = 0;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", message='" + message + '\'' +
                '}';
    }
}
