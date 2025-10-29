#!/bin/bash

if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
fi

echo "----Iniciando docker do sonar (1/4)----"

docker compose up -d

echo ""
echo "----Aguardando 60s para inicializacao dos containers (2/4)----"
sleep 60

echo "----Executando script de setup do sonar e gerando token do projeto (3/4)----"
./sonar/setup-sonar.sh ${SONAR_CHANGE_PASSWORD:-true} ${SONAR_CREATE_PASSWORD:-true}

echo "----Executando analise de cobertura de codigo do projeto (4/4)----"
COMMAND_FILE="sonar/command-mvn.txt"
if [ ! -s "$COMMAND_FILE" ]; then
    echo "O arquivo $COMMAND_FILE está vazio ou não existe. Abortando."
    exit 1
fi

COMMAND=$(<"$COMMAND_FILE")
if [ -z "$COMMAND" ]; then
    echo "O comando no $COMMAND_FILE está vazio. Abortando."
    exit 1
fi

cd payment-gateway || { echo "Não foi possível acessar a pasta payment-gateway"; exit 1; }
eval "$COMMAND"
cd ..


echo "----Finalizado com sucesso (4/4)----"