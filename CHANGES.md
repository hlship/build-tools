# 0.12.1 -- 7 Aug 2025

Revert to version 0.2.1 of slipset/deps-deploy.

# 0.12.0 -- 7 Aug 2025

Added new :aliases option to `create-jar`; optional seq of aliases to use when creating the basis.

Added an `install-jar` function that takes the results from `create-jar`.
 
# 0.11.0 -- 27 Mar 2025

A version string may now be 'alpha' (as well a 'beta' or 'rc').

# 0.10.2 -- 27 Mar 2024

Include :basis and :class-dir in the result from `create-jar`.

# 0.10.1 -- 8 Feb 2024

Fix bug in license abbreviation.

# 0.10 -- 4 Jan 2024

Can now specify a license in the :scm key, e.g., `{:scm {:license :esl}}`.
Builtin support for :esl (Eclipse Public License) and :asl (Apache Licence, 2.0). Can also specify
map with :name and :url.

# 0.9 -- 30 Mar 2023

Can now specify :net.lewisship.build/scm map in project's deps.edn.

deploy-jar: new :sign-artifacts? option

jar: Adds the project version as the SCM tag


# 0.8 -- 27 Jan 2023

Added optional :output-path option to the generate-codox command.

# 0.7 -- 21 Oct 2022

Removed the `net.lewisship.build/delegate` macro, as it can't copy over the very useful docstring.

Added `net.lewisship.build.versions` with opinionated support for parsing and
manipulating version numbers.
