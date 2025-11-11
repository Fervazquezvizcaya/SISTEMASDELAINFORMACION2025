package com.tuproyecto;

import java.io.IOException;
import java.sql.*;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/edit-oficio")
public class EditOficioServlet extends HttpServlet {

    private static final String DB_URL = "jdbc:mariadb://54.242.175.198:3306/webapp_db?sslMode=disable";

    // --- Lógica para CARGAR el oficio en el formulario (doGet) ---
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("index.html");
            return;
        }

        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isEmpty()) {
            response.sendRedirect("list-oficios-user?error=no_id");
            return;
        }
        
        int oficioId = 0;
        try {
            oficioId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            response.sendRedirect("list-oficios-user?error=invalid_id");
            return;
        }

        String dbUser = System.getenv("DB_USER");
        String dbPassword = System.getenv("DB_PASSWORD");
        
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        Oficio oficio = null;

        try {
            Class.forName("org.mariadb.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, dbUser, dbPassword); 

            // Se obtiene id_oficio real y numero_oficio
            String sql = "SELECT id_oficio, numero_oficio, persona_dirigida, area, asunto, fecha, hash_firma FROM oficios WHERE id_oficio = ?"; 
            
            statement = conn.prepareStatement(sql);
            statement.setInt(1, oficioId);
            result = statement.executeQuery();
            
            if (result.next()) {
                // Obtener los datos del ResultSet y usarlos en el constructor de 7 argumentos
                oficio = new Oficio(
                    result.getInt("id_oficio"), 
                    result.getInt("numero_oficio"), 
                    result.getString("persona_dirigida"),
                    result.getString("area"),
                    result.getString("asunto"),
                    result.getString("fecha"),
                    result.getString("hash_firma")
                );

                request.setAttribute("oficio", oficio);
                
                // Transferir el control al formulario
                request.getRequestDispatcher("edit_oficio.jsp").forward(request, response); 
                
            } else {
                response.sendRedirect("list-oficios-user?error=oficio_not_found");
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            response.sendRedirect("list-oficios-user?error=db_query_failed");
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

    // --- Lógica para ACTUALIZAR los cambios en la DB (doPost) ---
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("index.html");
            return;
        }

        String dbUser = System.getenv("DB_USER");
        String dbPassword = System.getenv("DB_PASSWORD");
        
        Connection conn = null;
        PreparedStatement statement = null;

        try {
            // Obtener el ID del oficio y los campos editables
            int oficioId = Integer.parseInt(request.getParameter("id_oficio"));
            String personaDirigida = request.getParameter("persona_dirigida");
            String area = request.getParameter("area");
            String fecha = request.getParameter("fecha");
            String asunto = request.getParameter("asunto");
            
            // 1. Conexión a la DB
            Class.forName("org.mariadb.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, dbUser, dbPassword);
            
            // 🚨 SOLUCIÓN 1: Deshabilitar auto-commit para asegurar que la transacción se aplique
            conn.setAutoCommit(false); 

            // 2. Sentencia SQL para actualizar SOLO los campos solicitados
            String sql = "UPDATE oficios SET persona_dirigida=?, area=?, asunto=?, fecha=? WHERE id_oficio=?";
            
            statement = conn.prepareStatement(sql);
            
            // 3. Asignación de parámetros (la secuencia debe ser exacta a la sentencia SQL)
            statement.setString(1, personaDirigida);
            statement.setString(2, area);
            statement.setString(3, asunto);
            statement.setString(4, fecha);
            statement.setInt(5, oficioId); // ID para la cláusula WHERE
            
            // 4. Ejecución del UPDATE
            statement.executeUpdate();
            
            // 5. 🚨 SOLUCIÓN 2: Forzar la confirmación de la transacción
            conn.commit(); 
            
            // Redirigir de vuelta a la lista
            response.sendRedirect("list-oficios-user?success=updated"); 

        } catch (SQLException | NumberFormatException | ClassNotFoundException e) {
            e.printStackTrace();
            // Si hay un error, intentar revertir los cambios
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            response.sendRedirect("list-oficios-user?error=update_failed");
        } finally {
            // Cierre seguro de recursos
            try {
                if (statement != null) statement.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}