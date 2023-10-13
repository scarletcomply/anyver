(ns scarlet.anyver-test
  (:require [clojure.test :refer [deftest is]]
            [scarlet.anyver :as anyver :refer [version version-compare
                                               version?]]))

(deftest coerce-test
  (is (version? (version "1.0")))
  (is (version? (version "v1.0")))
  (is (version? (version [1 0])))
  (is (version? (version 1)))
  (is (version? (version (version "1.0"))))
  (is (version? (version nil))))

(deftest equals-hashCode-test
  (let [v1 (version [1 0])
        v2 (version "1-0")
        v3 (version "v1.0")]

    (is (= v1 v1))
    (is (= v2 v2))
    (is (= v3 v3))

    (is (= v1 v2))
    (is (= v1 v3))
    (is (= v2 v3))

    (is (= (hash v1) (hash v2)))
    (is (= (hash v1) (hash v3)))
    (is (= (hash v2) (hash v3)))

    (is (not= v1 (str v1)))
    (is (not= v2 (str v2)))
    (is (not= v3 (str v3)))

    (is (not= (version 1) 1))
    (is (not= (version [1 0]) [1 0]))

    (is (= anyver/empty-version (version nil)))
    (is (not= anyver/empty-version nil))))

(deftest seq-test
  (is (= [1 0] (seq (version "v1.0"))))
  (is (= [1 2 3 "alpha" 42] (seq (version "1.2.3-alpha42")))))

(deftest compare-test
  (is (pos? (version-compare "2" "1")))
  (is (pos? (version-compare "10" "9")))
  (is (pos? (version-compare "1.10.2" "1.9.9")))
  (is (pos? (version-compare "2.0" "1.0")))
  (is (pos? (version-compare "2.0" "2")))
  (is (pos? (version-compare "2.1" "2.0.0")))
  (is (pos? (version-compare "1.0" nil)))
  (is (pos? (version-compare "1.0" "1.0-alpha")))
  (is (pos? (version-compare "1.0-rc2" "1.0-rc1")))
  (is (pos? (version-compare "20231013160800" "20231012100400")))
  (is (pos? (version-compare "2147483650" "1")))
  (is (pos? (version-compare "2147483651" "2147483650")))

  (is (zero? (version-compare "1.0" "v1.0")))
  (is (zero? (version-compare "1.0" "1_0")))
  (is (zero? (version-compare "1.0" "1-0")))
  (is (zero? (version-compare "1.0" "1+0")))
  (is (zero? (version-compare "1.0" [1 0])))
  (is (zero? (version-compare "1" 1)))
  (is (zero? (version-compare "v1" 1)))
  (is (zero? (version-compare "1.0-beta2" [1 0 "beta" 2])))
  (is (zero? (version-compare nil nil)))
  (is (zero? (version-compare [1 nil] [1 nil])))

  (is (neg? (version-compare "1.0.0" "1.0.0+365")))
  (is (neg? (version-compare "2.0.0" "4")))
  (is (neg? (version-compare "1.9.7" "1.12.0")))
  (is (neg? (version-compare 1 [1 0])))
  (is (neg? (version-compare "1.0.0-alpha2" "1.0.0-beta1")))
  (is (neg? (version-compare "1.0.0-alpha2" "1.0.0-alpha3")))
  (is (neg? (version-compare "1.0.0-alpha2" "1.0.0-alpha10")))
  (is (neg? (version-compare "1.0.0-alpha2" "1.0.0")))
  (is (neg? (version-compare "1" "2147483650")))
  (is (neg? (version-compare "2147483650" "2147483651")))
  (is (neg? (version-compare nil "1.0")))

  (is (anyver/earlier? "v1.0" "2.1"))
  (is (anyver/earlier? nil "2.1"))
  (is (anyver/earlier? "1.1" "1.1.0-alpha1" "1.1.0-rc2" "1.1.0"))

  (is (anyver/later? "7" "v6.9-rc6"))
  (is (anyver/later? "7" nil))
  (is (anyver/later? "1.1.0" "1.1.0-rc2" "1.1.0-alpha1" "1.1"))

  (is (= "v3.1.4" (anyver/latest ["1.0" "v3.1" "v3.1.4" "3.1.3" "2" "1.8"]))))

(deftest sort-test
  (let [versions ["0.7" "1.0" "1.8" "2" "2.0" "v3.1" "3.1.3" "v3.1.4"]]
    (is (= versions (-> versions shuffle anyver/version-sort)))))
