package http;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

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
        String url = getDefaultUrl(httpRequest.getPath());

        if ("/user/create".equals(url)) {
            User user = new User(httpRequest.getParameter("userId"), httpRequest.getParameter("password"), httpRequest.getParameter("name"), httpRequest.getParameter("email"));
            log.debug("user : {}", user);
            DataBase.addUser(user);
            response302Header(dos);
        } else if ("/user/login".equals(url)) {
            User user = DataBase.findUserById(httpRequest.getParameter("userId"));
            if (user != null) {
                if (user.login(httpRequest.getParameter("password"))) {
                    response302LoginSuccessHeader(dos);
                } else {
                    responseResource("/user/login_failed.html");
                }
            } else {
                responseResource("/user/login_failed.html");
            }
        } else if ("/user/list".equals(url)) {
            if (!isLogin(httpRequest.getHeader("Cookie"))) {
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
            response200Header(dos, body.length);
            responseBody(dos, body);
        }

        else if (httpRequest.getPath().endsWith(".css")) {
            responseCssResource(url);
        } else {
            responseResource(url);
        }
    }

    private String getDefaultUrl(String url) {
        if (url.equals("/")) {
            url = "/index.html";
        }
        return url;
    }
    private void responseResource(String url) throws IOException {
        byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
        response200Header(dos, body.length);
        responseBody(dos, body);
    }
    private void responseCssResource(String url) throws IOException {
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

    private boolean isLogin(String line) {
        String[] headerTokens = line.split(";");
        Map<String, String> cookies = HttpRequestUtils.parseCookies(headerTokens[0].trim());
        String value = cookies.get("logined");
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }
}
