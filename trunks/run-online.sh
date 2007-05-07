export MAVEN2_REPO=$HOME/.m2/repository
$JAVA_HOME/bin/java  -Dpomstrap.maven2.repository=http://open-im.net/m2/repository/ -jar pomstrap-1.0.7.jar net.java.dev.openim:openim-plexus-server:1.5 net.java.dev.openim.App:launch
