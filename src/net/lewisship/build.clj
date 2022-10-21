(ns net.lewisship.build
  "Utilities for creating build commands.")

(defmacro requiring-invoke
  "Uses `requiring-resolve` to invoke a fully qualified (but not quoted name);

  e.g. `(requiring-invoke net.lewisship.build.jar/deploy-jar jar-params)`"
  [sym & params]
  (assert (qualified-symbol? sym))
  `((requiring-resolve '~sym) ~@params))

(defn create-jar
  "Creates a Jar file for the project under `target`.

  :project-name (qualified symbol, required)
  :version (string. required)
  :url (string) the project URL (aka, the home page)
  :src-dirs (coll of strings), defaults to [\"src\"]
  :resource-dirs (coll of strings), defaults to [\"resources\"]
  :class-dir (string) defaults to \"target/classes\"
  :jar-file (string) name of output jar file, defaults to \"target/<project-name>-<version>.jar\"

  Returns the name of the created output file."
  [options]
  (requiring-invoke net.lewisship.build.jar/create-jar options))


(defn deploy-jar
  "Deploys a Jar file created by [[create-jar]] to Clojars.

  :project-name (qualified symbol, required)
  :version (string)
  :jar-file (string) overrides default from project-name and version
  :class-dir (string) defaults to \"target/classes\"

  Requires environment variable CLOJARS_GPG_ID to be set.

  Will prompt for GPG passphrase.

  Returns nil."
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

  Returns nil."
  [options]
  (requiring-invoke net.lewisship.build.codox/generate options))
