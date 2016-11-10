# SoP API
An API exposing regulations and business rules to assist Department of Veterans Affairs (Australia) determine veteran compensation claims.

# Build Status 

|devtest|master|
|-------|----------|
| [![Build Status](https://travis-ci.org/govlawtech/dva-sop-api.svg?branch=devtest)](https://travis-ci.org/govlawtech/dva-sop-api) | [![Build Status](https://travis-ci.org/govlawtech/dva-sop-api.svg?branch=master)](https://travis-ci.org/govlawtech/dva-sop-api)  |

# Configuration Notes

Azure App Service Application Settings:

* Java Version: Java 8
* Java Minor version: Newest
* Web container: Newest Jetty 9.3
* App Settings:
    - Environment variable 'DEP_ENV' set to 'devtest', 'staging' or 'prod' as appropriate.
    - Deployment slot names: 'dvasopapi-devtest', 'dvasopapi-staging', 'dvasopapi' (production).
    - devsopapi-devtest automatically swaps to devsopapi-staging
    
| Environment | DEP_ENV | Deployment Slot |
|-------------|---------|-----------------|
| development | devtest | dvasopapi-devtest|
| staging     | prod    | dvasopapi-staging|
| production  | prod    | dvasopapi       |
    
