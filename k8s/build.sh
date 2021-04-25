#!/bin/sh

if [ -n "$1" ]; then
  echo 'start building images'
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