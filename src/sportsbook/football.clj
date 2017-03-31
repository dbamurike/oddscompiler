(ns sportsbook.football
  (:require [sportsbook.slang :as sl]))

(def full-time #{"H1" "H2"})

;;;; Markets & selectoins

(sl/defselection away
  :settle-fn 
  (let [scope (:scope MARKET-PARAMS)
        game-part (:sl/game-part MARKET-PARAMS)
        team-1 (sl/count-values ["and" {"sl/scope?" scope  "sl/game-part?" game-part "sl/team?" "home"}])
        team-2 (sl/count-values ["and" {"sl/scope?" scope  "sl/game-part?" game-part "sl/team?" "away"}])
        ]
    (< team-1 team-2)))

(sl/defselection home
  :settle-fn
  (let [scope (:scope MARKET-PARAMS)
        game-part (:sl/game-part MARKET-PARAMS)
        team-1 (sl/count-values ["and" {"sl/scope?" scope  "sl/game-part?" game-part "sl/team?" "home"}])
        team-2 (sl/count-values ["and" {"sl/scope?" scope  "sl/game-part?" game-part "sl/team?" "away"}])
        ]
    (> team-1 team-2)))

(sl/defselection draw
  :settle-fn
  (let [scope (:scope MARKET-PARAMS)
        game-part (:sl/game-part MARKET-PARAMS)
        team-1 (sl/count-values ["and" {"sl/scope?" scope  "sl/game-part?" game-part "sl/team?" "home"}])
        team-2 (sl/count-values ["and" {"sl/scope?" scope  "sl/game-part?" game-part "sl/team?" "away"}])
        ]
    (= team-1 team-2)))

(sl/defmarket match-winner
  :is-auto-cancel? true
  :MARKET-PARAMS  {:game-part full-time :scope "goal"}
  :selections [home draw away])

(sl/defselection under
  :settle-fn
  (let [total (:total MARKET-PARAMS)
        game-part (:sl/game-part MARKET-PARAMS)
        scope (:scope MARKET-PARAMS)
        goals  (count (filter #(and
                                (sl/game-part? game-part %)
                                (sl/scope? scope %)) event-log))]
    (< goals total)))

(sl/defselection over
  :settle-fn
  (let [total (:total MARKET-PARAMS)
        game-part (:sl/game-part MARKET-PARAMS)
        scope (:scope MARKET-PARAMS)
        goals  (count (filter #(and
                                (sl/game-part? game-part %)
                                (sl/scope? scope %)) event-log))]
    (> goals total)))

(sl/defmarket total-2-goals
  :is-auto-cancel? true
  :MARKET-PARAMS  {:total 2 :game-part "full-time" :scope "goal"}
  :selections [over under]
  )

