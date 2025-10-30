# Contributing to the WildFly Jakarta Expression Language API project

Thanks for your interest in this project.

## Overview

This is a WildFly-specific fork of the Eclipse EE4J Jakarta Expression Language [spec and API repository](https://github.com/jakartaee/expression-language).

The purpose of this project is to maintain a variant of the Jakarta Expression Language API binary that is tailored for use in a WildFly application server environment.

A key goal of this project is to minimize the differences between the API binaries produced by the project and those from Eclipse EE4J. Generally this means limiting changes to things specific to operation in a WildFly server. _Developers interested in making more broadly applicable changes are encouraged to contribute to [the Eclipse EE4J repository](https://github.com/jakartaee/expression-language) instead_.

We expect all contributors and users to follow our [Code of Conduct](https://github.com/wildfly/jboss-jakarta-el-api_spec#coc-ov-file) when communicating through project channels. These include, but are not limited to: chat, issues, code.

## Legal

All original contributions to this project are licensed under the
[Eclipse Public License - v 2.0](https://www.eclipse.org/legal/epl-v20.html),
or, if another license is specified as governing the file or directory being modified, such other license.
The EPL 2.0 license text is included verbatim in the [`LICENSE.md`](LICENSE.md) file.

All contributions are subject to the [Developer Certificate of Origin (DCO)](https://developercertificate.org/).
The DCO text is available verbatim in the [dco.txt](dco.txt) file in the root directory of the repository.

### Compliance with Laws and Regulations

All contributions must comply with applicable laws and regulations, including U.S. export control and sanctions restrictions.
For background, see the Linux Foundation’s guidance:
[Navigating Global Regulations and Open Source: US OFAC Sanctions](https://www.linuxfoundation.org/blog/navigating-global-regulations-and-open-source-us-ofac-sanctions).

## Issues

This project uses GitHub issues to manage issues. Issues can be found [here](https://github.com/wildfly/jboss-jakarta-el-api_spec/issues).

Historical issues can also be found in the [JBEE project in Red Hat JIRA](https://issues.redhat.com/projects/JBEE/summary), often associated with the [jboss-el-api JIRA component](https://issues.redhat.com/issues/?jql=project%20%3D%20JBEE%20AND%20component%20%3D%20jboss-el-api).

## Branches

The project maintains a branch per major/minor version of the Jakarta Expression Language specification for which WildFly needed an API jar.

Changes made to the branch for an earlier version of the spec must also be made to the branches for later versions, if the change is relevant.

The `master` branch is unmaintained, will likely be removed and can be ignored.

## Developer resources

The [Hacking on WildFly](https://docs.wildfly.org/38/Hacking_On_WildFly.html) document contains broadly useful information about contributing to projects in the WildFly family. (Note that it does contain a lot of information specific to the main WildFly server project and to WildFly Core.)

## Contact

The project developers can be reached in two ways:

* Via the mailing list - https://lists.jboss.org/mailman/listinfo/wildfly-dev
* On Zulip (recommended) - https://wildfly.zulipchat.com
