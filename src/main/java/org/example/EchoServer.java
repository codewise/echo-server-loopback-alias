package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.StdErrLog;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import static java.util.Map.of;
import static java.util.stream.Collectors.toMap;

public class EchoServer {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Server server;
    private Thread serverThread;

    public static void main(String[] args) throws Exception {
        new EchoServer().start();

        Thread.sleep(1000000000);
    }

    private void start() {
        serverThread = new Thread("EchoServer") {
            @Override
            public void run() {
                try {
                    server.start();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        serverThread.setDaemon(true);
        serverThread.start();
    }

    private EchoServer() {
        StdErrLog logger = new StdErrLog();
        logger.setDebugEnabled(true);
        Log.setLog(logger);

        // randomly allocated port
        server = new Server(0);
        server.setHandler(new AbstractHandler() {

            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest request,
                    HttpServletResponse response) throws IOException {
                response.addHeader("content-type", "application/json");
                response.setStatus(200);

                ServletOutputStream outputStream = response.getOutputStream();
                InetSocketAddress remoteAddress = baseRequest.getHttpChannel().getEndPoint().getRemoteAddress();
                InetSocketAddress localAddress = baseRequest.getHttpChannel().getLocalAddress();
                LinkedHashMap<String, Object> output = new LinkedHashMap<>() {
                    {
                        put("protocol", request.getProtocol());
                        put("method", request.getMethod());
                        put("hostname", baseRequest.getRemoteHost());
                        put("remoteAddress", of("ip", remoteAddress.getHostString(), "port", remoteAddress.getPort()));
                        put("localAddress", of("ip", localAddress.getHostString(), "port", localAddress.getPort()));
                        // put("port", req.port());
                        put("path", target);
                        put("query", baseRequest.getQueryString());
                        put("headers", Collections.list(baseRequest.getHeaderNames())
                                .stream()
                                .collect(toMap(headerName -> headerName, baseRequest::getHeader)));
                        put("body", request.getReader().lines().collect(Collectors.joining()));
                        put("url", request.getRequestURL().toString());
                        put("uri", baseRequest.getRequestURI());
                    }
                };
                byte[] bytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(output);
                outputStream.write(bytes);
                outputStream.write("\n".getBytes());
                baseRequest.setHandled(true);
            }
        });
    }
}