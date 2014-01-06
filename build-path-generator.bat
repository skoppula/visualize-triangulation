
set JAVA_HOME=c:\Apps\Java\jdk1.6.0_25
set PATH=%PATH%;c:\Apps\Java\jdk1.6.0_25\bin
set PG_HOME=c:\home\work\java\workspace\PathGenerator

javac ^
    -classpath  %PG_HOME%\lib\cconv.jar;%PG_HOME%\lib\poly2tri.jar;%PG_HOME%\lib\triangulate.jar;%PG_HOME%\lib\core.jar ^
    -sourcepath %PG_HOME%\src ^
    -d          %PG_HOME%\classes %PG_HOME%\src\org\remoteaquisition\path\PathGenerator.java

java ^
    -classpath  %PG_HOME%\lib\cconv.jar;%PG_HOME%\lib\poly2tri.jar;%PG_HOME%\lib\triangulate.jar;%PG_HOME%\lib\core.jar;%PG_HOME%\classes ^
    org.remoteaquisition.path.PathGenerator data\boundary.txt

