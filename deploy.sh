#!/bin/bash
if [ "$TRAVIS_BRANCH" == "dev" ]; then
  ls app/deploy/webapps
  echo $DEV_USER
  curl --ftp-create-dirs -T "app/deploy/webapps/ROOT.war" -u "$DEV_USER:$DEV_PASSWORD" ftp://waws-prod-sy3-011.ftp.azurewebsites.windows.net/site/wwwroot
fi
