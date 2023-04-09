package model;

public class Response {
    private String response;
    private Object value;
    private String reason;

    public Response(String response){
        this.response = response;
    }
    public Response(String response, Object value){
        this(response);
        this.value = value;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "Response{" +
                "response='" + response + '\'' +
                ", value=" + value +
                ", reason='" + reason + '\'' +
                '}';
    }
}
