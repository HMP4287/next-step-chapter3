package http;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;

public class HttpResponse {
    private static final Logger log = LoggerFactory.getLogger(http.HttpResponse.class);
    private DataOutputStream dos = null;

    public HttpResponse(OutputStream out) {
        dos = new DataOutputStream(out);
    }

    public void response(HttpRequest httpRequest) throws IOException {
        // res to res
        String url = getDefaultUrl(httpRequest.tokens);
        if ("/user/create".equals(url)) {
            String body = IOUtils.readData(httpRequest.br, httpRequest.contentLength);
            Map<String, String> params = HttpRequestUtils.parseQueryString(body);
            User user = new User(params.get("userId"), params.get("password"), params.get("name"),
                    params.get("email"));
            log.debug("user : {}", user);
            DataBase.addUser(user);
//            DataOutputStream dos = new DataOutputStream(out);
            response302Header(dos);
        } else if ("/user/login".equals(url)) {
            String body = IOUtils.readData(httpRequest.br, httpRequest.contentLength);
            Map<String, String> params = HttpRequestUtils.parseQueryString(body);
            User user = DataBase.findUserById(params.get("userId"));
            if (user != null) {
                if (user.login(params.get("password"))) {
//                    DataOutputStream dos = new DataOutputStream(out);
                    response302LoginSuccessHeader(dos);
                } else {
                    responseResource("/user/login_failed.html");
                }
            } else {
                responseResource("/user/login_failed.html");
            }
        } else if ("/user/list".equals(url)) {
            if (!httpRequest.logined) {
                responseResource("/user/login.html");
                return;
            }

            Collection<User> users = DataBase.findAll();
            StringBuilder sb = new StringBuilder();
            sb.append("<table border='1'>");
            for (User user : users) {
                sb.append("<tr>");
                sb.append("<td>" + user.getUserId() + "</td>");
                sb.append("<td>" + user.getName() + "</td>");
                sb.append("<td>" + user.getEmail() + "</td>");
                sb.append("</tr>");
            }
            sb.append("</table>");
            byte[] body = sb.toString().getBytes();
//            DataOutputStream dos = new DataOutputStream(out);
            response200Header(dos, body.length);
            responseBody(dos, body);
        } else if (url.endsWith(".css")) {
            responseCssResource(url);
        } else {
            responseResource(url);
        }
    }

    private String getDefaultUrl(String[] tokens) {
        String url = tokens[1];
        if (url.equals("/")) {
            url = "/index.html";
        }
        return url;
    }

//    private void responseResource(OutputStream out, String url) throws IOException {
//        DataOutputStream dos = new DataOutputStream(out);
//        byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
//        response200Header(dos, body.length);
//        responseBody(dos, body);
//    }
    private void responseResource(String url) throws IOException {
//        DataOutputStream dos = new DataOutputStream(out);
        byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
        response200Header(dos, body.length);
        responseBody(dos, body);
    }

//    private void responseCssResource(OutputStream out, String url) throws IOException {
//        DataOutputStream dos = new DataOutputStream(out);
//        byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
//        response200CssHeader(dos, body.length);
//        responseBody(dos, body);
//    }

    private void responseCssResource(String url) throws IOException {
//        DataOutputStream dos = new DataOutputStream(out);
        byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
        response200CssHeader(dos, body.length);
        responseBody(dos, body);
    }
    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200CssHeader(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Location: /index.html \r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302LoginSuccessHeader(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Set-Cookie: logined=true \r\n");
            dos.writeBytes("Location: /index.html \r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.writeBytes("\r\n");
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}