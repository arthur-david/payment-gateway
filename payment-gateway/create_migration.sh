#!/bin/bash

# Script para criar uma nova migration Flyway usando o padrão normal (V<timestamp>__<mensagem>.sql)

# Pasta de migrations (edite se necessário)
MIGRATIONS_DIR="src/main/resources/db/migration"

# Obtém o timestamp atual no formato Unix epoch time
TIMESTAMP=$(date +%s)

# Solicita título da migration ao usuário e transforma em snake case e sem espaços
read -p "Digite uma mensagem para a migration (ex: create_users_table): " DESCRIPTION
# Remove espaços e caracteres especiais desnecessários, converte para minúsculas e troca espaços/traços por _
DESCRIPTION=$(echo "$DESCRIPTION" | tr '[:upper:]' '[:lower:]' | tr ' ' '_' | tr '-' '_' | tr -cd '[:alnum:]_')

# Nome do arquivo
FILENAME="V${TIMESTAMP}__${DESCRIPTION}.sql"

# Cria arquivo na pasta de migrations
touch "${MIGRATIONS_DIR}/${FILENAME}"

echo "Migration criada: ${MIGRATIONS_DIR}/${FILENAME}"
