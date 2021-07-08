#!/bin/bash
envsubst < $HOME/adm.properties | tee $HOME/adm.properties
java -jar payara-micro.jar --deploy adm.war
