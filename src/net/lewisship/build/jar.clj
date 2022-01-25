(ns net.lewisship.build.jar
  "Utilities to build a Jar and deploy it to Clojars."
  (:require [clojure.tools.build.api :as b]
            [deps-deploy.deps-deploy :as d]))

(defn ^:private jar-file
  [project-name version]
  (format "target/%s-%s.jar"
          (name project-name)
          version))

(def ^:private class-dir
  "Directory to which all sources and resources are copied, this becomes
  the primary content of the jar file."
  "target/classes")

(defn create-jar
  "Creates a Jar file for the project under `target`.

  :url is the project URL (aka, the home page)."
  [{:keys [project-name version url]}]
  (let [basis (b/create-basis)
        output-file (jar-file project-name version)]
    (b/write-pom {:class-dir class-dir
                  :lib project-name
                  :version version
                  :basis basis
                  :scm {:url url}
                  :src-dirs ["src"]
                  :resource-dirs ["resources"]})
    (b/copy-dir {:src-dirs ["src" "resources"]
                 :target-dir class-dir})
    (b/jar {:class-dir class-dir
            :jar-file output-file})
    (println "Created:" output-file)))

(defn deploy-jar
  "Deploys a Jar file created by [[create-jar]] to Clojars.

  Requires environment variable CLOJARS_GPG_ID to be set.

  Will prompt for GPG passphrase."
  [{:keys [project-name version]}]
  (d/deploy {:installer :remote
             :artifact (jar-file project-name version)
             :pom-file (b/pom-path {:lib project-name
                                    :class-dir class-dir})
             :sign-releases? true
             :sign-key-id (or (System/getenv "CLOJARS_GPG_ID")
                              (throw (RuntimeException. "CLOJARS_GPG_ID environment variable not set")))}))