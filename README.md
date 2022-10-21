# io.github.hlship/build-tools

A tiny collection of command line tools for simple project deployment to Clojars and the generation
of Codox-based documentation.

The main namespace is `net.lewisship.build`.
This is an API namespace that using `requireing-resolve` to avoid immediate class loading.

You might find the `requiring-invoke` macro to be useful in your own builds.

## Usage:

Generally, you don't invoke the provided build functions directly, you define a project-specific
`build.clj` and make that the default namespace.

See the Lacinia [build.clj](https://github.com/walmartlabs/lacinia/blob/master/build.clj)
and [deps.edn](https://github.com/walmartlabs/lacinia/blob/master/deps.edn#L49) as examples.
