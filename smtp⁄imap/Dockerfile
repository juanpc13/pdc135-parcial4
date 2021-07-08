FROM debian:bullseye

ENV DOMINIO="torrejas"
ENV RECIBE="riguas"
ENV HOME_VMAIL="/home/vmail"
ENV HOME_DOMAIN="${HOME_VMAIL}/${DOMINIO}"
ENV LDAP_IP="192.168.255.14"
ENV LDAP_PORT="389"

RUN echo "postfix postfix/main_mailer_type string Internet Site" | debconf-set-selections &&\
    echo "postfix postfix/mailname string mail.${DOMINIO}.com" | debconf-set-selections
RUN apt update && apt install -y --no-install-recommends postfix postfix-ldap
##Instalacion de dovecot
RUN apt-get update && apt install -y --no-install-recommends dovecot-imapd dovecot-ldap

##Creacion de directorio para vmail y usuario vmail
RUN groupadd -g 1005 vmail && mkdir ${HOME_VMAIL} && mkdir ${HOME_DOMAIN} && useradd -d ${HOME_VMAIL} -g 1005 -u 1005 vmail && chown -R vmail:vmail ${HOME_VMAIL}/
##Configuraciones del sistema
RUN cp /etc/hosts /var/spool/postfix/etc/ && cp /etc/host.conf /var/spool/postfix/etc/ && cp /etc/localtime /var/spool/postfix/etc/ && cp /etc/nsswitch.conf /var/spool/postfix/etc/ && cp /etc/resolv.conf /var/spool/postfix/etc/ && cp /etc/services /var/spool/postfix/etc/
## configuraciones del master
RUN echo "virtual_mailbox_domains = /etc/postfix/vhosts.txt\nvirtual_mailbox_base = ${HOME_VMAIL}\nvirtual_mailbox_maps = ldap:/etc/postfix/ldap-maps.cf\nvirtual_uid_maps = static:1005\nvirtual_gid_maps = static:1005\nvirtual_transport = dovecot\nmaillog_file=/var/log/mail.log\nsmtpd_sasl_tytpe=dovecot\nsmtpd_sasl_auth_enable=yes\nsmtpd_sasl_security_options=noanonymous" \
    >> /etc/postfix/main.cf && \
    echo "${DOMINIO}.com\n${RECIBE}.com" > /etc/postfix/vhosts.txt &&\
    echo "server_host = ${LDAP_IP}:${LDAP_PORT}\nsearch_base = ou=usuarios,dc=${DOMINIO},dc=com\nbind = yes\nbind_dn = cn=admin,dc=${DOMINIO},dc=com\nbind_pw = pdc135\ntimeout = 2\nquery_filter = mail=%s\nresult_attribute = mailbox"\
    > /etc/postfix/ldap-maps.cf &&\
    sed -i 's/#smtps     inet  n       -       y       -       -       smtpd/smtps     inet  n       -       y       -       -       smtpd\n  -o smtpd_tls_security_level=encrypt/g' /etc/postfix/master.cf &&\
    sed -i 's/#  -o syslog_name=postfix\/smtps/  -o syslog_name=postfix\/smtps/g' /etc/postfix/master.cf &&\
    sed -i 's/#  -o smtpd_tls_wrappermode=yes/  -o smtpd_tls_wrappermode=yes\n  -o smtpd_tls_auth_only=yes/g' /etc/postfix/master.cf


##Creacion de archivo /etc/dovecot/dovecot.conf
RUN echo "!include_try /usr/share/dovecot/protocols.d/*.protocol\n\ndict {\n}\n\n!include conf.d/*.conf\n\n!include_try local.conf\nlogin_greeting = Dovecot ready" > /etc/dovecot/dovecot.conf

##Creacion de archivo /etc/dovecot/dovecot-ldap.conf.ext
RUN echo "hosts = ${LDAP_IP}:${LDAP_PORT}\ndn = cn=admin,dc=${DOMINIO},dc=com\ndnpass = abc123\nauth_bind = yes\nbase = ou=usuarios,dc=${DOMINIO},dc=com\ndebug_level = 1\nldap_version = 3\nderef = always\nscope = subtree\nuser_attrs = homeDirectory=home,mailbox=mail=maildir:${HOME_VMAIL}/%\$,homeDirectory=home\nuser_filter = (&(objectClass=CourierMailAccount)(uid=%n))\npass_attrs = uid=mail,userPassword=password\npass_filter = (&(objectClass=CourierMailAccount)(uid=%n))" > /etc/dovecot/dovecot-ldap.conf.ext

##Creacion de archivo /etc/dovecot/conf.d/auth-ldap.conf.ext
RUN echo "passdb {\ndriver = ldap\nargs = /etc/dovecot/dovecot-ldap.conf.ext\n}\nuserdb {\ndriver = ldap\nargs = /etc/dovecot/dovecot-ldap.conf.ext\ndefault_fields = uid=vmail gid=vmail\n}" > /etc/dovecot/conf.d/auth-ldap.conf.ext

##Creacion de archivo /etc/dovecot/conf.d/10-auth.conf
RUN echo "auth_username_chars = abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890.-_@\n\nauth_mechanisms = plain\n\n!include auth-ldap.conf.ext" > /etc/dovecot/conf.d/10-auth.conf

##Creacion de archivo /etc/dovecot/conf.d/10-master.conf
RUN echo "service imap-login {\n  inet_listener imap {\n   #port = 143\n  }\n\n  inet_listener imaps {\n   #port = 993\n   #ssl = yes\n  }\n}\n\nservice pop3-login {\n inet_listener pop3 {\n   #port = 110\n  }\n  inet_listener pop3s {\n   #port = 995\n   #ssl = yes\n  }\n}\n\nservice submission-login {\n  inet_listener submission {\n   #port = 587\n  }\n}\n\nservice imap {\n}\n\nservice lmtp {\n  inet_listener lmtp {\n   address = 0.0.0.0\n   port = 54321\n  }\n}\n\nservice pop3 {\n}\n\nservice submission {\n}\n\nservice auth {\n\n  unix_listener auth-userdb {\n  }\n\n  #unix_listener /var/spool/postfix/private/auth {\n   #mode = 0666\n   #user = postfix\n   #group = postfix\n  #}\n\n  inet_listener {\n   port = 12345\n  }\n}\n\nauth_mechanisms = plain login\n\nservice auth-worker {\n}\n\nservice dict {\n  unix_listener dict {\n  }\n}" > /etc/dovecot/conf.d/10-master.conf

##Creacion de archivo /etc/dovecot/conf.d/15-lda.conf para autocrear directorios
RUN echo "lda_mailbox_autocreate = yes\nlda_mailbox_autosubscribe = yes\n\nprotocol lda { \n}" > /etc/dovecot/conf.d/15-lda.conf

##Creacion de archivo /etc/dovecot/conf.d/10-logging.conf
RUN echo "log_path = /var/log/mail.log\ninfo_log_path = /var/log/mail.log\ndebug_log_path = /var/log/mail.log\n\nplugin {\n}" > /etc/dovecot/conf.d/10-logging.conf


##Permisos del archivoDovecot 2 /auth-userdb permissions
#RUN chown vmail:vmail /var/run/dovecot/auth-userdb

#Para gluster
#RUN sed -i 's/# If you need to set multiple mailbox locations or want to change default/mail_nfs_index = yes\nmail_nfs_storage = yes/g' /etc/dovecot/conf.d/10-mail.conf &&\
#	sed -i 's/#mmap_disable = no/mmap_disable=yes/g' /etc/dovecot/conf.d/10-mail.conf &&\
#	sed -i 's/#mail_fsync = optimized/mail_fsync=always/g' /etc/dovecot/conf.d/10-mail.conf

EXPOSE 25
EXPOSE 143
EXPOSE 993
#EXPOSE 995 USA POP

ENTRYPOINT cat /etc/hosts > /var/spool/postfix/etc/hosts && postfix start -v && chown -R vmail:vmail /home/vmail && service dovecot start && chown vmail:vmail /var/run/dovecot/auth-userdb && /bin/bash

#docker build -t smtp/imap:1 .
#docker run --name smtp-imap -p 25:25 -p 143:143 -p 993:993 smtp/imap