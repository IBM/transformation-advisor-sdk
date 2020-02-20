## Contributing In General
Our project welcomes external contributions. If you have an itch, please feel
free to scratch it.

To contribute code or documentation, please submit a [pull request](https://github.com/IBM/transformation-advisor-sdk/pulls).

A good way to familiarize yourself with the codebase and contribution process is
to look for and tackle low-hanging fruit in the [issue tracker](https://github.com/IBM/transformation-advisor-sdk/issues).
Before embarking on a more ambitious contribution, please quickly [get in touch](#communication) with us.

**Note: We appreciate your effort, and want to avoid a situation where a contribution
requires extensive rework (by you or by us), sits in backlog for a long time, or
cannot be accepted at all!**

### Proposing new features

If you would like to implement a new feature, please [raise an issue](https://github.com/IBM/transformation-advisor-sdk/issues)
before sending a pull request so the feature can be discussed. This is to avoid
you wasting your valuable time working on a feature that the project developers
are not interested in accepting into the code base.

### Fixing bugs

If you would like to fix a bug, please [raise an issue](https://github.com/IBM/transformation-advisor-sdk/issues) before sending a
pull request so it can be tracked.

### Merge approval

The project maintainers use LGTM (Looks Good To Me) in comments on the code
review to indicate acceptance. A pull request need 2 code review approvals and all the build checks passed before the merge. 
When create pull request,  you can select **ta-sdk-admin** as the reviewer.

For a list of the maintainers, see the [MAINTAINERS.md](MAINTAINERS.md) page.

## Legal

Each source file must include a license header for the Apache
Software License 2.0. Using the SPDX format is the simplest approach.
e.g.

```
/*
Copyright <holder> All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
```

We have tried to make it as easy as possible to make contributions. This
applies to how we handle the legal aspects of contribution. We use the
same approach - the [Developer's Certificate of Origin 1.1 (DCO)](https://developercertificate.org/) - that the Linux® Kernel [community](https://elinux.org/Developer_Certificate_Of_Origin)
uses to manage code contributions.

We simply ask that when submitting a patch for review, the developer
must include a sign-off statement in the commit message.

Here is an example Signed-off-by line, which indicates that the
submitter accepts the DCO:

```
Signed-off-by: John Doe <john.doe@example.com>
```

You can include this automatically when you commit a change to your
local git repository using the following command:

```
git commit -s
```

## Communication
**_FIXME_** Please feel free to connect with us on our [Slack channel](link).

## Setup
Install Java and Maven project on your development machine.

The following instructions are for using IntelliJ as development tool.
By default,  Maven plugin should already be installed.
1.  Clone this project to IntelliJ
2.  Set the project SDK for the cloned migration projet
     + Click File -> Project Structure...,
     + Select a Java SDK from the Project SDK drop down list,
     + Click Apply,  click OK button.
3.  Build this project
     + Click the Maven Projects tab on the right side to open Maven plugin
     + Click the Refresh button to Reimport All Maven Projects
     + Expend ta-sdk -> Lifecycle
     + Select Clean,  click the green triangle button to run this task.
     + Select Install, click the green triangle button to build the project.

## Testing
Build this project, and invoke the run command to the sample plug-in.

You should see following messages:

```
# mvn clean install
# java -jar target/ta-sdk-sample-0.5.2.jar sample run test test
Command 'run' completed successfully.


```

## Branch naming convention

issue-#<number>

## Commit message convention

issue #<number> - short description

long description

