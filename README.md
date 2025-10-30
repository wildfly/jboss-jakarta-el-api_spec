# WildFly Jakarta Expression Language API

This is a WildFly-specific fork of the Eclipse EE4J Jakarta Expression Language [spec and API repository](https://github.com/jakartaee/expression-language).

The purpose of this project is to maintain a variant of the Jakarta Expression Language API binary that is tailored for use in a WildFly application server environment.

This fork is limited to the API portion of the Eclipse EE4J repository. Specification and TCK content in the Eclipse EE4J repository has been removed.

[Online JavaDoc](https://javadoc.io/doc/jakarta.el/jakarta.el-api/)

Building
--------

Jakarta Authentication can be built by executing the following from the project root:

``mvn clean package``

The API jar can then be found in /target.

Releases of this project should be performed from the api subfolder, which will only release the API jar.

Making Changes
--------------

To make changes, fork this repository, make your changes, and submit a pull request.

About Jakarta Expression Language
-------------

Jakarta Expression Language defines an expression language for Java applications.
