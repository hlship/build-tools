(ns net.lewisship.build
  "Utilities for creating build commands.")

(defmacro requiring-invoke
  "Uses `requiring-resolve` to invoke a fully qualified (but not quoted name);

  e.g. `(requiring-invoke net.lewisship.build.jar/create-jar jar-params)`"
  [sym & params]
  (assert (qualified-symbol? sym))
  `((requiring-resolve '~sym) ~@params))

(defn create-jar
  "Creates a Jar file for the project under `target`.

  :project-name (qualified symbol, required)
  :version (string. required)
  :scm (map, optional) Maven SCM data, usually just :url
  :src-dirs (coll of strings), defaults to [\"src\"]
  :resource-dirs (coll of strings), defaults to [\"resources\"]
  :class-dir (string) defaults to \"target/classes\"
  :jar-file (string) name of output jar file, defaults to \"target/<project-name>-<version>.jar\"

  Returns a map of options that can to be passed to deploy-jar."
  [options]
  (requiring-invoke net.lewisship.build.jar/create-jar options))


(defn deploy-jar
  "Signs and deploys a JAR artifact, created by [[create-jar]].

  :artifact-id (symbol, required) - artifact to deploy, e.g. 'org.example/my-project
  :version (string, required) - version of artifact to deploy, e.g., \"1.2.3-rc-1\"
  :jar-path (string, required) - path to the JAR file to be deployed
  :pom-path (string, required) - path to the POM file
  :sign-key-id (string, optional) - used to sign the artifacts
  :work-dir (string, optional) - directory to write temporary artifacts to
  (defaults to \"target\")

  The :pom-path is usually computed via clojure.tools.build.api/pom-path.

  If :sign-key-id is omitted, it is obtained from environment variable CLOJARS_GPG_ID; if
  that is not set, then a RuntimeException is thrown."
  [options]
  (requiring-invoke net.lewisship.build.jar/deploy-jar options))

(defn generate-codox
  "Generates Codox documentation for a project.

  :project-name (qualified symbol, required)
  :version (string, required)
  :codox-version (string) defaults to \"0.10.8\"
  :exclusions (coll of qualified symbols) dependencies of Codox to ignore when constructing class path
  :aliases (coll of keyword) aliases to enable when constructing class path
  :codox-config (map) merged with :codox/config from the deps.edn basis
  :output-path (string, optional) defaults to \"target/doc\"

  The :codox/config key in deps.edn provides defaults passed to codox; typically contains keys :description and :source-uri.

  Returns nil."
  [options]
  (requiring-invoke net.lewisship.build.codox/generate options))
