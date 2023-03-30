# 0.9 -- UNRELEASED

Can now specify :net.lewisship.build.scm map in project's deps.edn.

deploy-jar: new :sign-artifacts? option

jar: Adds the project version as the SCM tag


# 0.8 -- 27 Jan 2023

Added optional :output-path option to the generate-codox command.

# 0.7 -- 21 Oct 2022

Removed the `net.lewisship.build/delegate` macro, as it can't copy over the very useful docstring.

Added `net.lewisship.build.versions` with opinionated support for parsing and
manipulating version numbers.
