(ns net.lewisship.build.jar
  "Utilities to build a Jar and deploy it to Clojars."
  (:require [clojure.set :as set]
            [clojure.tools.build.api :as b]
            [deps-deploy.deps-deploy :as d]
            [deps-deploy.gpg :as gpg]
            [cemerick.pomegranate.aether :as aether]))

(defn- default-jar-file
  [project-name version]
  (format "target/%s-%s.jar"
          (name project-name)
          version))

(def ^:private licenses
  {:epl {:name "Eclipse Public License"
         :url "http://www.eclipse.org/legal/epl-v10.html"}
   :asl {:name "Apache License, Version 2.0"
         :url "https://www.apache.org/licenses/LICENSE-2.0.html"}})

(defn- extract-license-info
  [scm]
  (when-let [license (:license scm)]
    (cond
      (keyword? license)
      (or (get licenses license)
          (throw (ex-info (str "Unknown license: " license)
                          {:scm scm})))

      (map? license)
      license                                               ; assume it has the expected keys!

      :else
      (throw (ex-info "Unexpected license value" {:scm scm})))))

(defn create-jar
  [options]
  (let [{:keys [project-name version src-dirs resource-dirs class-dir jar-file scm aliases]} options
        basis (b/create-basis {:aliases aliases})
        src-dirs' (or src-dirs ["src"])
        resource-dirs' (or resource-dirs ["resources"])
        output-file (or jar-file
                        (default-jar-file project-name version))
        class-dir' (or class-dir "target/classes")
        scm' (merge scm
                    (:net.lewisship.build/scm basis)
                    {:tag version})
        license (extract-license-info scm')]
    (b/write-pom (cond-> {:class-dir class-dir'
                          :lib project-name
                          :version version
                          :basis basis
                          :scm (dissoc scm' :license)
                          :src-dirs src-dirs'
                          :resource-dirs resource-dirs'}

                         license (assoc :pom-data [[:licenses
                                                    [:license
                                                     [:name (:name license)]
                                                     [:url (:url license)]]]])))
    (b/copy-dir {:src-dirs (concat src-dirs' resource-dirs')
                 :target-dir class-dir'})
    (b/jar {:class-dir class-dir'
            :jar-file output-file})
    (println "Created:" output-file)
    ;; Return options that can be passed to deploy-jar
    {:artifact-id project-name
     :version version
     :jar-path output-file
     :class-dir class-dir'
     :basis basis
     :pom-path (b/pom-path {:lib project-name
                            :class-dir class-dir'})}))

(defn install-jar
  [options]
  (b/install (set/rename-keys options {:artifact-id :lib
                                       :jar-path :jar-file})))

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
  :sign-artifacts? (boolean, optional, default: true)
  :work-dir (string, optional) - directory to write temporary artifacts to
  (defaults to \"target\")

  The :pom-path is usually computed via clojure.tools.build.api/pom-path.

  If :sign-key-id is omitted, it is obtained from environment variable CLOJARS_GPG_ID; if
  that is not set, then a RuntimeException is thrown."
  [artifact-data]
  (let [{:keys [artifact-id version jar-path pom-path sign-key-id work-dir sign-artifacts?]
         :or {work-dir "target"
              sign-artifacts? true}} artifact-data
        sign-key-id' (when sign-artifacts?
                       (or sign-key-id
                           (System/getenv "CLOJARS_GPG_ID")
                           (throw (RuntimeException. "CLOJARS_GPG_ID environment variable not set"))))
        versioned-pom-path (str work-dir "/" (name artifact-id) "-" version ".pom")
        _ (b/copy-file {:src pom-path
                        :target versioned-pom-path})
        paths [jar-path versioned-pom-path]
        upload-paths (if sign-artifacts?
                       (into paths
                             (map #(sign-path sign-key-id' %) paths))
                       paths)
        upload-artifacts (d/artifacts version upload-paths)
        aether-coordinates [(symbol artifact-id) version]]
    (aether/deploy :artifact-map upload-artifacts
                   ;; Clojars is the default repository for uploads
                   :repository d/default-repo-settings
                   :transfer-listener :stdout
                   :coordinates aether-coordinates)))
