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
  (= value  (get dict key false)))

(def game-part? (partial get-compare :game-part))
(def scope? (partial get-compare :scope))
(def team? (partial get-compare :team))

(defn fill-auto-cancel [cancel? selections]
  (if (and cancel? (every? lose? (map :status selections)))
    (map #(assoc % :status :cancel) selections)
    selections))

(defmacro defselection [selection-name & body]
  (let [hash-body (apply hash-map body)]
    `(def ~selection-name ~hash-body)))

(defmacro defmarket [market-name & body]
  `(def ~market-name ~(apply hash-map body)))

(defn settle-selection [selection event-log]
  (let [settle-fn (:settle-fn selection)
        settle-result (settle-fn event-log)]
    {:name (:name selection) :status (map-result settle-result)}))

(defn settle [market event-log]
  (let [selections-results (->> market
                                :selections
                                (map #(settle-selection % event-log))
                                (fill-auto-cancel (:is-auto-cancel? market) )
                                )]
    (assoc market :selections selections-results)
    ))

;;;; Markets & selectoins

(defselection away
              :id 1
              :name :away
              :map-id 32
              :settle-fn (fn [event-log]
                           (let [team-1 (count (filter #(and
                                                          (game-part? "full-time" %)
                                                          (scope? "goal" %)
                                                          (team? "home" %)) event-log))
                                 team-2 (count (filter #(and
                                                          (game-part? "full-time" %)
                                                          (scope? "goal" %)
                                                          (team? "away" %)) event-log))
                                 ]
                             (< team-1 team-2))))


(defselection home
              :id 1
              :name :home
              :map-id 32
              :settle-fn
              (fn [event-log]
                (let [team-1 (count (filter #(and
                                               (game-part? "full-time" %)
                                               (scope? "goal" %)
                                               (team? "home" %)) event-log))
                      team-2 (count (filter #(and
                                               (game-part? "full-time" %)
                                               (scope? "goal" %)
                                               (team? "away" %)) event-log))]
                  (> team-1 team-2))))

(defselection draw
              :id 3
              :name :draw
              :map-id 32
              :settle-fn
              (fn [event-log]
                (let [team-1 (count (filter #(and
                                               (game-part? "full-time" %)
                                               (scope? "goal" %)
                                               (team? "home" %)) event-log))
                      team-2 (count (filter #(and
                                               (game-part? "full-time" %)
                                               (scope? "goal" %)
                                               (team? "away" %)) event-log))]
                  (= team-1 team-2))))


(defmarket match-winner
           :is-auto-cancel? true
           :selections [home draw away]
           :id 1)


(defselection over
              :id 10
              :name :over
              :map-id 32
              :settle-fn
              (fn [event-log]
                (let [param 2
                      total  (count (filter #(and
                                              (game-part? "full-time" %)
                                              (scope? "goal" %)) event-log))]
                  (print total)
                  (> total param))))

(defselection under
              :id 11
              :name :over
              :map-id 32
              :settle-fn
              (fn [event-log]
                (let [param 2
                      total  (count (filter #(and
                                               (game-part? "full-time" %)
                                               (scope? "goal" %)) event-log))]
                  (< total param))))

(defmarket total
  :is-auto-cancel? true
  :params {:total 1.5}
  :selections [over under]
  :id 2)

;;; Tests

(def test-log [{:scope "goal" :game-part "full-time" :team "home"}
               {:scope "goal" :game-part "full-time" :team "away"}
               {:scope "corner" :game-part "full-time" :team "away"}])

;;(settle-selection home test-log)

(settle match-winner test-log)

(settle total test-log)
