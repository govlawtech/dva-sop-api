# SoP API
An API exposing regulations and business rules to assist Department of Veterans' Affairs (Australia) to determine veteran compensation claims.

# Build Status 

|devtest|master|
|-------|----------|
| [![Build Status](https://travis-ci.org/govlawtech/dva-sop-api.svg?branch=devtest)](https://travis-ci.org/govlawtech/dva-sop-api) | [![Build Status](https://travis-ci.org/govlawtech/dva-sop-api.svg?branch=master)](https://travis-ci.org/govlawtech/dva-sop-api)  |

# Specifications

[List of SoPs](docs/sops.csv)

# Configuration Notes

Azure App Service Application Settings:

* Java Version: Java 8
* Java Minor version: Newest
* Web container: Newest Jetty 9.3
* App Settings:
    - Environment variable 'DEP_ENV' set to 'devtest', 'staging' or 'prod' as appropriate.
    - Deployment slot names: 'dvasopapi-devtest', 'dvasopapi-staging', 'dvasopapi' (production).
* web.config file in the repository root enforces redirection to https:// endpoint.
* CORS enabled via Azure management interface.
* DEP_ENV environment setting set via Azure management interface as follows: 

| Environment | DEP_ENV | Deployment Slot | Git Branch |
|-------------|---------|-----------------|------------|
| development | devtest | dvasopapi-devtest| devtest   |
| staging     | prod    | dvasopapi-staging| master    |
| production  | prod    | dvasopapi       | master     |
* No handler mappings in Azure management interface
* Virtual Applications and Directories: '/','site\wwwroot',Application.

# Deployment Instructions

To deploy to the devtest and staging environments, deploy either via FTP or source control as described below.  

To deploy to production, swap the Azure deployment slots via the Azure management interface.

* Target: 'dvasopapi'
* Source: 'dvasopapi-staging'

## FTP Deploy
1. Build .war using Gradle 'war' task.
1. Rename the .war to 'ROOT.war'.
1. FTP the war to '/site/wwwroot/webapps' folder for the relevant Azure Deployment Slot. 
    - Configure the FTP user name and password via the Azure management interface.
    - The user name is prefaced with the deployment slot name. For example, 'dvasopapi_devtest\yourusername'.  The password is set 'Deployment Credentials' in the Azure management interface.

## Source Control Deploy

1. Build the .war using the Gradle 'copyWar' task.  This builds the war and copies it to the 'webapps' directory.
1. Git commit and push.

# Development Environment Setup

* To run integration tests for Azure Storage, download and install the Azure Storage Emulator and (optionally) the Azure Storage Explorer.  These run on Windows only.
