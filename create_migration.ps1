#!/usr/bin/env pwsh

# Script para criar uma nova migration Flyway usando o padrão normal (V<timestamp>__<mensagem>.sql)

# Pasta de migrations (edite se necessário)
$MIGRATIONS_DIR = "src/main/resources/db/migration"

# Obtém o timestamp atual no formato Unix epoch time
$TIMESTAMP = [int][double]::Parse((Get-Date -UFormat %s))

# Solicita título da migration ao usuário
$DESCRIPTION = Read-Host "Digite uma mensagem para a migration (ex: create_users_table)"

# Remove espaços e caracteres especiais desnecessários, converte para minúsculas e troca espaços/traços por _
$DESCRIPTION = $DESCRIPTION.ToLower() `
    -replace '\s+', '_' `
    -replace '-', '_' `
    -replace '[^a-z0-9_]', ''

# Nome do arquivo
$FILENAME = "V${TIMESTAMP}__${DESCRIPTION}.sql"

# Cria o diretório se não existir
if (-not (Test-Path $MIGRATIONS_DIR)) {
    New-Item -ItemType Directory -Path $MIGRATIONS_DIR -Force | Out-Null
}

# Cria arquivo na pasta de migrations
New-Item -ItemType File -Path "$MIGRATIONS_DIR/$FILENAME" -Force | Out-Null

Write-Host "Migration criada: $MIGRATIONS_DIR/$FILENAME" -ForegroundColor Green

