(ns net.lewisship.build
  "Utilities for creating build commands.")

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
