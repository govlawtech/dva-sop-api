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
| devtest | devtest | dvasopapi-devtest| devtest   |
| staging     | prod    | dvasopapi-staging| master    |
| production  | prod    | dvasopapi       | master     |


* If the JVM property ```DEV_ENV``` is set to 'devtestlocal', this indicates the app is running on a local development machine.  This means the local Azure store emulator is used.

* No handler mappings in Azure management interface
* Virtual Applications and Directories: '/','site\wwwroot',Application.

## Required Environment Variable Settings

These environment variables need to be set for each deployment slot.

| Envorinment Variable Name | Value | Example |
|---------------------------|-------|---------|
| AZURE_STORAGE_CONNECTION_STRING | The connection string including account name and key. | DefaultEndpointsProtocol=https;AccountName=myAccount;AccountKey=myKey; |

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

# Case Summary

[Case summary template](src/main/resources/docs/Case Summary Template.docx)

[Generated case summary example] (src/test/resources/Case Summary.docx)

# Notes on Operation Names

Operation names are expected to be exactly as they appear in the relevant Service Determinations.

* Note the em-dash (U+2014) in the name "Enduring Freedom&#151;Afghanistan"
* The Operation Name for operation with description 'ADF contribution to the NATO no-fly-zone and maritime enforcement operation against Libya' is ```NNMEOAL```.  (The Service Determination omits a name, so we made one up to identify the operation.)

# Notes of Variations from Spec

* The array label for the array of legislative instrument ID's in the example response to the Get Operations query is ```registerIDs``` rather than ```legislativeInstrumentIds```, to be consistent with the terminology on Legislation.gov.au and elsewhere.

* The label shown as ```registeredId``` in the example response for the Get Sop Factors query is actually ```registerId```.
