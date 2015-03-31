(ns linemode.core-test
  (:import [java.io ByteArrayOutputStream])
  (:require [clojure.test :refer :all]
            [linemode.core :refer [run-commands]]
            [linemode.drivers.star :refer [star-printer]]))


(deftest compile-test
  (testing "Simple writes should be copied straight to output"
    (let [output (new ByteArrayOutputStream)
          printer (star-printer output)]
      ; TODO
      (run-commands printer [[:write "Hello World"]])
      (is (= (.toString output) "Hello World")))))
