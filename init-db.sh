#!/bin/bash
set -e

# Cria o banco de dados para o SonarQube
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE sonarqube;
    GRANT ALL PRIVILEGES ON DATABASE sonarqube TO $POSTGRES_USER;
EOSQL

echo "Banco de dados 'sonarqube' criado com sucesso!"

