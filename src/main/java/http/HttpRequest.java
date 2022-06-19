package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class HttpRequest {

    private static final Logger log = LoggerFactory.getLogger(http.HttpRequest.class);
    private RequestLine requestLine;
    private RequestParams requestParams = new RequestParams();
    private HttpHeaders headers;

    public HttpRequest(InputStream is) {
        try {

            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            requestLine = new RequestLine(createRequestLine(br));
            requestParams.addQueryString(requestLine.getQueryString());
            headers = processHeaders(br);
            requestParams.addBody(IOUtils.readData(br, headers.getContentLength()));
            // ---

//            String line = br.readLine();
//            if (line == null) {
//                return;
//            }
//
//            log.debug("request line : {}", line);
//
//            tokens = line.split(" ");
//            contentLength = 0;
//            logined = false;
//
//            while (!line.equals("")) {
//                line = br.readLine();
//                log.debug("header : {}", line);
//
//                if (line.contains("Content-Length")) {
//                    contentLength = getContentLength(line);
//                }
//
//                if (line.contains("Cookie")) {
//                    logined = isLogin(line);
//                }
//            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }

    }

    private String createRequestLine(BufferedReader br) throws IOException {
        String line = br.readLine();
        if (line == null) {
            throw new IllegalStateException();
        }
        return line;
    }

    private int getContentLength(String line) {
        String[] headerTokens = line.split(":");
        return Integer.parseInt(headerTokens[1].trim());
    }

    public HttpMethod getMethod() {
        return requestLine.getMethod();
    }

    public String getPath() {
        return requestLine.getPath();
    }

    public String getHeader(String name) {
        return headers.getHeader(name);
    }
    public String getParameter(String name) {
        return requestParams.getParameter(name);
    }
    private HttpHeaders processHeaders(BufferedReader br) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        String line;
        while (!(line = br.readLine()).equals("")) {
            headers.add(line);
        }
        return headers;
    }
}
