@echo off
REM 
set jar=DirectoryCleaner-1.0.jar

REM point to the java install directory or leave blank and rely on windows path
set jre_=
set path=%java_home%\jre\bin;%java_home%\bin;%path%

REM Capture Timestamp
set dt=%DATE:~0,4%%DATE:~5,2%%DATE:~8,2%_%TIME:~0,2%%TIME:~3,2%%TIME:~6,2%
set dt=%dt: =0%


java -jar %jar% -age 0 -list -match .xml -source c:\temp
java -jar %jar% -age 0 -list -match .txt -source c:\temp


