package com.codecool.queststore.controller;

import com.codecool.queststore.DAO.*;
import com.codecool.queststore.model.RequestFormater;
import com.codecool.queststore.model.SingletonAcountContainer;
import com.codecool.queststore.model.classRoom.ClassRoom;
import com.codecool.queststore.model.user.Admin;
import com.codecool.queststore.model.user.Mentor;
import com.codecool.queststore.model.user.UserDetails;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminController implements HttpHandler {

    private Admin admin;

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

        redirectToLoginPageIfSessionExpired(httpExchange);

        this.admin = getAdminByCookie(httpExchange);

        String method = httpExchange.getRequestMethod();
        String response = "";

        if (isGetMethod(method)) {
            constructResponse(httpExchange, response);
        } else {
            manageDataAndRedirect(httpExchange);
        }

        sendResponse(httpExchange, response);

//        Map<String, String> actionsDatas = parseURI(httpExchange);
//        if (requestToMenu(actionsDatas, method)) {
//            response = getResponse("templates/menu-admin.twig");
//        } else if (requestToAddMentor(actionsDatas, method)) {
//            response = getResponse("templates/add-mentor.twig");
//        } else if (mentorDataConfirmed(actionsDatas, method)) {
//            createMentor(httpExchange);
//            redirect(httpExchange, "/admin");
//        } else if (requestToAddClassRoom(actionsDatas, method)) {
//            response = getResponse("templates/add-class.twig");
//        } else if (requestLogout(actionsDatas, method)) {
//            clearSession();
//            redirect(httpExchange, "/index");
//        }

    }

    private void redirectToLoginPageIfSessionExpired(HttpExchange httpExchange) throws IOException {
        String cookieStr = httpExchange.getRequestHeaders().getFirst("Cookie");
        String sessionId = getSessionIdbyCookie(cookieStr);

        if (sessionExpired(sessionId)) { redirect(httpExchange, "index"); }
    }

    private String getSessionIdbyCookie(String cookieStr) {
        HttpCookie httpCookie = HttpCookie.parse(cookieStr).get(0);
        return httpCookie.toString().split("=")[1];
    }

    private boolean sessionExpired(String sessionId) {
        return sessionId == null;
    }

    private void redirect(HttpExchange httpExchange, String location) throws IOException {
        Headers headers = httpExchange.getResponseHeaders();
        headers.add("Location", location);
        httpExchange.sendResponseHeaders(302, -1);
        httpExchange.close();
    }

    private Admin getAdminByCookie(HttpExchange httpExchange) {
        String sessionId = getSessionId(httpExchange);
        int codecoolerId = getCodecoolerId(sessionId);
        AdminDAOImpl adminDAO = getAdminDao();
        return adminDAO.getAdmin(codecoolerId);
    }

    private String getSessionId(HttpExchange httpExchange) {
        HttpCookie httpCookie;
        String cookieStr = httpExchange.getRequestHeaders().getFirst("Cookie");
        httpCookie = HttpCookie.parse(cookieStr).get(0);
        return httpCookie.toString().split("=")[1];
    }

    private int getCodecoolerId(String sessionId) {
        SingletonAcountContainer sessionsIDs = SingletonAcountContainer.getInstance();
        return sessionsIDs.getCodecoolerId(sessionId);
    }

    private AdminDAOImpl getAdminDao() {
        DAOFactoryImpl daoFactory = new DAOFactoryImpl();
        return new AdminDAOImpl(daoFactory);
    }

    private void constructResponse(HttpExchange httpExchange, String response) throws IOException {
        String dataUri = getDataUri(httpExchange);

        switch (dataUri) {
            case "admin":
                response = getResponse("templates/menu-admin.twig");
                break;
            case "add-mentor":
                response = getResponse("templates/add-mentor.twig");
                break;
            case "add-class":
                response = getResponse("templates/add-class.twig");
                break;
            case "logout":
                clearSession();
                redirect(httpExchange, "/index");
                break;
        }
    }

    private Map<String, String> parseURI(HttpExchange httpExchange) {

        String uri = httpExchange.getRequestURI().toString();
        String[] actionsDatas = uri.split("/");

        Map <String, String> keyValue = new HashMap<>();
        for (int i = 0; i < actionsDatas.length - 1; i++) { keyValue.put("action", actionsDatas[i]); }
        keyValue.put("data", actionsDatas[actionsDatas.length - 1]);

        return keyValue;
    }

    private boolean isGetMethod(String method) { method.equals("GET"); }

    private boolean requestToMenu(Map<String, String> actionsDatas, String method) {
        boolean isDataCorrect = actionsDatas.get("data").equals("admin");
        boolean isGetMethod = method.equals("GET");
        return isDataCorrect && isGetMethod;
    }

    private boolean requestToAddMentor(Map<String, String> actionsDatas, String method) {
        boolean isActionCorrect = actionsDatas.get("action").contains("admin");
        boolean isDataCorrect = actionsDatas.get("data").equals("add-mentor");
        boolean isGetMethod = method.equals("GET");
        return isActionCorrect && isDataCorrect && isGetMethod;
    }

    private boolean mentorDataConfirmed(Map<String, String> actionsDatas, String method) {
        boolean isActionCorrect = actionsDatas.get("action").contains("admin");
        boolean isDataCorrect = actionsDatas.get("data").equals("add-mentor");
        boolean isPostMethod = method.equals("POST");
        return isActionCorrect && isDataCorrect && isPostMethod;
    }

    private boolean requestToAddClassRoom(Map<String, String> actionsDatas, String method) {
        boolean isActionCorrect = actionsDatas.get("action").contains("admin");
        boolean isDataCorrect = actionsDatas.get("data").equals("add-class");
        boolean isGetMethod = method.equals("GET");
        return isActionCorrect && isDataCorrect && isGetMethod;
    }

    private boolean requestLogout(Map<String, String> actionsDatas, String method) {
        boolean isActionCorrect = actionsDatas.get("action").contains("admin");
        boolean isDataCorrect = actionsDatas.get("data").equals("logout");
        return isActionCorrect && isDataCorrect;
    }

    private void createMentor(HttpExchange httpExchange) throws IOException {
        Map<String, String> formMap = new RequestFormater().getMapFromRequest(httpExchange);
        UserDetails userDetails = new UserDetails(formMap.get("firstname"), formMap.get("lastname"), formMap.get("email"), formMap.get("login"), formMap.get("password"), "mentor");
        Mentor mentor = new Mentor(0, userDetails);
        DAOFactoryImpl daoFactory = new DAOFactoryImpl();
        MentorDAO mentorDAO = daoFactory.getMentorDAO();
        mentorDAO.add(mentor);
    }

    private void clearSession() {
        SingletonAcountContainer acountContainer = SingletonAcountContainer.getInstance();
        acountContainer.removeSession(admin.getUserDetails().getId());
    }

    private String getResponse(String templatePath) {

        JtwigTemplate jtwigTemplate = JtwigTemplate.classpathTemplate(templatePath);
        JtwigModel jtwigModel = JtwigModel.newModel();
        setHeaderDetails(jtwigModel);
        if (templatePath.contains("add-mentor")) { setClassRooms(jtwigModel); }

        return jtwigTemplate.render(jtwigModel);
    }

    private void setHeaderDetails(JtwigModel jtwigModel) {
        UserDetails userDetails = admin.getUserDetails();
        jtwigModel.with("fullname", userDetails.getFirstName() + " " + userDetails.getLastName());
    }

    private void setClassRooms(JtwigModel jtwigModel) {
        List<ClassRoom> classRooms = new DAOFactoryImpl().getClassDAO().getAll();
        jtwigModel.with("classroom", classRooms);
    }

    private void sendResponse(HttpExchange httpExchange, String response) throws IOException {
        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
