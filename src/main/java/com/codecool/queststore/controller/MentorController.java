package com.codecool.queststore.controller;

import com.codecool.queststore.DAO.*;
import com.codecool.queststore.model.RequestFormater;
import com.codecool.queststore.model.SingletonAcountContainer;
import com.codecool.queststore.model.classRoom.ClassRoom;
import com.codecool.queststore.model.inventory.Category;
import com.codecool.queststore.model.inventory.Item;
import com.codecool.queststore.model.user.Mentor;
import com.codecool.queststore.model.user.Student;
import com.codecool.queststore.model.user.UserDetails;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class MentorController implements HttpHandler {

    private DAOFactory daoFactory;
    private AccountDAO accountDAO;
    private MentorDAO mentorDAO;
    private Mentor mentor;
    private RequestFormater requestFormater;
    private ClassDAO classDAO;
    private StudentDAO studentDAO;
    private ItemDAO itemDAO;
    private MentorMenuContainer mentorMenuContainer;
    private QuestManagment questManagment;
    private String response;
    private String sessionId;
    private Map<String, String> formMap;
    private SingletonAcountContainer accountContainer;

    public MentorController() {
        daoFactory = new DAOFactoryImpl();
        accountDAO = daoFactory.getAccountDAO();
        mentorDAO = daoFactory.getMentorDAO();
        requestFormater = new RequestFormater();
        classDAO = daoFactory.getClassDAO();
        studentDAO = daoFactory.getStudentDAO();
        itemDAO = daoFactory.getItemDAO();
        mentorMenuContainer = new MentorMenuContainer();
        questManagment = new QuestManagment();
        formMap = new HashMap<>();
        accountContainer = SingletonAcountContainer.getInstance();
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String method = httpExchange.getRequestMethod();
        URI uri = httpExchange.getRequestURI();
        String stringUri = uri.toString();
        String cookieStr = httpExchange.getRequestHeaders().getFirst("Cookie");
        validateCookie(stringUri, httpExchange, cookieStr);


        System.out.println(stringUri);
        System.out.println(method);




        formMap = getFormMap(httpExchange, method);
        response = mentorMenuContainer.getMenuMentor();

        if (stringUri.equals("/mentor/add-student") && method.equals("GET")) {
            response = mentorMenuContainer.getMenuAddStudent();

        } else if (stringUri.equals("/mentor/add-student") && method.equals("POST")) {

            int classId = Integer.parseInt(formMap.get("room"));
            ClassRoom classRoom = classDAO.getClass(classId);
            UserDetails userDetails = new UserDetails(formMap.get("firstname"), formMap.get("lastname"), formMap.get("email"), formMap.get("login"), formMap.get("password"), "student");
            Student student = new Student(userDetails, classRoom);
            studentDAO.add(student);

        } else if (stringUri.equals("/mentor/student-classes") && method.equals("GET")) {
            response = mentorMenuContainer.getMenuStudentClassesToEdit();

        } else if (stringUri.contains("/mentor/student-classes/class-id") && method.equals("GET")) {
            int classId = getParameter(uri);
            response = mentorMenuContainer.getMenuStudentOfClassToEdit(classId);

        } else if (stringUri.contains("/mentor/student-to-edit") && method.equals("GET")) {
            String login = getLogin(uri);
            response = mentorMenuContainer.getMenuEditStudent(login);

        } else if (stringUri.contains("/mentor/student-to-edit") && method.equals("POST")) {
            System.out.println("----------------------------------------------------");

            int classId = Integer.parseInt(formMap.get("room"));
            ClassRoom classRoom = classDAO.getClass(classId);
            UserDetails userDetails = new UserDetails(formMap.get("firstname"), formMap.get("lastname"), formMap.get("email"), formMap.get("login"), formMap.get("password"), "student");
            Student student = new Student(userDetails, classRoom);
            studentDAO.update(student);
            redirect(httpExchange, "/mentor");

        } else if (stringUri.contains("add-artifact") && method.equals("GET")) {
            response = mentorMenuContainer.getMenuAddArtifact();

        } else if (stringUri.contains("add-artifact") && method.equals("POST")) {

            int price = Integer.parseInt(formMap.get("price"));
            Category category = new Category(formMap.get("category"));
            Item item = new Item(formMap.get("artifactname"), formMap.get("description"), price, category);
            itemDAO.add(item);
            redirect(httpExchange, "/mentor");

        } else if (stringUri.contains("add-quest") && method.equals("GET")) {
            response = mentorMenuContainer.getMenuAddQuest();

        } else if (stringUri.contains("add-quest") && method.equals("POST")) {

            int price = -Integer.parseInt(formMap.get("reward"));
            Category category = new Category("Quest");
            Item item = new Item(formMap.get("questname"), formMap.get("description"), price, category);
            itemDAO.add(item);
            redirect(httpExchange, "/mentor");

        } else if (stringUri.equals("/mentor/quests-students") && method.equals("GET")) {
            response = mentorMenuContainer.getMenuQuestsStudents();

        } else if (stringUri.contains("mentor/quest-to-student/quest-id") && method.equals("GET")) {
            response = mentorMenuContainer.getMenuQuestsStudents();
            int questId = getParameter(uri);
            questManagment.setQuestId(questId);
            redirect(httpExchange, "/mentor/quest-classes");

        } else if (stringUri.contains("/mentor/quest-classes") && method.equals("GET")) {
            response = mentorMenuContainer.getMenuStudentClassesToAddQuest();

        } else if (stringUri.contains("quest-student-classes/class-id") && method.equals("GET")) {
            int classId = getParameter(uri);
            questManagment.setClassId(classId);
            redirect(httpExchange, "/mentor/quest-to-students");


        }  else if (stringUri.contains("/mentor/quest-to-students") && method.equals("GET")) {
            response = mentorMenuContainer.getMenuStudentChooser(questManagment.getClassId());

        } else if (stringUri.contains("/mentor/quest-to-students") && method.equals("POST")) {

            questManagment.addQuestToStudents(formMap);
            response = mentorMenuContainer.getMenuStudentChooser(questManagment.getClassId());
            redirect(httpExchange, "/mentor");

        } else if (stringUri.equals("/mentor/edit-artifact-list") && method.equals("GET")) {
            response = mentorMenuContainer.getMenuArtifactsToEdit();

        } else if (stringUri.contains("/mentor/artifact-to-edit/artifact-id") && method.equals("GET")) {
            int artifactId = getParameter(uri);
            System.out.println(artifactId);
            response = mentorMenuContainer.getMenuEditArtifact(artifactId);

        } else if (stringUri.contains("/mentor/artifact-to-edit/artifact-id") && method.equals("POST")) {

            int artifactId = getParameter(uri);
            int price = -Integer.parseInt(formMap.get("price"));
            Category category = new Category(formMap.get("category"));
            Item item = new Item(artifactId, formMap.get("artifactname"), formMap.get("description"), price, category);
            itemDAO.update(item);
            redirect(httpExchange, "/mentor");

        } else if (stringUri.equals("/mentor/edit-quest-list") && method.equals("GET")) {
            response = mentorMenuContainer.getMenuQuestsToEdit();

        } else if (stringUri.contains("/mentor/quest-to-edit/quest-id") && method.equals("GET")) {
            int artifactId = getParameter(uri);
            System.out.println(artifactId);
            response = mentorMenuContainer.getMenuEditQuest(artifactId);

        } else if (stringUri.contains("/mentor/quest-to-edit/quest-id") && method.equals("POST")) {
            int questId = getParameter(uri);
            int price = Integer.parseInt(formMap.get("price"));
            Category category = new Category("Quest");
            Item item = new Item(questId, formMap.get("artifactname"), formMap.get("description"), price, category);
            itemDAO.update(item);
            redirect(httpExchange, "/mentor");

        } else if (stringUri.contains("logout") && method.equals("GET")) {
            clearSession();
            redirect(httpExchange, "/index");

        }


        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private Map<String, String> getFormMap(HttpExchange httpExchange, String method) throws IOException {
        if (method.equals("POST")) {
            return requestFormater.getMapFromRequest(httpExchange);
        }

        return null;
    }

    private void checkAccount(HttpExchange httpExchange, String accountType, int id) throws IOException {
        if (!accountType.equals("mentor")) {
            redirect(httpExchange, "account");
        } else {
            mentor = mentorDAO.getMentor(id);
            mentorMenuContainer.setMentor(mentor);
        }
    }

    private void checkIfExistSessionId(HttpExchange httpExchange, int id) throws IOException {
        if (accountContainer.checkIfContains(sessionId)) {
            id = accountContainer.getCodecoolerId(sessionId);
            String accountType = accountDAO.getAccountType(id);
            checkAccount(httpExchange, accountType, id);
        }
    }

    private void validateCookie(String stringUri, HttpExchange httpExchange, String cookieStr) throws IOException {
        if (stringUri.contains("mentor") && cookieStr != null) {
            HttpCookie httpCookie = HttpCookie.parse(cookieStr).get(0);
            sessionId = httpCookie.getValue();
            int id = 0;
            checkIfExistSessionId(httpExchange, id);
        }
    }

    private void redirect(HttpExchange httpExchange, String location) throws IOException {
        Headers headers = httpExchange.getResponseHeaders();
        headers.add("Location", location);
        httpExchange.sendResponseHeaders(302, -1);
        httpExchange.close();
    }

    private int getParameter(URI uri) {
        String stringUri = uri.toString();
        String[] uriArray = stringUri.split("/");
        System.out.println(uriArray);
        int INDEX_PARAMETER = uriArray.length - 1;
        String classId = uriArray[INDEX_PARAMETER];

        return Integer.parseInt(classId);
    }

    private String getLogin(URI uri) {
        String stringUri = uri.toString();
        String[] uriArray = stringUri.split("/");
        System.out.println(uriArray);
        int INDEX_LOGIN = uriArray.length - 1;

        return uriArray[INDEX_LOGIN];
    }

    private void clearSession() {
        SingletonAcountContainer acountContainer = SingletonAcountContainer.getInstance();
        acountContainer.removeSession(mentor.getId());
    }
}
