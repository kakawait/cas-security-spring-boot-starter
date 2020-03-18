#!/usr/bin/env bash

if [[ "$OSTYPE" == "darwin"* ]]; then
  sed() {
    gsed "$@"
  }
  date() {
    gdate "$@"
  }
fi

new_version="${1}"

if [[ -z "${new_version}" ]]; then
  echo "Missing argument: version"
  exit 1
fi

current_version="$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)"
echo "Current version is: ${current_version}"
echo "New version will be: ${new_version}"
echo
read -p "Are you sure? " -r
if [[ $REPLY =~ ^[Yy]$ ]]; then
  mvn versions:set "-DnewVersion=${new_version}" -DoldVersion=* -DgroupId=* -DartifactId=* -q -DforceStdout
  mvn -f cas-security-spring-boot-sample/pom.xml versions:set "-DnewVersion=${new_version}" -DoldVersion=* -DgroupId=* -DartifactId=* -q -DforceStdout
  mvn -f cas-security-spring-boot-sample/pom.xml versions:update-parent "-DparentVersion=${new_version}" -q -DforceStdout
  mvn -f spring-security-cas-extension/pom.xml versions:update-parent "-DparentVersion=${new_version}" -q -DforceStdout
  sed -i 's/\(cas-security-spring-boot-sample-\).*\.jar/\1'"${new_version}"'.jar/g' ./cas-security-spring-boot-sample/Dockerfile
  sed -i 's/\(image: cas-security-spring-boot-sample:\).*/\1'"${new_version}"'/g' ./cas-security-spring-boot-sample/docker-compose.yml
  sed -i 's/\(cas-security-spring-boot-starter%7C\).*%7Cjar/\1'"${new_version}"'%7Cjar/g' README.md
  sed -i 's/\(<version>\).*\(<\/version>\)/\1'"${new_version}"'\2/g' README.md
fi
