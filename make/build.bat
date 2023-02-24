cd ..\bin
call mkenv

copy ..\src\log4j.properties ..\classes\log4j.properties

cd ..


javac -sourcepath src -d classes src\com\objecttel\ClassOne\BridgeTest\*.java

pause