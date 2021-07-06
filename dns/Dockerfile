# Obtener imagen de debian
FROM debian:bullseye

# Actualizar e instalar bind9
RUN apt update && apt install -y bind9

# Se agregan los archivos
COPY named.conf.local /etc/bind/named.conf.local
COPY db.riguas.com /etc/bind/db.riguas.com
COPY db.torrejas.com /etc/bind/db.torrejas.com
COPY start-dns.sh /root/start-dns.sh

# Test de configuración de las zonas
RUN named-checkconf -z

# Bind9
EXPOSE 53/tcp 53/udp

ENTRYPOINT ["/bin/bash", "start-dns.sh"]

