#Sistema Operativo base
FROM debian:bullseye

#Configuraciones para slapd
RUN echo "slapd slapd/internal/generated_adminpw password pdc135" | debconf-set-selections &&\
echo "slapd slapd/internal/adminpw password pdc135" | debconf-set-selections &&\
echo "slapd slapd/password2 password pdc135" | debconf-set-selections &&\
echo "slapd slapd/password1 password pdc135" | debconf-set-selections &&\
echo "slapd slapd/domain string riguas.com" | debconf-set-selections &&\
echo "slapd shared/organization string riguas" | debconf-set-selections &&\
echo "slapd slapd/backend string MDB" | debconf-set-selections &&\
echo "slapd slapd/purge_database boolean true" | debconf-set-selections &&\
echo "slapd slapd/move_old_database boolean true" | debconf-set-selections &&\
echo "slapd slapd/allow_ldap_v2 boolean false" | debconf-set-selections &&\
echo "slapd slapd/no_configuration boolean false" | debconf-set-selections &&\
echo "slapd slapd/dump_database select when needed" | debconf-set-selections

#Actualizar repositorios e instalar paquetes necesarios
RUN apt update && apt install -yq slapd ldap-utils wget 

#Cambiar listas de control de acceso, cambiar contraseña de config, creación de usuario para réplica, cargar módulo syncprov, 
RUN service slapd start && \
echo "\
dn: olcDatabase={1}mdb,cn=config\n\
changetype: modify\n\
replace: olcAccess\n\
olcAccess: {0}to attrs=userPassword by self write by anonymous auth by dn=\"cn=replica,dc=riguas,dc=com\" read by * none\n\
olcAccess: {1}to attrs=shadowLastChange by self write by * read\n\
olcAccess: {2}to * by dn=\"cn=aplicacion,dc=riguas,dc=com\" write by dn=\"cn=admin,dc=riguas,dc=com\" write \
by dn=\"cn=replica,dc=riguas,dc=com\" read by * none" \
> tmp.ldif && \
ldapmodify -w "pdc135" -Y EXTERNAL -H ldapi:/// -f tmp.ldif && \
echo "\
dn: cn=config\n\
changetype: modify\n\n\
dn: olcDatabase={0}config,cn=config\n\
changetype: modify\n\
add: olcRootPW\n\
olcRootPW: {MD5}rOMv3+NbPnOWfYV34E1vaA==\n\n\
dn: olcDatabase={0}config,cn=config\n\
changetype: modify\n\
add: olcAccess\n\
olcAccess: {1}to * by dn="cn=admin,cn=config" write" > tmp.ldif && \
ldapadd -w "pdc135" -Y EXTERNAL -H ldapi:/// -f tmp.ldif && \
echo "\
dn: cn=replica,dc=riguas,dc=com\n\
objectClass: simpleSecurityObject\n\
objectClass: organizationalRole\n\
description: Replication User\n\
userPassword: {MD5}rOMv3+NbPnOWfYV34E1vaA==" > tmp.ldif && \
ldapadd -x -w "pdc135" -D "cn=admin,dc=riguas,dc=com" -f tmp.ldif && \
echo "\
dn: cn=module,cn=config\n\
objectClass: olcModuleList\n\
cn: module\n\
olcModulePath: /usr/lib/ldap\n\
olcModuleLoad: syncprov.la" > tmp.ldif && \
ldapadd -x -w "pdc135" -D "cn=admin,cn=config" -f tmp.ldif && \
echo "\
dn: olcOverlay=syncprov,olcDatabase={1}mdb,cn=config\n\
objectClass: olcOverlayConfig\n\
objectClass: olcSyncProvConfig\n\
olcOverlay: syncprov\n\
olcSpSessionLog: 100" > tmp.ldif && \
ldapadd -w "pdc135" -Y EXTERNAL -H ldapi:/// -f tmp.ldif && \
echo "\
dn: ou=usuarios,dc=riguas,dc=com\n\
objectClass: organizationalUnit\n\
objectClass: top\nou: usuarios" > tmp.ldif && \
ldapadd -x -w "pdc135" -D "cn=admin,dc=riguas,dc=com" -f tmp.ldif && \
rm tmp.ldif && \
service slapd stop
#EXPOSE 389

#Iniciar slapd en modo debug
ENTRYPOINT slapd -d -1
