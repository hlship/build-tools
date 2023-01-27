(ns net.lewisship.build.versions
  "Utilities to parse strings to version data, advance at a level, and unparse
  versions.

  Opinionated:  -SNAPSHOT can't have an index, -beta and -rc must have an index.
  Releases are always major.minor.patch."
  (:require [clojure.string :as str]))

(defn parse-version
  [version]
  ;; -snapshot not allowed an index, but -beta and -rc require one.
  (let [[_ major minor patch _ stability _ index :as match]
        (re-matches #"(?ix)
               \s*
               (\d+)     # major
               \. (\d+)  # minor
               \. (\d+)  # patch
               (\-
                 (snapshot|beta|rc)
                 (\-
                   (\d+))?)?
               \s*"
                    version)
        stability' (when stability
                     (-> stability str/lower-case keyword))]
    (when (or (nil? match)
              (not= (some? index)
                    (contains? #{:beta :rc} stability')))
      (throw (RuntimeException. (format "Version '%s' is not parsable" version))))
    (cond-> {:major (parse-long major)
             :minor (parse-long minor)
             :patch (parse-long patch)}
      stability' (assoc :stability stability')
      index (assoc :index (parse-long index)))))

(def ^:private stability->label
  ;; The rules about snapshot versions and that SNAPSHOT is all caps is very specific
  ;; to how Clojars handles snapshots.
  {:snapshot "SNAPSHOT"
   :beta "beta"
   :rc "rc"})

(defn unparse-version
  [version-data]
  (let [{:keys [major minor patch stability index]} version-data]
    (str major
         "."
         minor
         "."
         patch
         (when (some? stability)
           (str "-" (stability->label stability)))
         (when (contains? #{:rc :beta} stability)
           (str "-" index)))))

(defn advance
  "Given parsed version data and a desired stability level, this returns a new version
   data with the desired stability level and adds/increments/removes the index."
  [{:keys [stability] :as version-data} level]
  (case level
    :major (-> version-data
               (update :major inc)
               (assoc :minor 0 :patch 0)
               (dissoc :index :stability))
    :minor (-> version-data
               (update :minor inc)
               (assoc :patch 0)
               (dissoc :index :stability))
    :patch (-> version-data
               (update :patch inc)
               (dissoc :index :stability))
    :release (if (nil? stability)
               (throw (IllegalStateException. "Already a release version, not a snapshot, beta, or rc"))
               (-> version-data
                   (dissoc :index :stability)))
    :snapshot (-> version-data
                  (assoc :stability :snapshot)
                  (dissoc :index))
    (:beta :rc) (if (= stability level)
                  (update version-data :index inc)
                  (assoc version-data :stability level :index 1))))
