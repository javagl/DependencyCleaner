# DependencyCleaner

Utility for cleaning up Maven dependencies

![DependencyCleaner001.png](/screenshots/DependencyCleaner001.png)

A utility that I created after reading the question about how to
[Avoid Corrupted Jars ( Invalid LOC Header ) when using Maven](https://stackoverflow.com/q/52741518/3182664) on stack overflow.

Clone and compile with

    mvn clean package assembly:single
    
to write it as a standalone application into the `target` directory.

It is not tested thoroughly, and may or may not work at all for you.
The functionality should really be implemented as some sort of
validation step of the [Maven Dependency Plugin](https://maven.apache.org/plugins/maven-dependency-plugin/).
And maybe it already is. I did not check that. I actually 
wanted to have a look at how to programmatically detect
Maven dependencies...

