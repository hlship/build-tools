# 0.10 -- 4 Jan 2024

Can now specify a license in the :scm key, e.g., `{:scm {:license :esl}}`.
Builtin support for :esl (Eclipse Public License) and :asl (Apache Licence, 2.0). Can also specify
map with :name and :url.

# 0.9 -- 30 Mar 2023

Can now specify :net.lewisship.build.scm map in project's deps.edn.

deploy-jar: new :sign-artifacts? option

jar: Adds the project version as the SCM tag


# 0.8 -- 27 Jan 2023

Added optional :output-path option to the generate-codox command.

# 0.7 -- 21 Oct 2022

Removed the `net.lewisship.build/delegate` macro, as it can't copy over the very useful docstring.

Added `net.lewisship.build.versions` with opinionated support for parsing and
manipulating version numbers.
