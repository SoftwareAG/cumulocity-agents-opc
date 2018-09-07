package com.cumulocity.opcua.mock.notification.controller;

import lombok.RequiredArgsConstructor;
import org.cometd.bayeux.server.ServerTransport;
import org.cometd.server.BayeuxServerImpl;
import org.cometd.server.transport.HttpTransport;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
public class NotificationController extends HttpServlet {

    private final BayeuxServerImpl server;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (isOptionsRequest(request)) {
            doOptions(request, response);
        } else {
            doService(request, response);
        }
    }

    private boolean isOptionsRequest(HttpServletRequest request) {
        return "OPTIONS".equals(request.getMethod());
    }

    private void doService(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
        try {
            handleTransport(request, response);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    public void handleTransport(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
            HttpTransport transport = resolveHttpTransport(request);
            try {
                server.setCurrentTransport(transport);
                transport.setCurrentRequest(request);
                transport.handle(request, response);
            } finally {
                transport.setCurrentRequest(null);
                server.setCurrentTransport(null);
            }
        } catch (NoSuchElementException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown Bayeux Transport");
        }
    }

    private HttpTransport resolveHttpTransport(HttpServletRequest request) {
        final List<String> allowedTransports = server.getAllowedTransports();
        for (final String transportName : allowedTransports) {
            ServerTransport serverTransport = server.getTransport(transportName);
            if (serverTransport instanceof HttpTransport) {
                HttpTransport t = (HttpTransport) serverTransport;
                if (t.accept(request)) {
                    return t;
                }
            }
        }
        throw new NoSuchElementException("Unable to resolve http transport");
    }
}
