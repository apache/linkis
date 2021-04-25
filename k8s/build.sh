#!/bin/sh

if [ -n "$1" ]; then
  echo 'start building images'

  cp -r linkis-engineconn-plugins/engineconn-plugins/spark/target/out/ linkis-engineconn-plugins/linkis-engineconn-plugin-framework/linkis-engineconn-plugin-server/plugins/
  cp -r linkis-engineconn-plugins/engineconn-plugins/hive/target/out/ linkis-engineconn-plugins/linkis-engineconn-plugin-framework/linkis-engineconn-plugin-server/plugins/
  cp -r linkis-engineconn-plugins/engineconn-plugins/python/target/out/ linkis-engineconn-plugins/linkis-engineconn-plugin-framework/linkis-engineconn-plugin-server/plugins/
  cp -r linkis-engineconn-plugins/engineconn-plugins/shell/target/out/ linkis-engineconn-plugins/linkis-engineconn-plugin-framework/linkis-engineconn-plugin-server/plugins/

  cat k8s/build.info | while read line; do
    {
      key=$(echo $line | awk '{print $1}')
      folder=$(echo $line | awk '{print $2}')
      imageName="nm.hub.com/luban/${key}:1.0"
      echo "build image: ${imageName}"

      cp -r assembly/public-module/target/out/lib/*  ${folder}/target/out/lib/
      docker build -t $imageName -f ${folder}/Dockerfile ${folder}
      docker push ${imageName}
    }
  done
  echo 'build finished'
else
  echo "Usage: sh build.sh {versionNumber}"
fi