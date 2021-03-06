dir=`dirname $0`
parentdir="$(dirname "$dir")"

echo "Exporting system variables..."
. $parentdir/conf/jaws-env.sh

echo $TACHYON_WAREHOUSE_PATH
echo $TACHYON_MASTER
echo $MESOS_NATIVE_LIBRARY
echo $JAVA_OPTS

echo "Deploying jaws..."
$dir/main-jaws.sh
