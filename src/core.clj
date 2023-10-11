(ns core
  (:require [clojure.data.json :as json]
           [clojure.spec.alpha :as s]
           [clojure.walk :as walk]
           [clj-time.coerce :as c]
           [clj-time.format :as time-format]
           [invoice-spec :as invoice-spec]))

(defn xor [a b]
  (or (and a (not b))
      (and (not a) b)))

(defn check-item
  "Checks if an invoice item sadisfies the criteria."
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

(defn- convert-snake-to-kebab
  "Converts a snake_case keyword to a kebab-case keyword."
  [k]
  (-> k name (clojure.string/replace "_" "-") keyword))

(defn- recursively-convert-keys
  "Recursively converts the keys in a nested map using the provided function."
  [m f]
  (into {} (map (fn [[k v]] [(f k) (if (map? v) (recursively-convert-keys v f) v)]) m)))

(defn- parse-invoice
  "Reads the invoice JSON file and converts it to a Clojure map with kebab-case keys."
  [file-name]
  (-> (slurp file-name)
      (json/read-str :key-fn keyword)
      (recursively-convert-keys convert-snake-to-kebab)))

(defn- adjust-keys
  "Updates the keys in a map based on the provided key mappings."
  [data keys]
  (let [[old-key new-key] keys]
    (walk/postwalk
     (fn [x]
       (if (and (map? x) (contains? x old-key))
         (-> x
             (dissoc old-key)
             (assoc new-key (x old-key)))
         x))
     data)))

(defn- convert-string-to-instant
  "Converts a string in the format 'dd/MM/yyyy' to a Clojure instant."
  [s]
  (-> (time-format/formatter "dd/MM/yyyy")
      (time-format/parse s)
      c/to-date))

(defn- adjust-values
  "Adjusts the values in a map to match the invoice spec."
  [data]
  (walk/postwalk
   (fn [x]
     (if (map? x)
       (cond
         (and (contains? x :tax/category) (= (:tax/category x) "IVA"))
         (-> (assoc x :tax/category :iva) (assoc :tax/rate (double (:tax/rate x))))

         (contains? x :invoice/issue-date)
         (assoc x :invoice/issue-date (convert-string-to-instant (:invoice/issue-date x)))
         :else x)
       x))
   data))

(def replace-keys [[:issue-date :invoice/issue-date] [:items :invoice/items] [:customer :invoice/customer] [:price :invoice-item/price]
                   [:quantity :invoice-item/quantity] [:sku :invoice-item/sku] [:taxes :invoice-item/taxes]
                   [:tax_category :tax/category] [:tax_rate :tax/rate] [:company-name :customer/name] [:email :customer/email]])

(defn- apply-key-mappings
  "Applies a series of key mappings to an invoice."
  [invoice]
  (loop [invoice invoice
         keys replace-keys]
    (if (empty? keys)
      invoice
      (recur (adjust-keys invoice (first keys))
             (rest keys)))))

(defn generate-invoice
  "Generates an invoice from a JSON file and validates it against the invoice spec."
  [file-name]
  (let [invoice (-> file-name
                    parse-invoice
                    apply-key-mappings
                    first
                    val
                    adjust-values)]
    (if (s/valid? :invoice-spec/invoice invoice)
      invoice
      (throw (ex-info "Invalid invoice" {:invoice invoice})))))

(comment
  (generate-invoice "invoice.json")
  (filter-invoice-items "invoice.edn")
  "")
