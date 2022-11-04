(ns net.lewisship.build.codox
  (:require
    [clojure.tools.build.api :as b]))

(defn generate
  [params]
  (let [{:keys [aliases codox-version codox-config version project-name output-path exclusions]
         :or {codox-version "0.10.8"}} params
        _ (do
            (assert version "no :version specified")
            (assert project-name "no :project-name specified"))
        basis (b/create-basis {:extra {:deps {'codox/codox
                                              {:mvn/version codox-version
                                               :exclusions exclusions}}}
                               :aliases aliases})
        project-config (cond-> {:version version
                                :name (str project-name)}
                         output-path (assoc :output-path output-path))
        codox-config' (merge
                        {:metadata {:doc/format :markdown}}
                        (:codox/config basis)
                        codox-config
                        project-config)
        expression `(do ((requiring-resolve 'codox.main/generate-docs) ~codox-config') nil)
        process-params (b/java-command
                         {:basis basis
                          :main "clojure.main"
                          :main-args ["--eval" (pr-str expression)]})]
    (b/process process-params)
    nil))
