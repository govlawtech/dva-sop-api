# SoP API
An API exposing regulations and business rules to assist Department of Veterans Affairs (Australia) determine veteran compensation claims.

# Build Status

|devtest|staging|production|
|-------|-------|----------|
| [![Build Status](https://travis-ci.org/govlawtech/dva-sop-api.svg?branch=devtest)](https://travis-ci.org/govlawtech/dva-sop-api) |  [![Build Status](https://travis-ci.org/govlawtech/dva-sop-api.svg?branch=staging)](https://travis-ci.org/govlawtech/dva-sop-api)  |[![Build Status](https://travis-ci.org/govlawtech/dva-sop-api.svg?branch=master)](https://travis-ci.org/govlawtech/dva-sop-api)  |

# Configuration Notes

Azure App Service Application Settings:

* Java Version: Java 8
* Java Minor version: Newest
* Web container: Newest Jetty 9.3
* App Settings:
    - Environment variable 'DEP_ENV' set to 'devtest', 'staging' or 'production' as appropriate.
    
