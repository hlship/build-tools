(ns net.lewisship.build.versions-test
  (:require [clojure.test :refer [deftest is are]]
            [net.lewisship.build.versions :as v]))

(deftest parse-versions
  (are [s d]
    (is (= d (v/parse-version s)))

    "1.0.0" {:major 1 :minor 0 :patch 0}

    ;; Leading/trailing spaces are ignored
    "  1.2.3 " {:major 1 :minor 2 :patch 3}

    "4.3.2-snapshot" {:major 4 :minor 3 :patch 2 :stability :snapshot}

    "1.2.3-rc-3" {:major 1 :minor 2 :patch 3 :stability :rc :index 3}

    "4.3.2-SnapShot" {:major 4 :minor 3 :patch 2 :stability :snapshot}

    "1.2.3-alpha-3" {:major 1 :minor 2 :patch 3 :stability :alpha :index 3}


    "1.2.3-Rc-3" {:major 1 :minor 2 :patch 3 :stability :rc :index 3}

    "1.2.3-beta-9999" {:major 1 :minor 2 :patch 3 :stability :beta :index 9999}))

(deftest not-parseable
  (are [s]
    (is (thrown? RuntimeException (v/parse-version s)))
    ""
    "1"
    "1.0"
    ;; Spaces not allowed inside
    "1. 0.3"
    ;; No leading 'v' allowed, just bare numbers
    "v1.0.0"
    ;; Snapshot's can't have indexes
    "1.3.0-snapshot-2"
    ;; beta- and rc- must have indexes
    "1.2.3-rc"
    "1.2.3-beta")

  (deftest round-trip-valid-versions
    (are [s]
      (= s (-> s v/parse-version v/unparse-version))
      "1.0.0"
      "4.3.2-SNAPSHOT"
      "1.2.3-rc-1"
      "1.2.3-beta-2")))

(deftest advance
  (are [s l o]
    (is (= o (-> s v/parse-version
                 (v/advance l)
                 v/unparse-version)))
    "1.0.0" :major "2.0.0"
    "1.2.3" :minor "1.3.0"
    "1.2.3" :patch "1.2.4"
    "1.2.3" :snapshot "1.2.3-SNAPSHOT"
    "1.2.3" :rc "1.2.3-rc-1"
    "1.2.3" :beta "1.2.3-beta-1"
    "1.2.3" :alpha "1.2.3-alpha-1"
    "1.2.3-rc-9" :rc "1.2.3-rc-10"
    "1.2.3-beta-9" :beta "1.2.3-beta-10"
    "1.2.3-rc-9" :release "1.2.3"
    "1.2.3-snapshot" :release "1.2.3"
    "1.2.3-beta-12" :release "1.2.3"
    "1.2.3-beta-8" :snapshot "1.2.3-SNAPSHOT"
    "1.2.3-rc-9" :patch "1.2.4"
    "1.2.3-beta-9" :minor "1.3.0"
    "1.2.3-beta-9" :major "2.0.0"))

(deftest advance-already-a-release
  (is (thrown? RuntimeException
               (-> "1.2.3" v/parse-version (v/advance :release)))))
