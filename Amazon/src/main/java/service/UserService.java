package service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.logging.log4j.LogManager;

import org.apache.logging.log4j.Logger;


import exceptions.UserNotFoundException;
import util.DBConnection;

public class UserService {

    private static final Logger logger = LogManager.getLogger(UserService.class);

    private boolean isValidName(String name) {
        if (name == null || name.trim().length() < 3) {
            return false;
        }
        return name.matches("[a-zA-Z ]+");
    }

    private boolean isValidEmail(String email) {
        if (email == null)
            return false;
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private boolean isValidPhone(String phone) {
        if (phone == null)
            return false;
        return phone.matches("[0-9]{10}");
    }

    private boolean isValidPassword(String password) {
        if (password == null)
            return false;

        if (password.length() < 6) {
            return false;
        }

        return true;
    }

    public void registerUser(String name, String email, String phone, String password) throws Exception {

        logger.info("Attempting user registration for email {}", email);

        if (!isValidName(name)) {
            logger.warn("Invalid name provided during registration: {}", name);
            System.out.println("Invalid Name (Only letters, min 3 characters)");
            return;
        }

        if (!isValidEmail(email)) {
            logger.warn("Invalid email format during registration: {}", email);
            System.out.println("Invalid Email Format");
            return;
        }

        if (!isValidPhone(phone)) {
            logger.warn("Invalid phone number during registration: {}", phone);
            System.out.println("Phone must be 10 digits");
            return;
        }

        if (!isValidPassword(password)) {
            logger.warn("Weak password during registration for email {}", email);
            System.out.println("Password must be at least 6 characters");
            return;
        }

        Connection con = DBConnection.getConnection();

        String query = "INSERT INTO users(user_name,email_id,phoneNumber,password) VALUES(?,?,?,?)";

        PreparedStatement ps = con.prepareStatement(query);

        ps.setString(1, name);
        ps.setString(2, email);
        ps.setString(3, phone);
        ps.setString(4, password);

        ps.executeUpdate();

        logger.info("User registered successfully with email {}", email);

        System.out.println("User Registered Successfully");

        ps.close();
        con.close();
    }

    public int loginUser(String email, String password) throws Exception, UserNotFoundException {

        logger.info("User attempting login with email {}", email);

        Connection con = DBConnection.getConnection();

        String query = "SELECT user_id FROM users WHERE email_id=? AND password=?";

        PreparedStatement ps = con.prepareStatement(query);

        ps.setString(1, email);
        ps.setString(2, password);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {

            int userId = rs.getInt("user_id");

            logger.info("User login successful for email {}", email);

            rs.close();
            ps.close();
            con.close();

            return userId;
        }

        logger.warn("Login failed for email {}", email);

        rs.close();
        ps.close();
        con.close();

        throw new UserNotFoundException("User not found with given email and password");
    }
}