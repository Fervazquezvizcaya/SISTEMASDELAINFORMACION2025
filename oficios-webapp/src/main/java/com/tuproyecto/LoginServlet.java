package com.tuproyecto;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement; 
import java.sql.ResultSet;      
import java.sql.SQLException;     

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    // Se mantiene la URL con la corrección SSL/TLS
    private static final String DB_HOST = "54.242.175.198"; 
    private static final String DB_NAME = "webapp_db";
    private static final String DB_URL = "jdbc:mariadb://" + DB_HOST + ":3306/" + DB_NAME + "?sslMode=disable";
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        // 🚨 SEGURIDAD: Obtiene las credenciales de las variables de entorno
        String dbUser = System.getenv("DB_USER"); 
        String dbPassword = System.getenv("DB_PASSWORD");

        // Verificación de seguridad
        if (dbUser == null || dbPassword == null) {
            response.sendRedirect("error_configuracion.html");
            return; 
        }

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null; // Declaración fuera del try

        try {
            Class.forName("org.mariadb.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, dbUser, dbPassword); 

            // Consulta SQL segura (utiliza SHA2 para comparar el hash)
            String sql = "SELECT es_admin, id_usuario FROM usuarios WHERE nombre_usuario = ? AND password = SHA2(?, 256)";
            
            statement = conn.prepareStatement(sql);
            statement.setString(1, username);
            statement.setString(2, password);

            result = statement.executeQuery(); 

            if (result.next()) {
                boolean esAdmin = result.getBoolean("es_admin");
                int userId = result.getInt("id_usuario");

                HttpSession session = request.getSession();
                session.setAttribute("username", username);
                session.setAttribute("isAdmin", esAdmin);
                session.setAttribute("userId", userId);

                // REDIRECCIÓN CORREGIDA: Apunta al servlet de carga de datos
                if (esAdmin) {
                    response.sendRedirect("list-users"); // Correcto: Llama al Servlet de carga de datos
                } else {
                    response.sendRedirect("user_panel.jsp"); 
                }
            } else {
                response.sendRedirect("index.html?error=true"); // Contraseña incorrecta
            }

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            response.sendRedirect("error_configuracion.html"); // Fallo de DB/Conexión
        } finally {
            // Cierre seguro de recursos
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}