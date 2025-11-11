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

    // Variables estáticas para la conexión a MariaDB.
    private static final String DB_HOST = "54.242.175.198"; 
    private static final String DB_NAME = "webapp_db";
    // URL de conexión: Incluye la corrección 'sslMode=disable' para AWS.
    private static final String DB_URL = "jdbc:mariadb://" + DB_HOST + ":3306/" + DB_NAME + "?sslMode=disable";
    
    /**
     * Propósito: Maneja la solicitud POST del formulario de inicio de sesión.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        // 1. OBTENER DATOS DE ENTRADA del formulario
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        // 2. SEGURIDAD: Leer credenciales de la DB desde las variables de entorno
        String dbUser = System.getenv("DB_USER"); 
        String dbPassword = System.getenv("DB_PASSWORD");

        // Verificación de seguridad: Si las credenciales de entorno no están cargadas.
        if (dbUser == null || dbPassword == null) {
            response.sendRedirect("error_configuracion.html");
            return; 
        }

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null; 

        try {
            // 3. CONEXIÓN A DB: Cargar Driver y establecer conexión segura
            Class.forName("org.mariadb.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, dbUser, dbPassword); 

            // 4. CONSULTA SQL: Compara el input (cifrado con SHA2) contra el hash almacenado.
            String sql = "SELECT es_admin, id_usuario FROM usuarios WHERE nombre_usuario = ? AND password = SHA2(?, 256)";
            
            statement = conn.prepareStatement(sql);
            statement.setString(1, username);
            statement.setString(2, password);

            result = statement.executeQuery(); 

            if (result.next()) {
                // 5. INICIO DE SESIÓN EXITOSO: Obtener rol y ID
                boolean esAdmin = result.getBoolean("es_admin");
                int userId = result.getInt("id_usuario");

                // 6. CREACIÓN DE SESIÓN: Guardar el estado y la identidad del usuario
                HttpSession session = request.getSession();
                session.setAttribute("username", username);
                session.setAttribute("isAdmin", esAdmin);
                session.setAttribute("userId", userId);

                // 7. REDIRECCIÓN CONDICIONAL: Enviar al servlet que carga los datos del panel.
                if (esAdmin) {
                    // Si es Admin: Carga usuarios y oficios.
                    response.sendRedirect("list-users"); 
                } else {
                    // Si es usuario común: Carga solo sus oficios.
                    response.sendRedirect("list-oficios-user"); 
                }
            } else {
                // 8. FALLO DE AUTENTICACIÓN: Redirigir al login con mensaje de error.
                response.sendRedirect("index.html?error=true"); 
            }

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            // 9. ERROR FATAL: Fallo en la conexión a DB.
            response.sendRedirect("error_configuracion.html"); 
        } finally {
            // 10. CIERRE SEGURO: Asegurar que todos los recursos de la DB se cierren.
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