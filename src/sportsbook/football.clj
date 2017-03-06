(ns sportsbook.football)

(defn alias-result [alias alternative result]
  (when (= alternative result)
    alias))

(defn is-result? [result selection-result]
  (->> selection-result :status (= result)))

(def win (partial alias-result :win true))
(def win? (partial is-result? :win))

(def lose (partial alias-result :lose false))
(def lose? (partial is-result? :lose))

(def undefined (partial alias-result :undefined nil))
(def undefined? (partial is-result? :undefined))

(def results-mapper (juxt win lose undefined))

(defn map-result [selection-result]
  (->> selection-result
       results-mapper
       (remove nil?)
       first
       ))

(defn is-contain? [k v list-of-structs]
  (filter #(= v (k %)) list-of-structs))

(def game-part? (partial is-contain? :game-part))
(def scope? (partial is-contain? :scope))
(def team? (partial is-contain? :team))

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
                             (println team-1 team-2)
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
                  (println team-1 team-2)
                  (> team-1 team-2))))

(defmarket match-winner
           :is-auto-cancel? false
           :selections [home away]
           :id 1)

;;; Tests

(def test-log [{:scope "goal" :game-part "full-time" :team "home"}
               {:scope "goal" :game-part "full-time" :team "away"}
               {:scope "corner" :game-part "full-time" :team "away"}])

;;(settle-selection home test-log)

(settle match-winner test-log)