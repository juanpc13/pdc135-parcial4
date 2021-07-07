# Obtener imagen de debian
FROM payara/server-full:latest

# Actualizar e instalar wget
USER root
RUN apt update && apt install -y wget
USER payara

# Se instala maven
RUN wget https://downloads.apache.org/maven/maven-3/3.8.1/binaries/apache-maven-3.8.1-bin.tar.gz
RUN tar -xvf apache-maven-3.8.1-bin.tar.gz

# Se implementa el codigo fuente
COPY --chown=payara adm/ $HOME_DIR/adm/

# Compilar el proyecto
RUN $HOME_DIR/apache-maven-3.8.1/bin/mvn clean install -f $HOME_DIR/adm
# Se mueve de carpeta
RUN mv $HOME_DIR/adm/target/adm-1.0-SNAPSHOT.war $HOME_DIR/deployments/adm.war

ENTRYPOINT ["/tini","--"]
CMD ["/bin/sh","-c","\"${SCRIPT_DIR}/entrypoint.sh\""]

