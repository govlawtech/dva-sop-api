java -jar $Env:JETTY_HOME/start.jar --add-module=server,http,deploy
cp ..\app\deploy\webapps\ROOT.war webapps
java -jar $Env:JETTY_HOME/start.jar 