(ns sportsbook.football)

(defn is-contain? [k v list-of-structs]
  (filter #(= v (k %)) list-of-structs))

(def game-part? (partial is-contain? :game-part))
(def scope? (partial is-contain? :scope))
(def team? (partial is-contain? :team))

(defmacro defsettle [lets settle-body]
  `(fn [event-logs#]
     (let ~lets 
      ~settle-body)))

(macroexpand '(defsettle [a 1 b 2] (> a b)))
(def f1 (defsettle [a 1 b 2] (> a b)))

(defmacro defselection [selection-name & body]
  (let [hash-body (apply hash-map body)
        lets (:let hash-body)
        settle (:settle-predicat hash-body)
        hash-body (dissoc hash-body :let)
        hash-body (dissoc hash-body :settle-predicat)
;        hash-body (assoc hash-body :settle 1)
        settle-fn `(fn [~'event-log]
                     (let ~lets 
                       ~settle))
        ]
    
    `(def ~selection-name ~(assoc hash-body :settle settle-fn))))

(defmacro defmarket [market-name & body]
  `(def ~market-name ~(apply hash-map body)))

(defselection home
  :id 1
  :name :home
  :map-id 32
  :let [team-1 (count (and 
                       (game-part? "full-time" event-log)
                       (scope?  "goal" event-log)
                       (team? "team-1" event-log)))
        team-2 (count (and 
                       (game-part? "full-time" event-log)
                       (scope?  "goal" event-log)
                       (team? "team-2" event-log)))]
  
  :settle-predicat  (> team-1 team-2))

(defselection away
  :id 1
  :name :home
  :map-id 32
  :let [team-1 (count (and 
                       (game-part? "full-time" event-log)
                       (scope?  "goal" event-log)
                       (team? "team-1" event-log)))
        team-2 (count (and 
                       (game-part? "full-time" event-log)
                       (scope?  "goal" event-log)
                       (team? "team-2" event-log)))]
  
  :settle-predicat  (< team-1 team-2))

(defselection draw
  :id 1
  :name :home
  :map-id 32
  :let [team-1 (count (and 
                       (game-part? "full-time" event-log)
                       (scope?  "goal" event-log)
                       (team? "team-1" event-log)))
        team-2 (count (and 
                       (game-part? "full-time" event-log)
                       (scope?  "goal" event-log)
                       (team? "team-2" event-log)))]
  
  :settle-predicat  (= team-1 team-2))


(defmarket match-winner 
  :selections [home draw away]
  :id 1)

(defn settle-selection [selection event-log]
  (->> selection :settle event-log))

(def test-log [{:scope "goal" :game-part "full-time" :team "home"}
               {:scope "goal" :game-part "full-time" :team "away"}
               {:scope "corner" :game-part "full-time" :team "away"}])

(settle-selection home test-log)

(defn settle [market event-log]
  (->> market
       :selections
       (map #(settle-selection event-log %))))

(settle match-winner test-log) -> [win lose win]
