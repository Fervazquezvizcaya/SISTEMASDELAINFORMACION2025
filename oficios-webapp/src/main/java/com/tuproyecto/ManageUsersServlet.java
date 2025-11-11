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
import jakarta.servlet.http.HttpSession; 

/**
 * Servlet para manejar las operaciones CRUD (Crear/Eliminar) de usuarios, 
 * restringido solo a usuarios administradores.
 */
@WebServlet("/manage-users")
public class ManageUsersServlet extends HttpServlet {

    // URL de conexión a la base de datos MariaDB.
    private static final String DB_URL = "jdbc:mariadb://54.242.175.198:3306/webapp_db?useSSL=true&verifyServerCertificate=false";

    /**
     * Procesa las solicitudes POST para añadir o eliminar usuarios.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Establece la codificación de caracteres a UTF-8 para manejo de tildes y caracteres especiales.
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        // Obtiene el parámetro 'action' para determinar la operación (add o delete).
        String action = request.getParameter("action");
        Connection conn = null;
        
        // --- Bloque de Verificación de Seguridad (Autenticación y Autorización) ---
        
        // Obtiene la sesión actual sin crear una nueva.
        HttpSession session = request.getSession(false);
        
        // Verifica que la sesión exista Y que el atributo 'isAdmin' sea verdadero.
        if (session == null || session.getAttribute("isAdmin") == null || !(Boolean)session.getAttribute("isAdmin")) {
            // Si no es administrador o no está logueado, redirige a la página de inicio.
            response.sendRedirect("index.html"); 
            return;
        }
        
        // Obtiene credenciales sensibles de la base de datos desde las variables de entorno.
        String dbUser = System.getenv("DB_USER"); 
        String dbPassword = System.getenv("DB_PASSWORD");
        
        // Verifica si las credenciales de entorno están disponibles.
        if (dbUser == null || dbPassword == null) {
            // Redirige con un error si la configuración de DB falta.
            response.sendRedirect("admin.jsp?error=db_config");
            return;
        }
        
        // --- Fin del Bloque de Seguridad ---
        
        try {
            // Carga el driver JDBC de MariaDB.
            Class.forName("org.mariadb.jdbc.Driver");
            
            // Establece la conexión a la base de datos usando las credenciales de entorno.
            conn = DriverManager.getConnection(DB_URL, dbUser, dbPassword);

            // --- Lógica para AÑADIR un usuario ---
            if ("add".equals(action)) {
                String nombreUsuario = request.getParameter("nombre_usuario");
                String password = request.getParameter("password");
                // Determina si el usuario es administrador basándose en si el checkbox fue marcado.
                boolean esAdmin = request.getParameter("es_admin") != null;

                // Define la consulta SQL para insertar un nuevo usuario. 
                // La contraseña se cifra directamente en la base de datos con SHA-256.
                String sql = "INSERT INTO usuarios (nombre_usuario, password, es_admin) VALUES (?, SHA2(?, 256), ?)";
                
                PreparedStatement statement = conn.prepareStatement(sql);
                // Establece los parámetros de la consulta para prevenir inyección SQL.
                statement.setString(1, nombreUsuario);
                statement.setString(2, password); 
                statement.setBoolean(3, esAdmin);
                
                // Ejecuta la inserción.
                statement.executeUpdate();

            // --- Lógica para ELIMINAR un usuario ---
            } else if ("delete".equals(action)) {
                // Convierte el ID de usuario a entero. Lanza NumberFormatException si no es válido.
                int idUsuario = Integer.parseInt(request.getParameter("id_usuario"));

                // Restricción de seguridad: Previene la eliminación del administrador principal (ID 1).
                if (idUsuario == 1) { 
                    response.sendRedirect("admin.jsp?error=delete_admin");
                    return;
                }

                // Define la consulta SQL para eliminar un usuario por ID.
                String sql = "DELETE FROM usuarios WHERE id_usuario = ?";
                
                PreparedStatement statement = conn.prepareStatement(sql);
                statement.setInt(1, idUsuario);
                
                // Ejecuta la eliminación.
                statement.executeUpdate();
            }

        // --- Manejo de Excepciones ---
        } catch (SQLException | ClassNotFoundException e) {
            // Imprime el rastro de la pila en caso de error de DB o driver.
            e.printStackTrace(); 
            // Redirige al administrador con un error genérico de operación de DB.
            response.sendRedirect("admin.jsp?error=db_op_failed"); 
            return;
        } catch (NumberFormatException e) {
            // Atrapa errores si el ID de usuario no es un número válido.
            response.sendRedirect("admin.jsp?error=invalid_id"); 
            return;
        } finally {
            // Bloque para asegurar que la conexión a la base de datos siempre se cierre.
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        
        // Redirige a un servlet para recargar y mostrar la lista de usuarios actualizada.
        response.sendRedirect("list-users");
    }
}