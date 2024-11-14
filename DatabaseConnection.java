package application;

import java.sql.*;
import java.time.LocalDateTime;

public class DatabaseConnection{
    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/talent_shopping";//database name
    private static final String DATABASE_USER = "root";
    private static final String DATABASE_PASSWORD = "Tanakam18&";

    private Connection connection;

    public DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
//            nC.showNotificationSomethingWrong("Failed to connect to the database");
        }
    }

    private void closeResources(AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public boolean checkExists(String query, String param, String columnName) {
        boolean exists = false;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, param);
            try (ResultSet rs = stmt.executeQuery()) {
                exists = rs.next() && param.equals(rs.getString(columnName));
            }

        } catch (SQLException e) {
            System.out.println("checking user input failed in database");
        }
        return exists;
    }


    public boolean checkQuestionWithField(String query, int param, String columnName,String field) {
        boolean exists = false;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, field);
            try (ResultSet rs = stmt.executeQuery()) {
                exists = rs.next() && param==rs.getInt(columnName);
            }

        } catch (SQLException e) {
            e.printStackTrace();
//            nC.showNotificationSomethingWrong("Database query failed");
        }
        return exists;
    }
    private boolean checkAnswerWithField(String query, String param, String columnName, String field) {
        boolean exists = false;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, field);
            try (ResultSet rs = stmt.executeQuery()) {
                exists = rs.next() && param.equals(rs.getString(columnName));
            }
        } catch (SQLException e) {
            e.printStackTrace();
//            nC.showNotificationSomethingWrong("Database query failed");
        }
        return exists;
    }
    public boolean checkPasswordWithField(String query, String fieldValue, String password, String field) {
        boolean isPasswordMatch = false;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, fieldValue);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String encryptedPassword = rs.getString("password");
                    EncryptionAndDecryptionPass eadp;
                    if(usernameOrEmail(field)){
                        eadp = new EncryptionAndDecryptionPass(getSecretKeyWithUsername(fieldValue));
                    }else {
                        eadp = new EncryptionAndDecryptionPass(getSecretKeyWithEmail(fieldValue));
                    }
                    isPasswordMatch = password.equals(eadp.decrypt(encryptedPassword));
                }
            }
        } catch (Exception e) {
            System.out.println("checking password failed in database");
        }
        return isPasswordMatch;
    }


    public boolean updateUserField(String query, String value, String identifier) {
       boolean result=false;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, value);
            stmt.setString(2, identifier);
            stmt.executeUpdate();
            result=true;
        } catch (SQLException e) {
            e.printStackTrace();
//            nC.showNotificationSomethingWrong("Failed to update database");
        }
        return result;
    }
    public String getUserField(String query, String identifier, String column) {
        String result = "";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, identifier);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    result = rs.getString(column);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
//            nC.showNotificationSomethingWrong("Failed to retrieve data from database");
        }
        return result;
    }
    public boolean checkingEmailInDatabase(String email) {
        String query = "SELECT email FROM users WHERE email = ?";
        return checkExists(query, email, "email");
    }
    public boolean checkingUsernameInDatabase(String username) {
        String query = "SELECT username FROM users WHERE username = ?";
        return checkExists(query, username, "username");
    }
        public boolean checkingPasswordWithUsername(String username, String password) {
        String query = "SELECT password FROM users WHERE username = ?";
        return checkPasswordWithField(query, username, password, "username");
    }
    public boolean checkingPasswordWithEmail(String email, String password) {
        String query = "SELECT password FROM users WHERE email = ?";
        return checkPasswordWithField(query, email, password, "email");
    }
    public boolean setUserInformationInDatabase(String username, String encryptedPassword, String email, int questionIndex, String answer, String generatedKey) {
        String query = "INSERT INTO users (role, username, password, email, security_question, security_answer, create_at, secret_key) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "user");
            stmt.setString(2, username);
            stmt.setString(3, encryptedPassword);
            stmt.setString(4, email);
            stmt.setInt(5, questionIndex);
            stmt.setString(6, answer);
            stmt.setTimestamp(7, getCurrentDateTime());
            stmt.setString(8, generatedKey);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("set information failed in database");
            return false;
//            nC.showNotificationSomethingWrong("Failed to insert user information");
        }
    }
    public boolean setNewEmailToOldEmail(String username, String newEmail) {
        String query = "UPDATE users SET email = ? WHERE username = ?";
        return updateUserField(query, newEmail, username);
    }
    public boolean setNewToOldUsername(String newUsername, String oldUsername) {
        String query = "UPDATE users SET username = ? WHERE username = ?";
        return updateUserField(query, newUsername, oldUsername);
    }
    public boolean setNewToOldPasswordWithUsername(String username, String newPassword) {
        String query = "UPDATE users SET password = ? WHERE username = ?";
        return updateUserField(query, newPassword, username);
    }
    public boolean setNewToOldPasswordWithEmail(String email, String newPassword) {
        String query = "UPDATE users SET password = ? WHERE email = ?";
        return updateUserField(query, newPassword, email);
    }
    public String getPasswordThroughEmail(String email) {
        String query = "SELECT password FROM users WHERE email = ?";
        return getUserField(query, email, "password");
    }
    public String getSecretKeyWithEmail(String email) {
        String query = "SELECT secret_key FROM users WHERE email = ?";
        return getUserField(query, email, "secret_key");
    }
    public String getPasswordThroughUsername(String username) {
        String query = "SELECT password FROM users WHERE username = ?";
        return getUserField(query, username, "password");
    }
    public String getSecretKeyWithUsername(String username) {
        String query = "SELECT secret_key FROM users WHERE username = ?";
        return getUserField(query, username, "secret_key");
    }

    public boolean checkingQuestionWithEmail(String email, int question) {
        String query = "SELECT security_question FROM users WHERE email = ?";
        return checkQuestionWithField(query, question, "security_question",email);
    }
    public boolean checkingQuestionWithUsername(String username, int question) {
        String query = "SELECT security_question FROM users WHERE username = ?";
        return checkQuestionWithField(query, question, "security_question",username);
    }
    public boolean checkingAnswerWithEmail(String email, String answer) {
        String query = "SELECT security_answer FROM users WHERE email = ?";
        return checkAnswerWithField(query, answer, "security_answer",email);
    }
    public boolean checkingAnswerWithUsername(String username, String answer) {
        String query = "SELECT security_answer FROM users WHERE username = ?";
        return checkAnswerWithField(query, answer, "security_answer",username);
    }
    private boolean usernameOrEmail(String email) {
        return (!email.endsWith("@gmail.com"));
    }
    public Timestamp getCurrentDateTime() {
        return Timestamp.valueOf(LocalDateTime.now());
    }
}
