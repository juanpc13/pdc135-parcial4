#Sistema operativo base
FROM debian:bullseye

#Variables de entorno
ENV SERVICIO masterldap

#Configuraciones para slapd
RUN echo "slapd slapd/internal/generated_adminpw password pdc135" | debconf-set-selections && \
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

#Cambiar lista de control de acceso, crear usuario para réplica, configurar el módulo sycrepl para la replicación
RUN service slapd start && \
echo "\
dn: olcDatabase={1}mdb,cn=config\n\
changetype: modify\n\
replace: olcAccess\n\
olcAccess: {0}to attrs=userPassword by self write by anonymous auth \
by dn=\"cn=replica,dc=riguas,dc=com\" write by * none\n\
olcAccess: {1}to attrs=shadowLastChange by self write by * read\n\
olcAccess: {2}to * by dn=\"cn=admin,dc=riguas,dc=com\" \
write by dn=\"cn=replica,dc=riguas,dc=com\" write by * none" \
> tmp.ldif && \
ldapmodify -w "pdc135" -Y EXTERNAL -H ldapi:/// -f tmp.ldif && \
echo "\
dn: cn=replica,dc=riguas,dc=com\n\
objectClass: simpleSecurityObject\n\
objectClass: organizationalRole\n\
description: Replication User\n\
userPassword: {MD5}rOMv3+NbPnOWfYV34E1vaA==" > tmp.ldif && \
ldapadd -x -w "pdc135" -D "cn=admin,dc=riguas,dc=com" -f tmp.ldif && \
echo "\
dn: olcDatabase={1}mdb,cn=config\n\
changetype: modify\n\
add: olcSyncRepl\n\
olcSyncRepl: rid=001\n\
  provider=ldap://HOSTMASTER/\n\
  bindmethod=simple\n\
  binddn=\"cn=replica,dc=riguas,dc=com\"\n\
  credentials=pdc135\n\
  searchbase=\"dc=riguas,dc=com\"\n\
  scope=sub\n\
  schemachecking=on\n\
  type=refreshAndPersist\n\
  retry=\"30 5 300 3\"\n\
  interval=00:00:05:00" > tmp.ldif && \
service slapd stop

#Hacer la asignación de las variables de entorno e iniciar slapd
ENTRYPOINT service slapd start && \
sed -i 's/HOSTMASTER/'$SERVICIO'/g' tmp.ldif && \
cat tmp.ldif && \
ldapmodify -w "pdc135" -Y EXTERNAL  -H ldapi:/// -f tmp.ldif && \
rm tmp.ldif && \
SLAPD_PID=$(cat /run/slapd/slapd.pid) && \
kill -15 $SLAPD_PID && \
while [ -e /proc/$SLAPD_PID ]; do sleep 0.1; done && \
slapd -d -1




