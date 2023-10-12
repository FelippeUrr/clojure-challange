(ns read-invoices)

(defn xor [a b]
  (or (and a (not b))
      (and (not a) b)))

(defn check-item
  "Checks if an invoice item satisfies the criteria."
  [item]
  (let [taxes (:taxable/taxes item)
        retentions (:retentionable/retentions item)
        iva-19 (some #(and (= 19 (:tax/rate %)) (= :iva (:tax/category %))) taxes)
        ret_fuente-1 (some #(and (= 1 (:retention/rate %)) (= :ret_fuente (:retention/category %))) retentions)]
    (xor iva-19 ret_fuente-1)))

(defn filter-invoice-items
  "Filters invoice items based on the criteria."
  [invoice]
  (->> (clojure.edn/read-string (slurp invoice))
       :invoice/items
       (filter check-item)))

(comment
  (filter-invoice-items "invoice.edn")
  "")
