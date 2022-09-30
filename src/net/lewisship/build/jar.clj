(ns net.lewisship.build.jar
  "Utilities to build a Jar and deploy it to Clojars."
  (:require [clojure.tools.build.api :as b]
            [deps-deploy.deps-deploy :as d]))

(defn ^:private default-jar-file
  [project-name version]
  (format "target/%s-%s.jar"
          (name project-name)
          version))

(def ^:private default-class-dir
  "Directory to which all sources and resources are copied, this becomes
  the primary content of the jar file."
  "target/classes")

(defn create-jar
  [options]
  (let [{:keys [project-name version src-dirs resource-dirs class-dir jar-file scm]
         :or {src-dirs ["src"]
              resource-dirs ["resources"]
              class-dir default-class-dir}} options
        basis (b/create-basis)
        src-dirs' (or src-dirs ["src"])
        resource-dirs' (or resource-dirs ["resources"])
        output-file (or jar-file
                        (default-jar-file project-name version))
        class-dir' (or class-dir default-class-dir)]
    (b/write-pom {:class-dir class-dir
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
    output-file))

(defn deploy-jar
  [options]
  (let [{:keys [project-name version jar-file class-dir]}]
    (d/deploy {:installer :remote
               :artifact (or jar-file
                             (default-jar-file project-name version))
               :pom-file (b/pom-path {:lib project-name
                                      :class-dir (or class-dir
                                                     default-class-dir)})
               :sign-releases? true
               :sign-key-id (or (System/getenv "CLOJARS_GPG_ID")
                                (throw (RuntimeException. "CLOJARS_GPG_ID environment variable not set")))})))
