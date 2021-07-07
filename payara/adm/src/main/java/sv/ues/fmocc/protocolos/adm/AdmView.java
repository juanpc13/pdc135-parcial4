/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sv.ues.fmocc.protocolos.adm;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.InvalidAttributeValueException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.servlet.http.HttpSession;
import sv.ues.fmocc.protocolos.adm.entity.UserLDAP;
import sv.ues.fmocc.protocolos.adm.utils.SessionUtils;
import sv.ues.fmocc.protocolos.adm.utils.SingleLDAP;

/**
 *
 * @author jcpleitez
 */
@Named
@ViewScoped
public class AdmView implements Serializable {

    private UserLDAP user;
    private UserLDAP selectedUsuario;
    private List<UserLDAP> usuarios;
    private SingleLDAP singleLDAP;

    private Properties properties;
    private String localIp;

    @PostConstruct
    public void init() {
        user = new UserLDAP();
        updateUSER();
        usuarios = new ArrayList<>();
        // Se cargan las propiedades del proyecto
        properties = getProperties();
    }
    
    public Properties getProperties() {
        try {
            InputStream input = new FileInputStream(System.getProperty("user.home") + "/adm.properties");
            //properties.load(FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream("/WEB-INF/adm.properties"));
            properties = new Properties();
            properties.load(input);
            localIp = InetAddress.getLocalHost().toString();
        } catch (IOException ex) {
            Logger.getLogger(LogInView.class.getName()).log(Level.SEVERE, null, ex);
        }
        return properties;
    }

    public UserLDAP getUser() {
        return user;
    }

    public void setUser(UserLDAP user) {
        this.user = user;
    }

    public UserLDAP getSelectedUsuario() {
        return selectedUsuario;
    }

    public void setSelectedUsuario(UserLDAP selectedUsuario) {
        this.selectedUsuario = selectedUsuario;
    }

    public List<UserLDAP> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<UserLDAP> usuarios) {
        this.usuarios = usuarios;
    }

    public String getLocalIp() {
        return localIp;
    }

    public void setLocalIp(String localIp) {
        this.localIp = localIp;
    }

    public void updateUID() {
        //user.setCn("");//user.setSn("");//user.setUserPassword("");//user.setUid("");       
        //Creando UID
        String uid = "";
        uid += user.getCn() == null ? "" : user.getCn().toLowerCase();
        uid += user.getCn() == null || user.getCn().isEmpty() || user.getSn() == null || user.getSn().isEmpty() ? "" : ".";
        uid += user.getSn() == null ? "" : user.getSn().toLowerCase();
        user.setUid(uid);
        updateUSER();
    }

    public void updateUSER() {
        if(properties == null){
            addMessage(FacesMessage.SEVERITY_INFO, "No se han definido propiedades del proyecto", "");
            return;
        }
        // Dependen del UID y de las propiedades definidas
        user.setHomeDirectory(properties.getProperty("default.homeDirectory").replace("$UID", user.getUid()));
        user.setMail(properties.getProperty("default.mail").replace("$UID", user.getUid()));
        user.setMailbox(properties.getProperty("default.mailbox").replace("$UID", user.getUid()));
    }

    public void crearUsuario() {
        if(properties == null){
            addMessage(FacesMessage.SEVERITY_INFO, "No se han definido propiedades del proyecto", "");
            return;
        }
        if (user.isComplete()) {
            Attributes attributes = user.generateAttributes();
            try {
                singleLDAP = new SingleLDAP(properties, SessionUtils.getUserUserDn(), SessionUtils.getUserPassword());
                singleLDAP.getContext().createSubcontext(properties.getProperty("usuarios.create").replace("$UID", user.getUid()), attributes);
                singleLDAP.getContext().close();
                addMessage(FacesMessage.SEVERITY_INFO, "Correo creado", "");
                user = new UserLDAP();
                cargarUsuarios();
            } catch (NamingException ex) {
                if (ex instanceof NameAlreadyBoundException) {
                    //Logger.getLogger(AdmView.class.getName()).log(Level.SEVERE, null, ex);
                    addMessage(FacesMessage.SEVERITY_INFO, "Usuario ya existe", "");

                } else if (ex instanceof CommunicationException) {
                    Logger.getLogger(AdmView.class.getName()).log(Level.SEVERE, null, ex);
                    addMessage(FacesMessage.SEVERITY_ERROR, "Error al comunicarse con el host", "");

                } else if (ex instanceof InvalidAttributeValueException) {
                    //Logger.getLogger(AdmView.class.getName()).log(Level.SEVERE, null, ex);
                    addMessage(FacesMessage.SEVERITY_ERROR, "Atributos invalidos en el arbol", "");

                } else if (ex instanceof AuthenticationException) {
                    //Logger.getLogger(AdmView.class.getName()).log(Level.SEVERE, null, ex);
                    addMessage(FacesMessage.SEVERITY_ERROR, "Credenciales Invalidas", "");

                } else {
                    Logger.getLogger(AdmView.class.getName()).log(Level.SEVERE, null, ex);
                    addMessage(FacesMessage.SEVERITY_ERROR, "Error no definido", "");

                }
            }
        }
    }

    public void cargarUsuarios() {
        usuarios = new ArrayList<>();
        if(properties == null){
            addMessage(FacesMessage.SEVERITY_INFO, "No se han definido propiedades del proyecto", "");
            return;
        }
        String searchFilter = properties.getProperty("usuarios.filtro");
        //String[] reqAtt = {"mail", "mailbox"};
        String[] reqAtt = user.fieldsList();
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setReturningAttributes(reqAtt);

        try {
            singleLDAP = new SingleLDAP(properties, SessionUtils.getUserUserDn(), SessionUtils.getUserPassword());
            NamingEnumeration users = singleLDAP.getContext().search(properties.getProperty("usuarios.dn"), searchFilter, controls);
            singleLDAP.getContext().close();

            SearchResult result = null;
            while (users.hasMore()) {
                result = (SearchResult) users.next();
                Attributes attr = result.getAttributes();
                UserLDAP u = new UserLDAP();
                u = u.BuildUserLDAP(attr);
                usuarios.add(u);
            }
        } catch (NamingException ex) {
            if (ex instanceof CommunicationException) {
                Logger.getLogger(AdmView.class.getName()).log(Level.SEVERE, null, ex);
                addMessage(FacesMessage.SEVERITY_ERROR, "Error al comunicarse con el host", "");

            } else if (ex instanceof AuthenticationException) {
                //Logger.getLogger(AdmView.class.getName()).log(Level.SEVERE, null, ex);
                addMessage(FacesMessage.SEVERITY_ERROR, "Credenciales Invalidas", "");

            } else {
                Logger.getLogger(AdmView.class.getName()).log(Level.SEVERE, null, ex);
                addMessage(FacesMessage.SEVERITY_ERROR, "Error no definido", "");

            }
        }
    }

    public void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }
    
    public String logout() {
        HttpSession session = SessionUtils.getSession();
        session.invalidate();
        return "login";
    }

}
