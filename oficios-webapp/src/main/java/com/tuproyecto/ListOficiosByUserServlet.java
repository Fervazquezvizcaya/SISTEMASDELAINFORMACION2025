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

    // URL de conexión a la base de datos (incluye corrección SSL/TLS)
    private static final String DB_URL = "jdbc:mariadb://54.242.175.198:3306/webapp_db?sslMode=disable";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        
        // 1. VERIFICACIÓN DE SESIÓN: Asegura que el usuario esté logueado.
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("index.html");
            return;
        }

        // 2. OBTENER PARÁMETROS DE BÚSQUEDA (Query 'q')
        String query = request.getParameter("q");
        
        // 3. OBTENER CREDENCIALES SEGURAS DEL ENTORNO
        String dbUser = System.getenv("DB_USER"); 
        String dbPassword = System.getenv("DB_PASSWORD");

        // Verificación de configuración de seguridad
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

            // 4. PREPARACIÓN DE LA CONSULTA SQL (Listado Global + Búsqueda)
            String sql = "SELECT id_oficio, numero_oficio, persona_dirigida, area, asunto, fecha, hash_firma " +
                         "FROM oficios ";
            
            // LÓGICA DE BÚSQUEDA: Añadir cláusula WHERE si hay un término de búsqueda.
            if (query != null && !query.trim().isEmpty()) {
                sql += " WHERE UPPER(CAST(id_oficio AS CHAR)) LIKE ? " +
                       " OR UPPER(CAST(numero_oficio AS CHAR)) LIKE ? " +
                       " OR UPPER(persona_dirigida) LIKE ? " +
                       " OR UPPER(area) LIKE ? " +
                       " OR UPPER(asunto) LIKE ? " +
                       " OR UPPER(fecha) LIKE ? " +
                       " OR UPPER(hash_firma) LIKE ? ";
            }
            
            sql += " ORDER BY id_oficio DESC"; // Ordena por ID descendente
            
            statement = conn.prepareStatement(sql);
            
            // 5. ASIGNACIÓN DE PARÁMETROS: Aplica el patrón de búsqueda a todos los campos.
            if (query != null && !query.trim().isEmpty()) {
                String searchPattern = "%" + query.toUpperCase() + "%";
                // Siete placeholders (?) requieren el patrón
                for (int i = 1; i <= 7; i++) {
                    statement.setString(i, searchPattern);
                }
            }
            
            result = statement.executeQuery();

            // 6. MAPEO DE RESULTADOS: Llenar la lista de objetos Oficio.
            while (result.next()) {
                // Obtener el ID_OFICIO real (clave primaria)
                int idOficioReal = result.getInt("id_oficio"); 
                // Obtener el NUMERO_OFICIO (consecutivo para la vista)
                int numeroOficio = result.getInt("numero_oficio"); 
                
                String personaDirigida = result.getString("persona_dirigida"); 
                String area = result.getString("area");
                String asunto = result.getString("asunto");
                String fecha = result.getString("fecha");
                String hash = result.getString("hash_firma");

                // Pasar los 7 argumentos al constructor
                oficioList.add(new Oficio(idOficioReal, numeroOficio, personaDirigida, area, asunto, fecha, hash));
            }
            
            // 7. TRANSFERENCIA DE CONTROL: Enviar la lista y el término de búsqueda a la vista JSP.
            request.setAttribute("oficioList", oficioList);
            request.setAttribute("queryTerm", query); 
            request.getRequestDispatcher("user_panel.jsp").forward(request, response);
            
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            // Fallo en la conexión o consulta, redirige con error.
            response.sendRedirect("user_panel.jsp?error=db_query_failed");
        } finally {
            // 8. Cierre seguro de recursos.
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