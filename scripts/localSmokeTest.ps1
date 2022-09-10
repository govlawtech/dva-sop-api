Invoke-WebRequest 'http://localhost:8080/app-2.0.0/getConnectionToService' -Method Post -Headers @{Accept = 'application/json'}  -InFile .\smokeTestRequest.json -OutFile result.json
