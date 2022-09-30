(ns net.lewisship.build.codox
  (:require
    [clojure.tools.build.api :as b]))

(defn generate
  "Generates Codox documentation.

   The caller must pass :version (e.g., \"0.1.0\") and :project-name (e.g. 'io.github.hlship/build-tools).

   The :codox/config key in deps.edn provides defaults passed to codox; typically contains keys :description and :source-uri."
  [params]
  (let [{:keys [aliases codox-version codox-config version project-name exclusions]
         :or {codox-version "0.10.8"}} params
        _ (do
            (assert version "no :version specified")
            (assert project-name "no :project-name specified"))
        basis (b/create-basis {:extra {:deps {'codox/codox
                                              {:mvn/version codox-version
                                               :exclusions exclusions}}}
                               :aliases aliases})
        codox-config' (merge
                        {:metadata {:doc/format :markdown}}
                        (:codox/config basis)
                        codox-config
                        {:version version
                         :name (str project-name)})
        expression `(do ((requiring-resolve 'codox.main/generate-docs) ~codox-config') nil)
        process-params (b/java-command
                         {:basis basis
                          :main "clojure.main"
                          :main-args ["--eval" (pr-str expression)]})]
    (b/process process-params)
    nil))
