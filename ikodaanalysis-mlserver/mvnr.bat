SET /P _go= Build springboot mlserver and run on Spark?
IF "%_go%"=="y" GOTO :build

GOTO :end

:build
echo mvn
call mvn install

SET /P _success= Build successful?
IF "%_success%"=="y" GOTO :run

GOTO :end

:run

echo copying assembly jar
copy   C:\Users\jake\__workspace\ikodaanalysis-mlserver\target\ikodaanalysis-mlserver-0.1.0-shaded.jar C:\Users\jake\_servers\spark-2.2.0-bin-hadoop2.7\ikoda\ikodaanalysis-mlserver-0.1.0.jar
copy   C:\Users\jake\__workspace\ikodaanalysis-mlserver\mlserver.bat C:\Users\jake\_servers\spark-2.2.0-bin-hadoop2.7\mlserver.bat


echo calling mlserver.bat
cd C:\Users\jake\_servers\spark-2.2.0-bin-hadoop2.7

call mlserver.bat
cd C:\Users\jake\__workspace\ikodaanalysis-mlserver
:end