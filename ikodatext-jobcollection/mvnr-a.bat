cd ..\ikoda-utils

call mvnr



cd ..\text

call mvn clean install

cd ..\ikodatext-jobcollection


call mvn clean install  -P assemble


rmdir ..\..\run_majors\packaged-resources /s
del ..\..\run_majors\launchjc.bat /F /Q
robocopy .\target\packaged-resources ..\..\run_majors\packaged-resources /X /E
copy .\launchjc.bat ..\..\run_majors\launchjc.bat 
copy .\target\ikodatext-jobcollection-0.0.1-SNAPSHOT.jar ..\..\run_majors\ikodatext-jobcollection-0.0.1-SNAPSHOT.jar

