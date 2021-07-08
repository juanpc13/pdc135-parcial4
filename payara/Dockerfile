# Obtener imagen de debian
FROM debian:bullseye

RUN apt update && apt install -y gettext wget nano

# directorio
WORKDIR /root
ENV HOME="/root"

# de descarga y export el JDK 8
RUN wget -O jdk1.8.0_291.tar.gz https://javadl.oracle.com/webapps/download/GetFile/1.8.0_291-b10/d7fc238d0cbf4b0dac67be84580cfb4b/linux-i586/jdk-8u291-linux-x64.tar.gz
# Se Descarga maven
RUN wget -O apache-maven-3.8.1.tar.gz  https://downloads.apache.org/maven/maven-3/3.8.1/binaries/apache-maven-3.8.1-bin.tar.gz 
# Se Descarga el Jar de Payara Micro
RUN wget -O payara-micro.jar https://s3-eu-west-1.amazonaws.com/payara.fish/Payara+Downloads/5.2021.4/payara-micro-5.2021.4.jar

# Se extraen
RUN tar -xf jdk1.8.0_291.tar.gz
RUN tar -xf apache-maven-3.8.1.tar.gz

# Crean paths de los binarios y homesApps
ENV JAVA_HOME="${HOME}/jdk1.8.0_291"
ENV PATH="${JAVA_HOME}/bin:${PATH}"
ENV MAVEN_HOME="${HOME}/apache-maven-3.8.1"
ENV PATH="${MAVEN_HOME}/bin:${PATH}"

# Se implementa el codigo fuente y propiedades
COPY adm/ $HOME/adm/
# Compilar el proyecto
RUN $MAVEN_HOME/bin/mvn clean install -f $HOME/adm
# Se mueve de carpeta al Home
RUN mv $HOME/adm/target/adm-1.0-SNAPSHOT.war adm.war

# Scripts y propiedades
COPY adm.properties $HOME/adm.properties
COPY proto.sh $HOME/proto.sh
RUN chmod +x $HOME"/proto.sh"

# Delimitardor del archivo al sustituir
ENV UID="\$UID"
ENV CN="\$CN"

#ENTRYPOINT ["/bin/bash -c"]
CMD ["./proto.sh"]
