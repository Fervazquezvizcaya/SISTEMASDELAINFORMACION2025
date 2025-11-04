package com.tuproyecto;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession; // Importamos HttpSession

@WebServlet("/manage-users")
public class ManageUsersServlet extends HttpServlet {

    // 1. 🚨 SEGURIDAD: Solo se declara la URL (con corrección SSL/TLS)
    private static final String DB_URL = "jdbc:mariadb://54.242.175.198:3306/webapp_db?useSSL=true&verifyServerCertificate=false";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        String action = request.getParameter("action");
        Connection conn = null;
        
        // --- CÓDIGO DE SEGURIDAD: Verificar si el usuario es Admin y está logueado ---
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("isAdmin") == null || !(Boolean)session.getAttribute("isAdmin")) {
            response.sendRedirect("index.html"); // Expulsar si no es administrador
            return;
        }
        
        // 2. 🚨 SEGURIDAD: Obtener Credenciales de Entorno
        String dbUser = System.getenv("DB_USER"); 
        String dbPassword = System.getenv("DB_PASSWORD");
        
        if (dbUser == null || dbPassword == null) {
            // Manejo de error de configuración de DB (simulado)
            response.sendRedirect("admin.jsp?error=db_config");
            return;
        }
        // --- FIN DEL CÓDIGO DE SEGURIDAD ---
        
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            // 3. CONEXIÓN SEGURA: Usando variables de entorno
            conn = DriverManager.getConnection(DB_URL, dbUser, dbPassword);

            if ("add".equals(action)) {
                // --- Lógica para AÑADIR un usuario ---
                String nombreUsuario = request.getParameter("nombre_usuario");
                String password = request.getParameter("password");
                // Los checkboxes solo se envían si están marcados. Verificamos != null
                boolean esAdmin = request.getParameter("es_admin") != null;

                // 4. LÓGICA: Contraseña cifrada en la inserción
                String sql = "INSERT INTO usuarios (nombre_usuario, password, es_admin) VALUES (?, SHA2(?, 256), ?)";
                
                PreparedStatement statement = conn.prepareStatement(sql);
                statement.setString(1, nombreUsuario);
                statement.setString(2, password); // La DB cifra el password
                statement.setBoolean(3, esAdmin);
                statement.executeUpdate();

            } else if ("delete".equals(action)) {
                // --- Lógica para ELIMINAR un usuario ---
                // Se debe proteger la cuenta del administrador principal (ID 1 = admin)
                int idUsuario = Integer.parseInt(request.getParameter("id_usuario"));

                if (idUsuario == 1) { // Prevención para no eliminar al admin principal
                    response.sendRedirect("admin.jsp?error=delete_admin");
                    return;
                }

                String sql = "DELETE FROM usuarios WHERE id_usuario = ?";
                
                PreparedStatement statement = conn.prepareStatement(sql);
                statement.setInt(1, idUsuario);
                statement.executeUpdate();
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace(); 
            response.sendRedirect("admin.jsp?error=db_op_failed"); 
            return;
        } catch (NumberFormatException e) {
            // Manejar si id_usuario no es un número válido al intentar borrar
            response.sendRedirect("admin.jsp?error=invalid_id"); 
            return;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        
        // Redirigir al servlet que carga la lista de usuarios para actualizar la tabla
        response.sendRedirect("list-users");
    }
}