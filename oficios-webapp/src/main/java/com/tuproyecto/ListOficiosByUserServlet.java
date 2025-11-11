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

@WebServlet("/list-oficios-user")
public class ListOficiosByUserServlet extends HttpServlet {

    // URL de la base de datos con corrección SSL/TLS
    private static final String DB_URL = "jdbc:mariadb://54.242.175.198:3306/webapp_db?sslMode=disable";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        
        // 1. Verificar Sesión (Seguridad)
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("index.html");
            return;
        }

        // 2. Obtener credenciales de entorno (Seguridad)
        String dbUser = System.getenv("DB_USER"); 
        String dbPassword = System.getenv("DB_PASSWORD");

        if (dbUser == null || dbPassword == null) {
            response.sendRedirect("user_panel.jsp?error=db_config");
            return;
        }

        List<Oficio> oficioList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            Class.forName("org.mariadb.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, dbUser, dbPassword); 

            // 🚨 CORRECCIÓN CLAVE 1: SELECCIONAR id_oficio y numero_oficio
            String sql = "SELECT id_oficio, numero_oficio, persona_dirigida, area, asunto, fecha, hash_firma " +
                         "FROM oficios ORDER BY id_oficio DESC"; 
            
            statement = conn.prepareStatement(sql);
            result = statement.executeQuery();

            // 🚨 CORRECCIÓN CLAVE 2: Llenar la lista con ambos IDs
            while (result.next()) {
                // Obtener la clave primaria real (con saltos)
                int idOficioReal = result.getInt("id_oficio"); 
                // Obtener el número consecutivo (sin saltos)
                int numeroOficio = result.getInt("numero_oficio"); 
                
                String personaDirigida = result.getString("persona_dirigida"); 
                String area = result.getString("area");
                String asunto = result.getString("asunto");
                String fecha = result.getString("fecha");
                String hash = result.getString("hash_firma");

                // 🚨 Crear objeto Oficio: (ID Real, ID para la tabla, resto de campos)
                oficioList.add(new Oficio(idOficioReal, numeroOficio, personaDirigida, area, asunto, fecha, hash));
            }
            
            // 5. Establecer la lista en el Request
            request.setAttribute("oficioList", oficioList);

            // 6. Transferir el control a la vista (JSP)
            request.getRequestDispatcher("user_panel.jsp").forward(request, response);
            
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            response.sendRedirect("user_panel.jsp?error=db_query_failed");
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