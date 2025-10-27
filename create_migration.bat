@echo off
REM Script para criar uma nova migration Flyway usando o padrão normal (V<timestamp>__<mensagem>.sql)

REM Pasta de migrations (edite se necessário)
SET MIGRATIONS_DIR=src\main\resources\db\migration

REM Obtém o timestamp atual no formato Unix epoch time
for /f %%i in ('powershell -Command "[int][double]::Parse((Get-Date -UFormat %%s))"') do set TIMESTAMP=%%i

REM Solicita título da migration ao usuário
set /p DESCRIPTION="Digite uma mensagem para a migration (ex: create_users_table): "

REM Transforma em snake case e minúsculas
REM (limitações do batch - conversão básica)
set DESCRIPTION=%DESCRIPTION: =_%
set DESCRIPTION=%DESCRIPTION:-=_%
for %%L in (A B C D E F G H I J K L M N O P Q R S T U V W X Y Z) do call set DESCRIPTION=%%DESCRIPTION:%%L=%%L%%

REM Nome do arquivo
SET FILENAME=V%TIMESTAMP%__%DESCRIPTION%.sql

REM Cria arquivo na pasta de migrations
type nul > "%MIGRATIONS_DIR%\%FILENAME%"

echo Migration criada: %MIGRATIONS_DIR%\%FILENAME%
pause

