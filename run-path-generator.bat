
set cp=lib\cconv.jar
set cp=%cp%;lib\poly2tri.jar
set cp=%cp%;lib\triangulate.jar
set cp=%cp%;lib\core.jar
set cp=%cp%;classes

java ^
    -classpath %cp% ^
    org.remoteaquisition.path.PathGenerator data\boundary.txt

