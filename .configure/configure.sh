#!/bin/sh

cd $(dirname $0)

mkdir -p ../.settings

cp -n org.eclipse.jdt.core.prefs ../.settings


if [ -z "$PREFIX" ]; then
  PREFIX=/usr/local
fi

echo $PREFIX > prefix.var

cat > routeplanner <<EOF
#!/bin/sh

java -Xmx6g -jar $PREFIX/lib/routeplanner/routeplanner.jar "\$@"

EOF

chmod +x routeplanner


echo "Project configured with prefix $PREFIX"

