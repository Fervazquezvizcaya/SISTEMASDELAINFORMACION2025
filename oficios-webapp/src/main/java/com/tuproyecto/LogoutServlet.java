package com.tuproyecto;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet encargado de manejar la lógica del cierre de sesión (logout).
 */
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    /**
     * Procesa las solicitudes GET para cerrar la sesión del usuario.
     * @param request El objeto HttpServletRequest que contiene la solicitud del cliente.
     * @param response El objeto HttpServletResponse que contiene la respuesta que se enviará al cliente.
     * @throws ServletException Si ocurre un error específico del servlet.
     * @throws IOException Si ocurre un error de entrada o salida.
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Establece la codificación de caracteres de la solicitud a UTF-8.
        request.setCharacterEncoding("UTF-8");
        // Establece la codificación de caracteres de la respuesta a UTF-8.
        response.setCharacterEncoding("UTF-8");
        
        // Obtiene la sesión actual sin crear una nueva si no existe (false).
        HttpSession session = request.getSession(false);
        
        // Verifica si existe una sesión activa.
        if (session != null) {
            // Invalida y destruye la sesión existente.
            session.invalidate(); 
        }
        
        // Redirige al usuario a la página principal o de inicio de sesión.
        response.sendRedirect("index.html"); 
    }
}