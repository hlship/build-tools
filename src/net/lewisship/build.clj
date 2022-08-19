(ns net.lewisship.build
  "Utilities for creating build commands.")

(defmacro requiring-invoke
  "Uses `requiring-resolve` to invoke a fully qualified (but not quoted name);

  e.g. `(requiring-invoke net.lewisship.build.jar/deploy-jar jar-params)`"
  [sym & params]
  (assert (qualified-symbol? sym))
  `((requiring-resolve '~sym) ~@params))

(defmacro delegate
  "Creates a function that uses `requiring-resolve` to delegate to a real implementation.

   This is a common pattern meant to minimize load times for the build tool."
  ([to-sym]
   (assert (qualified-symbol? to-sym))
   `(delegate ~(-> to-sym name symbol) ~to-sym))
  ([local-sym to-sym]
   (assert (simple-symbol? local-sym))
   (assert (qualified-symbol? to-sym))
   `(defn ~local-sym
      [params#]
      ((requiring-resolve '~to-sym) params#))))

(delegate net.lewisship.build.jar/create-jar)
(delegate new.lewisship.build.jar/deploy-jar)
(delegate codox net.lewisship.build.codox/generate)
