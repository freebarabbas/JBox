#!/bin/sh
echo "cat stub.sh JBox.jar > JBox && chmod +x JBox"
MYSELF=`which "$0" 2>/dev/null`
[ $? -gt 0 -a -f "$0" ] && MYSELF="./$0"
java=java
if test -n "$JAVA_HOME"; then
    java="$JAVA_HOME/bin/java"
fi
exec "$java" $java_args -jar -Xms4096m -Xmx8192m $MYSELF "$@"
exit 1
