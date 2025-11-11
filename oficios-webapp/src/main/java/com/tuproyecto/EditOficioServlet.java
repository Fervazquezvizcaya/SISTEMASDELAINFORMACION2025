package com.tuproyecto;

// Importaciones necesarias para el Servlet y la gestión de la base de datos (JDBC)

import java.io.IOException;           // Maneja errores de entrada/salida (e.g., al redirigir).
import java.sql.Connection;           // Interfaz para gestionar la conexión a la base de datos.
import java.sql.DriverManager;        // Gestiona los drivers y establece la conexión a la DB.
import java.sql.PreparedStatement;    // Permite ejecutar consultas SQL precompiladas (seguridad y eficiencia).
import java.sql.ResultSet;            // Almacena el resultado de una consulta SELECT.
import java.sql.SQLException;         // Maneja errores relacionados con la base de datos.

import jakarta.servlet.ServletException;      // Excepción base para problemas en el ciclo de vida del Servlet.
import jakarta.servlet.annotation.WebServlet; // Anotación que mapea el Servlet a la URL "/edit-oficio".
import jakarta.servlet.http.HttpServlet;      // Clase base para Servlets HTTP.
import jakarta.servlet.http.HttpServletRequest; // Objeto que representa la solicitud del cliente.
import jakarta.servlet.http.HttpServletResponse; // Objeto que permite enviar la respuesta al cliente.
import jakarta.servlet.http.HttpSession;      // Permite manejar la sesión del usuario para la autenticación.

@WebServlet("/edit-oficio")
public class EditOficioServlet extends HttpServlet {

    // Constante que define la URL de conexión a la base de datos.
    private static final String DB_URL = "jdbc:mariadb://54.242.175.198:3306/webapp_db?sslMode=disable";

    /**
     * Maneja las solicitudes GET.
     * Propósito: Cargar los datos de un oficio específico en el formulario de edición.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        // 1. Verificación de Seguridad: Asegura que el usuario esté autenticado.
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("index.html");
            return;
        }

        // 2. Obtener y validar el ID del oficio desde los parámetros de la URL.
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isEmpty()) {
            response.sendRedirect("list-oficios-user?error=no_id");
            return;
        }
        
        int oficioId = 0;
        try {
            oficioId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            // Redirigir si el ID no es un formato numérico válido.
            response.sendRedirect("list-oficios-user?error=invalid_id");
            return;
        }

        // 3. Obtener credenciales de la base de datos desde variables de entorno.
        String dbUser = System.getenv("DB_USER");
        String dbPassword = System.getenv("DB_PASSWORD");
        
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        Oficio oficio = null;

        try {
            // 4. Conexión a la base de datos
            Class.forName("org.mariadb.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, dbUser, dbPassword); 

            // Consulta SQL para seleccionar todos los campos del oficio por su ID.
            String sql = "SELECT id_oficio, numero_oficio, persona_dirigida, area, asunto, fecha, hash_firma FROM oficios WHERE id_oficio = ?"; 
            
            statement = conn.prepareStatement(sql);
            statement.setInt(1, oficioId);
            result = statement.executeQuery();
            
            if (result.next()) {
                // 5. Mapear los datos obtenidos de la DB al objeto Oficio.
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
                
                // 6. Enviar el objeto Oficio a la vista JSP para prellenar el formulario.
                request.getRequestDispatcher("edit_oficio.jsp").forward(request, response); 
                
            } else {
                // Redirigir si el ID es válido pero el oficio no existe.
                response.sendRedirect("list-oficios-user?error=oficio_not_found");
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            // Manejo de errores de conexión o consulta.
            response.sendRedirect("list-oficios-user?error=db_query_failed");
        } finally {
            // 7. Cierre seguro de todos los recursos de la base de datos.
            try {
                if (result != null) result.close();
                if (statement != null) statement.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Maneja las solicitudes POST.
     * Propósito: Recibir los datos del formulario de edición y ejecutar la actualización en la DB.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        // 1. Verificación de Seguridad: Asegura que el usuario esté autenticado.
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("index.html");
            return;
        }

        // 2. Obtener credenciales de la base de datos
        String dbUser = System.getenv("DB_USER");
        String dbPassword = System.getenv("DB_PASSWORD");
        
        Connection conn = null;
        PreparedStatement statement = null;

        try {
            // 3. Obtener el ID (clave) y los campos editables del formulario.
            int oficioId = Integer.parseInt(request.getParameter("id_oficio"));
            String personaDirigida = request.getParameter("persona_dirigida");
            String area = request.getParameter("area");
            String fecha = request.getParameter("fecha");
            String asunto = request.getParameter("asunto");
            
            // 4. Conexión a la base de datos
            Class.forName("org.mariadb.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, dbUser, dbPassword);
            
            // INICIO DE TRANSACCIÓN: Deshabilita auto-commit para control manual.
            conn.setAutoCommit(false); 

            // 5. Sentencia SQL de actualización, utilizando el ID en la cláusula WHERE.
            String sql = "UPDATE oficios SET persona_dirigida=?, area=?, asunto=?, fecha=? WHERE id_oficio=?";
            
            statement = conn.prepareStatement(sql);
            
            // 6. Asignación de parámetros (debe coincidir con el orden de la sentencia SQL).
            statement.setString(1, personaDirigida);
            statement.setString(2, area);
            statement.setString(3, asunto);
            statement.setString(4, fecha);
            statement.setInt(5, oficioId); // Clave primaria para la condición WHERE
            
            // 7. Ejecución del UPDATE.
            statement.executeUpdate();
            
            // FIN DE TRANSACCIÓN: Confirma los cambios a la base de datos (COMMIT).
            conn.commit(); 
            
            // Redirigir al listado con mensaje de éxito de actualización.
            response.sendRedirect("list-oficios-user?success=updated"); 

        } catch (SQLException | NumberFormatException | ClassNotFoundException e) {
            e.printStackTrace();
            // 8. Manejo de errores: Intenta revertir la transacción (ROLLBACK).
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            response.sendRedirect("list-oficios-user?error=update_failed");
        } finally {
            // 9. Cierre seguro de recursos en el bloque 'finally'.
            try {
                if (statement != null) statement.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}