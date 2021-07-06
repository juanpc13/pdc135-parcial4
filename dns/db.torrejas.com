; Datos Generales de la ZONA
$TTL    86400

; Record Inicio de la Autoridad de la ZONA
@       IN      SOA     ns1.protocolos.com. jcpleitez.protocolos.com. (
                              1         ; Serial
                         604800         ; Refresh
                          86400         ; Retry
                        2419200         ; Expire
                          86400 )       ; Negative Cache TTL
;

; Name Servers para el dominio
@       IN      NS      ns1.protocolos.com.

; Los registros para direcciones
ns1     IN      A       35.224.79.36
www     IN      A       192.168.255.6
ldap    IN      A       192.168.255.6
