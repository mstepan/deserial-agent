# Deserialization agent.
Allows to deserilize only classes from well known white list.

# Usage

`java -javaagent:<path-to-file>/deserial-agent-2.0.0-jar-with-dependencies.jar -Dwhite.list=<path-to-file>/white_list.txt <your-application>`