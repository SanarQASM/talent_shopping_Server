// ServerHandler.java
package application;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerHandler implements Runnable {
    private final Socket clientSocket;
    private final DatabaseConnection dbConnection;
    private static final EncryptionAndDecryptionPass eADP = EncryptionAndDecryptionPass.getInstance();

    public ServerHandler(Socket clientSocket, DatabaseConnection dbConnection) {
        this.clientSocket = clientSocket;
        this.dbConnection = dbConnection;
    }

    @Override
    public void run() {
        try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {
            String request = (String) in.readObject();
            String response = switch (request) {
                case "CHECK_EMAIL" -> handleCheckEmail(in);
                case "CHECK_USERNAME" -> handleCheckUsername(in);
                case "CHECK_PASSWORD_USERNAME" -> handleCheckPasswordUsername(in);
                case "CHECK_PASSWORD_EMAIL" -> handleCheckPasswordEmail(in);
                case "SET_USER_INFORMATION" -> handleSetUserInformation(in);
                case "SET_NEW_WITH_OLD_EMAIL" -> handleSetNewWithOldEmail(in);
                case "CHECK_QUESTION_USERNAME" -> handleCheckQuestionUsername(in);
                case "CHECK_QUESTION_EMAIL" -> handleCheckQuestionEmail(in);
                case "CHECK_ANSWER_EMAIL" -> handleCheckAnswerEmail(in);
                case "CHECK_ANSWER_USERNAME" -> handleCheckAnswerUsername(in);
                case "UPDATE_PASSWORD_EMAIL" -> handleUpdatePasswordEmail(in);
                case "UPDATE_PASSWORD_USERNAME" -> handleUpdatePasswordUsername(in);
                case "UPDATE_USERNAME" -> handleUpdateUsername(in);
                default -> "Invalid request";
            };
            out.writeObject(response);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String handleCheckEmail(ObjectInputStream in) throws Exception {
        String email = (String) in.readObject();
        boolean result = dbConnection.checkingEmailInDatabase(email);
        return result ? "1- exists" : "2- Email does not exist";
    }

    private String handleCheckUsername(ObjectInputStream in) throws Exception {
        String username = (String) in.readObject();
        boolean result = dbConnection.checkingUsernameInDatabase(username);
        return result ? "1- exists" : "2- Username does not exist";
    }

    private String handleCheckPasswordUsername(ObjectInputStream in) throws Exception {
        String username = (String) in.readObject();
        String password = (String) in.readObject();
        boolean result = dbConnection.checkingPasswordWithUsername(username, password);
        return result ? "1- exists" : "2- Password does not exist";
    }

    private String handleCheckPasswordEmail(ObjectInputStream in) throws Exception {
        String email = (String) in.readObject();
        String password = (String) in.readObject();
        boolean result = dbConnection.checkingPasswordWithEmail(email, password);
        return result ? "1- exists" : "2- Email does not exist";
    }

    private String handleSetUserInformation(ObjectInputStream in) throws Exception {
        String username = (String) in.readObject();
        String password = (String) in.readObject();
        String email = (String) in.readObject();
        String questionSecurity = (String) in.readObject();
        String answerSecurity = (String) in.readObject();
        String generatedKey = eADP.generateSecretKey();
        String encryptedPassword = eADP.encrypt(password);
        boolean result = dbConnection.setUserInformationInDatabase(username, encryptedPassword, email,
                Integer.parseInt(questionSecurity), answerSecurity, generatedKey);
        return result ? "1- exists" : "2- Cannot set user information";
    }

    private String handleSetNewWithOldEmail(ObjectInputStream in) throws Exception {
        String username = (String) in.readObject();
        String email = (String) in.readObject();
        boolean result = dbConnection.setNewEmailToOldEmail(username, email);
        return result ? "1- exists" : "2- Cannot set new with old email";
    }

    private String handleCheckQuestionUsername(ObjectInputStream in) throws Exception {
        String username = (String) in.readObject();
        String questionSecurity = (String) in.readObject();
        boolean result = dbConnection.checkingQuestionWithUsername(username, Integer.parseInt(questionSecurity));
        return result ? "1- exists" : "2- Cannot check question with username";
    }

    private String handleCheckQuestionEmail(ObjectInputStream in) throws Exception {
        String email = (String) in.readObject();
        String questionSecurity = (String) in.readObject();
        boolean result = dbConnection.checkingQuestionWithEmail(email, Integer.parseInt(questionSecurity));
        return result ? "1- exists" : "2- Cannot check question with email";
    }

    private String handleCheckAnswerEmail(ObjectInputStream in) throws Exception {
        String email = (String) in.readObject();
        String answerSecurity = (String) in.readObject();
        boolean result = dbConnection.checkingAnswerWithEmail(email, answerSecurity);
        return result ? "1- exists" : "2- Cannot check answer with email";
    }

    private String handleCheckAnswerUsername(ObjectInputStream in) throws Exception {
        String username = (String) in.readObject();
        String answerSecurity = (String) in.readObject();
        boolean result = dbConnection.checkingAnswerWithUsername(username, answerSecurity);
        return result ? "1- exists" : "2- Cannot check answer with username";
    }

    private String handleUpdatePasswordEmail(ObjectInputStream in) throws Exception {
        String email = (String) in.readObject();
        String password = (String) in.readObject();
        String encryptedPassword = eADP.encrypt(password);
        boolean result = dbConnection.setNewToOldPasswordWithEmail(email, encryptedPassword);
        return result ? "1- exists" : "2- Cannot update password with email";
    }

    private String handleUpdatePasswordUsername(ObjectInputStream in) throws Exception {
        String username = (String) in.readObject();
        String password = (String) in.readObject();
        String encryptedPassword = eADP.encrypt(password);
        boolean result = dbConnection.setNewToOldPasswordWithUsername(username, encryptedPassword);
        return result ? "1- exists" : "2- Cannot update password with username";
    }

    private String handleUpdateUsername(ObjectInputStream in) throws Exception {
        String newUsername = (String) in.readObject();
        String oldUsername = (String) in.readObject();
        boolean result = dbConnection.setNewToOldUsername(newUsername, oldUsername);
        return result ? "1- exists" : "2- Cannot update username";
    }

}
