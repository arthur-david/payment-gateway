#!/bin/bash

login="admin"
oldPassword="admin"
password="Sonar@123456"
projectName="Payment Gateway"
projectKey="payment-gateway"
projectTokenName="payment-gateway-token"
hostUrl="http://localhost:9000"

if [ "${SONAR_CHANGE_PASSWORD}" = "true" ]; then
  echo ""
  echo "----Alterando senha do usuario padrao (3/4)----"

  curl -u "${login}:${oldPassword}" -X POST \
    "$hostUrl/api/users/change_password" \
    -d "login=$login&previousPassword=$oldPassword&password=$password"
fi

if [ "${SONAR_CREATE_PASSWORD}" = "true" ]; then
  echo ""
  echo "----Criando projeto (3/4)----"

  curl -u "${login}:${password}" -X POST \
    "$hostUrl/api/projects/create" \
    -d "mainBranch=production&name=$projectName&newCodeDefinitionType=PREVIOUS_VERSION&project=$projectKey"
fi

sleep 5

echo ""
echo "----Gerando token do projeto (3/4)----"

response=$(curl -s -u "${login}:${password}" -X POST \
  "$hostUrl/api/user_tokens/generate" \
  -d "name=$projectTokenName&projectKey=$projectKey&type=PROJECT_ANALYSIS_TOKEN")

# Extrai o token do JSON
token=$(echo "$response" | grep -o '"token":"[^"]*"' | sed 's/"token":"//;s/"//')

echo ""
echo "----Salvando token do projeto em token.txt (3/4)----"

scriptDir=$(dirname "$(readlink -f "$0")")
echo "$token" > "$scriptDir/token.txt"

echo ""
echo "----Gerando comando mvn e salvando em command-mvn.txt (3/4)----"

commandMvn="mvn clean verify sonar:sonar -Dsonar.projectKey=$projectKey -Dsonar.projectName=\"$projectName\" -Dsonar.host.url=$hostUrl -Dsonar.token=$token"
echo "$commandMvn" > "$scriptDir/command-mvn.txt"