(ns net.lewisship.build.jar
  "Utilities to build a Jar and deploy it to Clojars."
  (:require [clojure.tools.build.api :as b]
            [deps-deploy.deps-deploy :as d]
            [deps-deploy.gpg :as gpg]
            [cemerick.pomegranate.aether :as aether]))

(System/setProperty "aether.checksums.forSignature" "true")

(defn ^:private default-jar-file
  [project-name version]
  (format "target/%s-%s.jar"
          (name project-name)
          version))

(defn create-jar
  [options]
  (let [{:keys [project-name version src-dirs resource-dirs class-dir jar-file scm]
         :or {src-dirs ["src"]
              resource-dirs ["resources"]}} options
        basis (b/create-basis)
        src-dirs' (or src-dirs ["src"])
        resource-dirs' (or resource-dirs ["resources"])
        output-file (or jar-file
                        (default-jar-file project-name version))
        class-dir' (or class-dir "target/classes")]
    (b/write-pom {:class-dir class-dir'
                  :lib project-name
                  :version version
                  :basis basis
                  :scm scm
                  :src-dirs src-dirs'
                  :resource-dirs resource-dirs'})
    (b/copy-dir {:src-dirs (concat src-dirs' resource-dirs')
                 :target-dir class-dir'})
    (b/jar {:class-dir class-dir'
            :jar-file output-file})
    (println "Created:" output-file)
    {:artifact-id project-name
     :version version
     :jar-path output-file
     :pom-path (b/pom-path {:lib project-name
                            :class-dir class-dir'})}))

(defn- sign-path
  "Do the signing of the file and use GPG's pinentry to get the necessary passphrase."
  [sign-key-id path]
  (let [args ["--yes"
              "--armour"
              "--default-key" sign-key-id
              "--detach-sign"
              path]
        {:keys [success? exit-code out err]} (gpg/gpg {:args args})]
    (when-not success?
      (binding [*out* *err*]
        (println (format "Error %d executing GPG" exit-code))
        (println out)
        (println err))
      (throw (ex-info "GPG Failure"
                      {:exit-code exit-code
                       :path path
                       :sign-key-id sign-key-id})))
    ;; Return the name of the created file
    (str path ".asc")))

(defn deploy-jar
  "Signs and deploys a JAR artifact.

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
  [artifact-data]
  (let [{:keys [artifact-id version jar-path pom-path sign-key-id work-dir]
         :or {work-dir "target"}} artifact-data
        sign-key-id' (or sign-key-id
                         (System/getenv "CLOJARS_GPG_ID")
                         (throw (RuntimeException. "CLOJARS_GPG_ID environment variable not set")))
        versioned-pom-path (str work-dir "/" (name artifact-id) "-" version ".pom")
        _ (b/copy-file {:src pom-path
                        :target versioned-pom-path})
        paths [jar-path versioned-pom-path]
        upload-paths (into paths
                           (map #(sign-path sign-key-id' %) paths))
        upload-artifacts (d/artifacts version upload-paths)
        aether-coordinates [(symbol artifact-id) version]]
    (aether/deploy :artifact-map upload-artifacts
                   ;; Clojars is the default repository for uploads
                   :repository d/default-repo-settings
                   :transfer-listener :stdout
                   :coordinates aether-coordinates)))
