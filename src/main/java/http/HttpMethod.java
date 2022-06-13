package http;

public enum HttpMethod {
    GET, POST;

    public boolean isPost() {
        return this == POST;
    }
//    public HttpMethod valueOf(String method) {
//        if (method.equals("GET")) {
//            return GET;
//        } else if (method.equals("POST")) {
//            return POST;
//        }
//        return null;
//    }
}