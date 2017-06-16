#!/bin/bash
if [ "$TRAVIS_BRANCH" == "dev" ]; then
  curl --ftp-create-dirs -T "app/deploy/webapps/ROOT.war" -u "$DEV_USER:$DEV_PASSWORD" ftp://waws-prod-sy3-011.ftp.azurewebsites.windows.net/site/wwwroot/
  curl --ftp-create-dirs -T "app/deploy/webapps/api-docs/yaml/dva-sop-api.yaml" -u "$DEV_USER:$DEV_PASSWORD" ftp://waws-prod-sy3-011.ftp.azurewebsites.windows.net/site/wwwroot/api-docs/yaml/
elif [ "$TRAVIS_BRANCH" == "devtest" ]; then
  curl --ftp-create-dirs -T "app/deploy/webapps/ROOT.war" -u "$DEVTEST_USER:$DEVTEST_PASSWORD" ftp://waws-prod-sy3-011.ftp.azurewebsites.windows.net/site/wwwroot/
  curl --ftp-create-dirs -T "app/deploy/webapps/api-docs/yaml/dva-sop-api.yaml" -u "$DEVTEST_USER:$DEVTEST_PASSWORD" ftp://waws-prod-sy3-011.ftp.azurewebsites.windows.net/site/wwwroot/api-docs/yaml/
elif [ "$TRAVIS_BRANCH" == "master" ]; then
  curl --ftp-create-dirs -T "app/deploy/webapps/ROOT.war" -u "$STAGING_USER:$STAGING_PASSWORD" ftp://waws-prod-sy3-011.ftp.azurewebsites.windows.net/site/wwwroot/
  curl --ftp-create-dirs -T "app/deploy/webapps/api-docs/yaml/dva-sop-api.yaml" -u "$STAGING_USER:$STAGING_PASSWORD" ftp://waws-prod-sy3-011.ftp.azurewebsites.windows.net/site/wwwroot/api-docs/yaml/
fi