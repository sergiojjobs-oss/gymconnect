@echo off
set MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.6
set PATH=%MAVEN_HOME%\bin;%PATH%
call mvn %*
