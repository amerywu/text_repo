
echo mvn
call mvn clean install
call mvn javadoc:javadoc
SET /P _success= Build successful?
IF "%_success%"=="y" GOTO :run

GOTO :end

:run

echo copying docs

xcopy C:\Users\jake\__workspace\ikoda-utils\target\site\apidocs C:\Users\jake\__workspace\text-repo\ikoda-utils\docs\javadoc /S /E /F /R /Y /I


:end