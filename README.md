APIParser
=========

Parses Java *source code* to extract the public API.
The public API is then output on the console. This allows comparing multiple APIs to find differences.

"mvn clean install" will create a target\uber-APIParser-X.Y.Z.jar jar that you can call like this:

java -jar uber-APIParser-X.Y.Z.jar <src-dir-1> <src-dir-2> ...

Licensed under Apache.
