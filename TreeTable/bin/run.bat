set HOME=%CD:~0,2%%CD:~2,-4%
set LIBPATH=%HOME%\LIB
set TREEJARS=%LIBPATH%\ew-looks.jar;%LIBPATH%\swingx-0.9.5.jar;%LIBPATH%\swingx-beaninfo-0.9.5.jar
set CLASSPATH=%TREEJARS%;%HOME%\bin

cd ..
C:\jdk1.6.0_10\bin\java com.objecttel.ClassOne.treetable.TreeTableTest