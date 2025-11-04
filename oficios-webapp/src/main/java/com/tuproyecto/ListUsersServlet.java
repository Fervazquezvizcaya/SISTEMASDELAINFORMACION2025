package com.tuproyecto;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession; // Se agregó import de HttpSession
import jakarta.servlet.RequestDispatcher; // Se agregó import

@WebServlet("/list-users")
public class ListUsersServlet extends HttpServlet {

    // --- DATOS DE CONEXIÓN SEGUROS ---
    private static final String DB_HOST = "54.242.175.198"; 
    private static final String DB_NAME = "webapp_db";
    private static final String DB_URL = "jdbc:mariadb://" + DB_HOST + ":3306/" + DB_NAME + "?sslMode=disable"; 

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        HttpSession session = request.getSession(false);
        
        // --- 1. VERIFICACIÓN DE SEGURIDAD (Solo Admin) ---
        if (session == null || session.getAttribute("isAdmin") == null || !(Boolean)session.getAttribute("isAdmin")) {
            response.sendRedirect("index.html");
            return;
        }

        // --- 2. OBTENER CREDENCIALES DEL ENTORNO ---
        String dbUser = System.getenv("DB_USER"); 
        String dbPassword = System.getenv("DB_PASSWORD");

        if (dbUser == null || dbPassword == null) {
            // Error de configuración (el que ya resolviste en /etc/default/tomcat10)
            request.getRequestDispatcher("admin.jsp?error=db_config").forward(request, response);
            return;
        }

        List<User> userList = new ArrayList<>();
        Connection conn = null;

        try {
            Class.forName("org.mariadb.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, dbUser, dbPassword);

            String sql = "SELECT id_usuario, nombre_usuario, es_admin FROM usuarios";
            Statement statement = conn.createStatement();
            ResultSet result = statement.executeQuery(sql);

            while (result.next()) {
                int id = result.getInt("id_usuario");
                String name = result.getString("nombre_usuario");
                boolean isAdmin = result.getBoolean("es_admin");
                userList.add(new User(id, name, isAdmin));
            }
            
            // 3. 🚨 CLAVE: Establecer la lista en el REQUEST y USAR FORWARD
            request.setAttribute("userList", userList);
            request.getRequestDispatcher("admin.jsp").forward(request, response);
            
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            request.setAttribute("dbError", "Error al conectar o consultar la base de datos.");
            request.getRequestDispatcher("admin.jsp?error=db_query").forward(request, response);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    // Clase auxiliar (Debe estar en este archivo o en un archivo User.java separado)
    public class User {
        private int id;
        private String name;
        private boolean isAdmin;

        public User(int id, String name, boolean isAdmin) {
            this.id = id;
            this.name = name;
            this.isAdmin = isAdmin;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public boolean isAdmin() { return isAdmin; }
    }
}