(ns read-invoices-test
  (:require [clojure.test :refer [deftest is testing]]
            [read-invoices :refer [filter-invoice-items]]))

(deftest test-read-invoice
  (testing "Read invoice"
    (is (= (filter-invoice-items "invoice.edn")
           '({:invoice-item/id "ii3", :invoice-item/sku "SKU 3", :taxable/taxes [#:tax{:id "t3", :category :iva, :rate 19}]}
             {:invoice-item/id "ii4",
              :invoice-item/sku "SKU 3",
              :retentionable/retentions [#:retention{:id "r2", :category :ret_fuente, :rate 1}]})))))
