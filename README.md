# SoP API
An API exposing regulations and business rules to assist Department of Veterans' Affairs (Australia) to determine veteran compensation claims.

# Build Status 

|devtest|master|
|-------|----------|
| [![Build Status](https://travis-ci.org/govlawtech/dva-sop-api.svg?branch=devtest)](https://travis-ci.org/govlawtech/dva-sop-api) | [![Build Status](https://travis-ci.org/govlawtech/dva-sop-api.svg?branch=master)](https://travis-ci.org/govlawtech/dva-sop-api)  |

# Documentation
<<<<<<< HEAD

[Documentation](https://govlawtech.github.io/dva-sop-api/index.html)

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

# Notes on Processing Rules

* Where a veteran has a service history including multiple service branches (eg, Army and RAN), the standard of proof (Reasonable Hypothesis or Balance of Probabilities) is determined taking into account:
    - the entire service history, including multiple branches;
    - the rank they held in the last service branch in which they started before the condition started.

* The number of days of operational service in a given interval is the number of whole elapsed 24 hour periods, rounded up to the nearest day.  This takes account of time zones, daylight saving and leap seconds etc.

* Dates of operations in Service Declarations are assumed to be in the "Australia/ACT" IANA TZDB time zone.  The start time is assumed to be 12 AM on the date specified in the Declaration.  End dates of operations are assumed to be inclusive: the end time is assumed to be 12 midnight PM on the date specified.

* Commencement dates and dates of effect are assumed to be in the "Australia/ACT" IANA TZDB time zone, at 12AM.

# Notes on Automatic SoP Update

There are four cases where SoP changes are handled automatically:

1. A new SoP instrument, which not not repeal any existing instruments (e.g.: for a previously unrecognised condition).
1. An amendment to an existing SoP instrument.
1. A new SoP instrument which repeals one or more existing instruments.
1. Revocation of an existing SoP without any replacement.

## New SoP instruments for new conditions

For first case, notification of the new SoP comes from the email updates services of the Legislation Register.  An example notification is:

```
Amendment Statement of Principles concerning anxiety disorder No. 100 of 2016
Amendment Statement of Principles concerning anxiety disorder.
Item was published on 1/11/2016
https://www.legislation.gov.au/Details/F2016L01698
```

Following notification, the API retrieves the SoP from the Legislation Register and adds it to its database.

## Amended SoP instruments

For this case, we wait until the Legislation Register publishes an updated compilation for the instrument.  The API retrieves the latest compilation by polling the Legislation Register (using the '/Latest', see https://www.legislation.gov.au/Content/Linking).
This occurs for all SoPs in the database regularly, at least every 24 hours.  When the Legislation Register indicates the latest compilation has a different Register ID to the one held in the database, the API *replaces* the existing one.

## New SoP instrument repealing existing instrument

For SoP instruments already in its database, the API regularly polls the Legislation Register to check if they have been repealed, ceased or revoked.  When this has happened, the Register gives a pointer to the repealing instrument as well as the date of repeal.  The API retrieves this new instrument and loads it to its database as a replacement SoP.  It end dates the previous SoP.  The end date of the previous SoP is the date *one day before* the date of effect of the new SoP.

## Cease/Repeal without Replacement

This works similarly to the previous case, except that the repealing instrument is not a SoP in its own right.  If the API cannot identify the repealing instrument as a new SoP (the case above), it end dates the existing SoP in its database.  The end date is the date of repeal shown on the Legislation Register.



=======
>>>>>>> 4325f6de4ec500f7cf95bf9584a8be902866bd49

[Documentation](https://govlawtech.github.io/dva-sop-api/index.html)
