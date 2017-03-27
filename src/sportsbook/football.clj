(ns sportsbook.football)


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

(defn get-compare [key value dict]
  (= value (get dict key false)))

(def game-part? (partial get-compare :game-part))
(def scope? (partial get-compare :scope))
(def team? (partial get-compare :team))


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

(macroexpand  '(defselection under
                :settle-fn
                (let [param (get MARKET-PARAMS :total)
                      total  (count (filter #(and
                                              (game-part? "full-time" %)
                                              (scope? "goal" %)) event-log))]
                  (< total param))))


(defmacro defmarket [market-name & body]
  (let [market-body (apply hash-map body)
        market (assoc market-body :name (keyword market-name))]
    `(def ~market-name ~market)))

(macroexpand '(defmarket total 
                :is-auto-cancel? true
                :MARKET-PARAMS  {:total 1.5 :game-part "full-time" :scope "goal"}
                :selections [over under]
                )
             )

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

;;;; Markets & selectoins

(defselection away
  :settle-fn 
  (let [team-1 (count (filter #(and
                                (game-part? "full-time" %)
                                (scope? "goal" %)
                                (team? "home" %)) event-log))
        team-2 (count (filter #(and
                                (game-part? "full-time" %)
                                (scope? "goal" %)
                                (team? "away" %)) event-log))]
    (< team-1 team-2)))

(defselection home
  :settle-fn
  (let [team-1 (count (filter #(and
                                (game-part? "full-time" %)
                                (scope? "goal" %)
                                (team? "home" %)) event-log))
        team-2 (count (filter #(and
                                (game-part? "full-time" %)
                                (scope? "goal" %)
                                (team? "away" %)) event-log))]
    (> team-1 team-2)))

(defselection draw
  :settle-fn
  (let [team-1 (count (filter #(and
                                (game-part? "full-time" %)
                                (scope? "goal" %)
                                (team? "home" %)) event-log))
        team-2 (count (filter #(and
                                (game-part? "full-time" %)
                                (scope? "goal" %)
                                (team? "away" %)) event-log))]
    (= team-1 team-2)))


(defmarket match-winner
  :is-auto-cancel? true
  :selections [home draw away])

(defselection under
  :settle-fn
  (let [total (:total MARKET-PARAMS)
        game-part (:game-part MARKET-PARAMS)
        scope (:scope MARKET-PARAMS)
        goals  (count (filter #(and
                                (game-part? game-part %)
                                (scope? scope %)) event-log))]
    (< goals total)))

(defselection over
  :settle-fn
  (let [total (:total MARKET-PARAMS)
        game-part (:game-part MARKET-PARAMS)
        scope (:scope MARKET-PARAMS)
        goals  (count (filter #(and
                                (game-part? game-part %)
                                (scope? scope %)) event-log))]
    (> goals total)))

(defmarket total-2-goals
  :is-auto-cancel? true
  :MARKET-PARAMS  {:total 2 :game-part "full-time" :scope "goal"}
  :selections [over under]
  )

;;; Tests

(def test-log [{:scope "goal" :game-part "full-time" :team "home"}
               {:scope "goal" :game-part "full-time" :team "away"}
               {:scope "corner" :game-part "full-time" :team "away"}])

;;(settle-selection home test-log)

(settle match-winner test-log)

(settle total-2-goals test-log)
