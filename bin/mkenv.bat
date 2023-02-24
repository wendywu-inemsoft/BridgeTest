set HOME=%CD:~0,2%%CD:~2,-4%
set LIBPATH=%HOME%\LIB
set ACPJARS=%LIBPATH%\modapi-6.0.0.0.250.jar;%LIBPATH%\acp-api-6.0.0.0.250.jar;%LIBPATH%\acpl-6.0.0.0.250.jar;%LIBPATH%\acpl-log4j-6.0.0.0.250.jar;%LIBPATH%\acp-modapi-impl-6.0.0.0.250.jar
set LOGJARS=%LIBPATH%\log4j-1.2.15.jar;%LIBPATH%\datedFileAppender-1.0.2.jar
set CLASSPATH=%ACPJARS%;%LOGJARS%;%HOME%\classes

set JAVAPATH=C:\jre1.6.0_27\bin
set JAVACPATH=C:\jdk1.6.0_27\bin
set PATH=%JAVAPATH%;%JAVACPATH%