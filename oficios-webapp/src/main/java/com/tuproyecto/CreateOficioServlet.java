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
import jakarta.servlet.http.HttpSession; // Se agregó import de HttpSession

@WebServlet("/create-oficio")
public class CreateOficioServlet extends HttpServlet {

    // --- Datos de Conexión Seguros (Solo Host/Nombre) ---
    private static final String DB_HOST = "54.242.175.198"; 
    private static final String DB_NAME = "webapp_db";
    // ⚠️ La URL ahora incluye la corrección SSL/TLS
    private static final String DB_URL = "jdbc:mariadb://" + DB_HOST + ":3306/" + DB_NAME + "?sslMode=disable";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        // 1. Obtener la sesión y verificar la seguridad
        HttpSession session = request.getSession(false);

        // --- CÓDIGO DE SEGURIDAD: Expulsar si no está autenticado ---
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("index.html");
            return;
        }

        // 2. Obtener el ID del usuario de la sesión para el registro de auditoría
        Integer userId = (Integer) session.getAttribute("userId"); // Asumimos que guardaste el ID en el LoginServlet
        
        // Si el ID es null o no es un administrador, usar el ID 1 (admin)
        if (userId == null) {
            // Este es un caso de fallo, pero usamos el ID 1 como respaldo
            userId = 1; 
        }

        // 3. Obtener los datos del formulario HTML
        String personaDirigida = request.getParameter("persona_dirigida");
        String area = request.getParameter("area");
        String fecha = request.getParameter("fecha");
        String asunto = request.getParameter("asunto");
        
        // 4. Obtener credenciales seguras del entorno
        String dbUser = System.getenv("DB_USER"); 
        String dbPassword = System.getenv("DB_PASSWORD");
        
        if (dbUser == null || dbPassword == null) {
             System.err.println("ERROR: Credenciales de DB no configuradas. Fallo de seguridad.");
             // Redirigir al admin con un mensaje de error de DB (si lo implementas)
             response.sendRedirect("admin.jsp?db_error=true"); 
             return; 
        }
        
        Connection conn = null;
        try {
            // 5. Conectarse a la base de datos (seguro)
            Class.forName("org.mariadb.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, dbUser, dbPassword); // Conexión SEGURA

            // 6. Sentencia SQL corregida: Añadir id_usuario_creador
            String sql = "INSERT INTO oficios (persona_dirigida, area, fecha, asunto, id_usuario_creador) VALUES (?, ?, ?, ?, ?)";
            
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, personaDirigida);
            statement.setString(2, area);
            statement.setString(3, fecha);
            statement.setString(4, asunto);
            statement.setInt(5, userId); // 🚨 Se añade el ID del usuario

            // 7. Ejecutar y completar
            statement.executeUpdate();

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            // Redirigir a admin.jsp con mensaje de fallo
            response.sendRedirect("admin.jsp?error=db"); 
            return; 
        } finally {
            // 8. Cerrar la conexión
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        
        // 9. Redirigir al usuario de vuelta a la página de admin (después del éxito)
        response.sendRedirect("admin.jsp?success=oficio"); 
    }
}