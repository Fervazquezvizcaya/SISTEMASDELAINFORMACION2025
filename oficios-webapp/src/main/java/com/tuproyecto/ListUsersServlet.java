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
import jakarta.servlet.http.HttpSession; 
import jakarta.servlet.RequestDispatcher; // Se agregó import

@WebServlet("/list-users")
public class ListUsersServlet extends HttpServlet {

    // URL de conexión a la base de datos (incluye corrección SSL/TLS)
    private static final String DB_URL = "jdbc:mariadb://54.242.175.198:3306/webapp_db?sslMode=disable"; 

    /**
     * Propósito: Carga la lista de usuarios de la base de datos y prepara la vista del administrador.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        HttpSession session = request.getSession(false);
        
        // 1. VERIFICACIÓN DE SEGURIDAD: Expulsa al usuario si no es administrador.
        if (session == null || session.getAttribute("isAdmin") == null || !(Boolean)session.getAttribute("isAdmin")) {
            response.sendRedirect("index.html");
            return;
        }

        // 2. OBTENER CREDENCIALES: Lee las credenciales de la DB del entorno.
        String dbUser = System.getenv("DB_USER"); 
        String dbPassword = System.getenv("DB_PASSWORD");

        // Fallo de seguridad: Si las variables de entorno no están cargadas.
        if (dbUser == null || dbPassword == null) {
            request.getRequestDispatcher("admin.jsp?error=db_config").forward(request, response);
            return;
        }

        List<User> userList = new ArrayList<>();
        Connection conn = null;

        try {
            // 3. CONEXIÓN Y CONSULTA: Establecer conexión y ejecutar SELECT.
            Class.forName("org.mariadb.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, dbUser, dbPassword);

            String sql = "SELECT id_usuario, nombre_usuario, es_admin FROM usuarios";
            Statement statement = conn.createStatement();
            ResultSet result = statement.executeQuery(sql);

            // 4. MAPEO DE DATOS: Iterar sobre el resultado y llenar la lista.
            while (result.next()) {
                int id = result.getInt("id_usuario");
                String name = result.getString("nombre_usuario");
                boolean isAdmin = result.getBoolean("es_admin");
                userList.add(new User(id, name, isAdmin));
            }
            
            // 5. PREPARAR VISTA: Adjuntar la lista de usuarios a la solicitud.
            request.setAttribute("userList", userList);
            
            // 6. TRANSFERENCIA: Usar forward para enviar los datos a admin.jsp sin cambiar la URL.
            request.getRequestDispatcher("admin.jsp").forward(request, response);
            
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            // Fallo en la base de datos, notificar al administrador.
            request.setAttribute("dbError", "Error al conectar o consultar la base de datos.");
            request.getRequestDispatcher("admin.jsp?error=db_query").forward(request, response);
        } finally {
            // 7. CIERRE SEGURO: Cerrar la conexión a la base de datos.
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    // Clase auxiliar para guardar los datos de un usuario (Modelo de datos)
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