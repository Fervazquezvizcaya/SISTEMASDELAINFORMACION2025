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

        // 2. Obtener Parámetros de Búsqueda
        String query = request.getParameter("q"); // Obtener el término de búsqueda
        
        // 3. Obtener credenciales de entorno (Seguridad)
        String dbUser = System.getenv("DB_USER"); 
        String dbPassword = System.getenv("DB_PASSWORD");

        if (dbUser == null || dbPassword == null) {
            // Error de configuración de seguridad
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

            // 🛑 CORRECCIÓN CLAVE: Sentencia SQL para búsqueda y listado global
            String sql = "SELECT id_oficio, numero_oficio, persona_dirigida, area, asunto, fecha, hash_firma " +
                         "FROM oficios ";
            
            // 🚨 LÓGICA DE BÚSQUEDA (WHERE LIKE)
            if (query != null && !query.trim().isEmpty()) {
                sql += " WHERE UPPER(CAST(id_oficio AS CHAR)) LIKE ? " +
                       " OR UPPER(CAST(numero_oficio AS CHAR)) LIKE ? " +
                       " OR UPPER(persona_dirigida) LIKE ? " +
                       " OR UPPER(area) LIKE ? " +
                       " OR UPPER(asunto) LIKE ? " +
                       " OR UPPER(fecha) LIKE ? " +
                       " OR UPPER(hash_firma) LIKE ? ";
            }
            
            sql += " ORDER BY id_oficio DESC"; // Ordenar para mostrar los nuevos primero
            
            statement = conn.prepareStatement(sql);
            
            // 🚨 Asignar parámetros de búsqueda si existen
            if (query != null && !query.trim().isEmpty()) {
                String searchPattern = "%" + query.toUpperCase() + "%";
                // Asigna el mismo patrón a todos los 7 placeholders (?)
                for (int i = 1; i <= 7; i++) {
                    statement.setString(i, searchPattern);
                }
            }
            
            result = statement.executeQuery();

            // 5. Llenar la lista de oficios
            while (result.next()) {
                int idOficioReal = result.getInt("id_oficio");
                int numeroOficio = result.getInt("numero_oficio"); 
                String personaDirigida = result.getString("persona_dirigida"); 
                String area = result.getString("area");
                String asunto = result.getString("asunto");
                String fecha = result.getString("fecha");
                String hash = result.getString("hash_firma");

                // Usamos el constructor modificado (ID Real, ID Presentación, Resto)
                oficioList.add(new Oficio(idOficioReal, numeroOficio, personaDirigida, area, asunto, fecha, hash));
            }
            
            // 6. Establecer la lista y la query en el Request
            request.setAttribute("oficioList", oficioList);
            request.setAttribute("queryTerm", query); // Guarda el término para que el buscador lo muestre
            
            // 7. Transferir el control a la vista (JSP)
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