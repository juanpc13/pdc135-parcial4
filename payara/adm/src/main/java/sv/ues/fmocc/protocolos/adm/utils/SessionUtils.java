/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sv.ues.fmocc.protocolos.adm.utils;

import java.io.IOException;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author jcpleitez
 */
public class SessionUtils {

    public static HttpSession getSession() {
        return (HttpSession) FacesContext.getCurrentInstance()
                .getExternalContext().getSession(false);
    }

    public static HttpServletRequest getRequest() {
        return (HttpServletRequest) FacesContext.getCurrentInstance()
                .getExternalContext().getRequest();
    }

    public static String getUserUserDn() {
        HttpSession session = getSession();
        if (session != null) {
            return (String) session.getAttribute("userDn");
        } else {
            return null;
        }
    }
    
    public static String getUserPassword() {
        HttpSession session = getSession();
        if (session != null) {
            return (String) session.getAttribute("userPassword");
        } else {
            return null;
        }
    }
}
