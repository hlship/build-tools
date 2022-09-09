# io.github.hlship/build-tools

A tiny collection of command line tools for simple project deployment to Clojars and the generation
of codox-based documentation.

The main namespace is `net.lewisship.build`.

You might find the `requiring-invoke` and `delegate` macros to be useful in your own builds.

## Usage

Generally, you don't invoke the provided build functions directly, you define a project-specific
`build.clj` and make that the default namespace.

See the Lacinia [build.clj](https://github.com/walmartlabs/lacinia/blob/master/build.clj)
and [deps.edn](https://github.com/walmartlabs/lacinia/blob/master/deps.edn#L49) as examples.



## create-jar

Creates a Jar file for the project under `target`.

Keys:
- :project-name - symbol
- :version - string
- :url - string; project URL, aka home page

## deploy-jar

Deploys a Jar file created by create-jar.

Keys: 
 - :project-name - symbol
 - :version - string

Requires CLOJARS_GPG_ID environment variable to be set
(along with CLOJARS_USERNAME and CLOJARS_PASSWORD).


## codox

Generates documentation using [Codox](https://github.com/weavejester/codox).

By default, the documentation is written to `target/doc`.

This is tricky, as it creates a new Basis that includes Codox as well as
your projects dependencies and runs that in a sub-process.

Additional Codox configuration can be provided in the project's `:codox/config` as a map;
for example, to specify the :description and :source-uri.

Keys:

- :project-name - symbol
- :version - string
- :aliases - seq of keywords, optional; used when building Codox classpath
- :codox-version - string, default is "0.10.8"
- :exclusions - exclusions to the `codox` dependency (when conflicts with the project)


