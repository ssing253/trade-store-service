SETLOCAL

call setEnv.bat

set PROFILE=dev
echo PROFILE=%PROFILE%

set PORT=8442
echo PORT=%PORT%

mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=%PORT% -Dspring-boot.run.profiles=%PROFILE% -Dspring-boot.run.jvmArguments="-Xms256m -Xmx512m" 


ENDLOCAL

