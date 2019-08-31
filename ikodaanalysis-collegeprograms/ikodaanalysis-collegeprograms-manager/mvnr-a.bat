cd ..\..\ikoda-utils

call mvnr



cd ..\text

call mvn clean install

cd ..\ikodaanalysis-collegeprograms\ikodaanalysis-collegeprograms-manager


call mvn clean install  -P assemble


rmdir ..\..\..\run_reddit\packaged-resources /s
del ..\..\..\run_reddit\launchcp.bat /F /Q
robocopy .\target\packaged-resources ..\..\..\run_reddit\packaged-resources /X /E
copy .\launchcp.bat ..\..\..\run_reddit\launchcp.bat 
copy .\target\ikodaanalysis-collegeprograms-manager-0.0.1-SNAPSHOT.jar ..\..\..\run_reddit\ikodaanalysis-collegeprograms-manager-0.0.1-SNAPSHOT.jar

