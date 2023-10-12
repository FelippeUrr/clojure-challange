(ns generate-invoice-test
  (:require [clojure.test :refer [deftest is testing]]
            [generate-invoice :refer [main]]
            [clojure.spec.alpha :as s]
            [invoice-spec :as invoice-spec]))


(deftest test-generate-invoice
  (testing "Validate invoice with Spec"
    (is (= (s/valid? :invoice-spec/invoice (main "invoice.json")) true))))
