(ns invoice-item-test
  (:require [clojure.test :refer [deftest is testing]]
            [invoice-item :refer [subtotal]]))

(deftest test-subtotal
  (testing "Subtotal with positive values"
    (is (= (subtotal {:invoice-item/precise-quantity 100 :invoice-item/precise-price 10 :invoice-item/discount-rate 10}) 900.0)))
  (testing "Subtotal with zero discount"
    (is (= (subtotal {:invoice-item/precise-quantity 100 :invoice-item/precise-price 10 :invoice-item/discount-rate 0}) 1000.0)))
  (testing "Subtotal with 100% discount"
    (is (= (subtotal {:invoice-item/precise-quantity 100 :invoice-item/precise-price 10 :invoice-item/discount-rate 100}) 0.0)))
  (testing "Subtotal with default discount"
    (is (= (subtotal {:invoice-item/precise-quantity 100 :invoice-item/precise-price 10}) 1000.0)))
  (testing "Subtotal with negative values"
    (is (thrown? ArithmeticException (subtotal {:invoice-item/precise-quantity -100 :invoice-item/precise-price -10 :invoice-item/discount-rate -10.0}))))
  (testing "Subtotal with values greater than 100"
    (is (= (subtotal {:invoice-item/precise-quantity 200 :invoice-item/precise-price 150 :invoice-item/discount-rate 50}) 15000.0))))
