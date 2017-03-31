(ns sportsbook.slang)

(defn is-contain? [result selection-result]
  (= selection-result result))

(defn alias-result [alias alternative result]
  (when (is-contain? alternative result)
    alias))


(def win (partial alias-result :win true))
(def win? (partial is-contain? :win))

(def lose (partial alias-result :lose false))
(def lose? (partial is-contain? :lose))

(def undefined (partial alias-result :undefined nil))
(def undefined? (partial is-contain? :undefined))

(def results-mapper (juxt win lose undefined))

(defn map-result [selection-result]
  (->> selection-result
       results-mapper
       (remove nil?)
       first
       ))

(defmulti get-compare (fn [key value dict] (type value)))

(defmethod get-compare clojure.lang.PersistentHashSet [key value dict]
  (get value (get dict key false)))

(defmethod get-compare :default [key value dict]
  (= value (get dict key false)))

(def game-part? (partial get-compare :game-part))
(def scope? (partial get-compare :scope))
(def team? (partial get-compare :team))
(def value? (partial get-compare :value))

(defn make-predicates [body]
  (map (fn
         [[predicate value]]
         (list predicate value)) body))

(defmacro filter-event-log [[and-or body]]
  (let [predicates (make-predicates body)]
    `(fn [~'log] 
       (~(symbol and-or) ~@(map (fn [[predicate value]]
                                  (list (symbol predicate) value (symbol "log"))) predicates)))
    ))

(defmacro sum-values [filters]
  `(->>
    ~'event-log
    (filter (filter-event-log ~filters))
    (map :value)
    (reduce +)))

(defmacro count-values [filters]
  `(->>
    ~'event-log
    (filter (filter-event-log ~filters))
    (map :value)
    count))

(defn count-events [event-log & {:keys [game-part scope team]}]
  [game-part scope team])

(defn fill-auto-cancel [cancel? selections]
  (if (and cancel? (every? lose? (map :status selections)))
    (map #(assoc % :status :cancel) selections)
    selections))

;; DANGER MACRO ZONE

(defmacro defselection [selection-name & body]
  (let [hash-body (apply hash-map body)
        settle-fn-body (:settle-fn hash-body)
        settle-fn `(fn [~'event-log ~'MARKET-PARAMS] ~settle-fn-body)
        selection (assoc hash-body :settle-fn settle-fn :name (keyword selection-name))]
    `(def ~selection-name ~selection)))


(defmacro defmarket [market-name & body]
  (let [market-body (apply hash-map body)
        market (assoc market-body :name (keyword market-name))]
    `(def ~market-name ~market)))


;; SETTLEMENT FUNCS

(defn settle-selection [selection event-log market-params]
  (let [settle-fn (:settle-fn selection)
        settle-result (settle-fn event-log market-params)]
    {:name (:name selection) :status (map-result settle-result)}))

(defn settle [market event-log]
  (let [market-params (:MARKET-PARAMS market)
        selections-results (->> market
                                :selections
                                (map #(settle-selection % event-log market-params))
                                (fill-auto-cancel (:is-auto-cancel? market) )
                                )]
    (assoc market :selections selections-results)
    ))

