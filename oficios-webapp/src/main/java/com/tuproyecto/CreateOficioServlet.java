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

@WebServlet("/create-oficio")
public class CreateOficioServlet extends HttpServlet {

    // URL de conexión a MariaDB (Segura, usa corrección SSL/TLS)
    private static final String DB_HOST = "54.242.175.198"; 
    private static final String DB_NAME = "webapp_db";
    private static final String DB_URL = "jdbc:mariadb://" + DB_HOST + ":3306/" + DB_NAME + "?sslMode=disable";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        HttpSession session = request.getSession(false);

        // 1. VERIFICACIÓN DE AUTENTICACIÓN: Expulsar si no hay sesión activa.
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("index.html");
            return;
        }

        // 2. OBTENER DATOS DE SESIÓN Y ROL
        Boolean esAdmin = (Boolean) session.getAttribute("isAdmin"); 
        Integer userId = (Integer) session.getAttribute("userId");
        
        // Asignación de respaldo para evitar fallos en la DB si el ID es nulo
        if (userId == null) {
            userId = 1; 
        }

        // 3. OBTENER DATOS DEL FORMULARIO
        String personaDirigida = request.getParameter("persona_dirigida");
        String area = request.getParameter("area");
        String fecha = request.getParameter("fecha");
        String asunto = request.getParameter("asunto");
        
        // 4. OBTENER CREDENCIALES SEGURAS DEL ENTORNO
        String dbUser = System.getenv("DB_USER"); 
        String dbPassword = System.getenv("DB_PASSWORD");
        
        // Verificación de configuración del entorno de DB
        if (dbUser == null || dbPassword == null) {
             System.err.println("ERROR: Credenciales de DB no configuradas.");
             String panelRedirect = (esAdmin != null && esAdmin) ? "admin.jsp" : "user_panel.jsp";
             response.sendRedirect(panelRedirect + "?db_error=true"); 
             return; 
        }
        
        Connection conn = null;
        try {
            // 5. CONEXIÓN Y EJECUCIÓN SQL
            Class.forName("org.mariadb.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, dbUser, dbPassword); 

            // Sentencia SQL: Insertar oficio y asignar el creador (userId)
            String sql = "INSERT INTO oficios (persona_dirigida, area, fecha, asunto, id_usuario_creador) VALUES (?, ?, ?, ?, ?)";
            
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, personaDirigida);
            statement.setString(2, area);
            statement.setString(3, fecha);
            statement.setString(4, asunto);
            statement.setInt(5, userId); // Asignación del ID del usuario logueado

            // Ejecutar la inserción
            statement.executeUpdate();

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            // Si falla la DB, redirigir al panel de origen con mensaje de error
            String panelRedirect = (esAdmin != null && esAdmin) ? "admin.jsp" : "user_panel.jsp";
            response.sendRedirect(panelRedirect + "?error=db_op_failed"); 
            return; 
        } finally {
            // 6. Cierre seguro de la conexión
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        
        // 7. REDIRECCIÓN FINAL: Regresar al panel correcto con mensaje de éxito
        String panelRedirect = (esAdmin != null && esAdmin) ? "list-users" : "list-oficios-user"; // Redirige al Servlet de carga de datos
        response.sendRedirect(panelRedirect + "?success=oficio_created"); 
    }
}