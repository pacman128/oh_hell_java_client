# Oh Hell Client

This repo holds the code for a Java client to my Oh Hell server.

It was developed using [Intellij](https://www.jetbrains.com/idea/).

## Build Instructions

Using _maven_, in the root directory, run:
```shell
mvn package
```
This will create two jar files in the _target_ directory:
_oh_hell_java_client-1.0.jar_ and
_oh_hell_java_client-1.0-jar-with-dependencies.jar_

## Install Instructions

### Windows

From the Oracle [download page](https://www.oracle.com/java/technologies/downloads/),
download the Windows JDK x64 installer. The latest JRE will _NOT_ work.

Run the installer.

The JDK will be installed to **C:\Program Files\java\SDK-_version_**.

To run the client, use the following command:
<pre>
<i>path_to_JDK</i>\bin\java -jar <i>path_to_jar</i>
</pre>
