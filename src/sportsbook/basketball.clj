(ns sportsbook.basketball
  (:require [sportsbook.slang :as sl]))

(def match-with-overtime #{"Q1" "Q2" "Q3" "Q4" "OT"})

(sl/defselection away
  :settle-fn
  (let [game-part (:game-part MARKET-PARAMS)
        scope (:scope MARKET-PARAMS)
        team-1 (sl/sum-values ["and" {"sl/scope?" scope  "sl/game-part?" game-part "sl/team?" "home"}])
        team-2 (sl/sum-values ["and" {"sl/scope?" scope  "sl/game-part?" game-part "sl/team?" "away"}])]
    (< team-1 team-2)))


(sl/defselection home
  :settle-fn
  (let [game-part (:game-part MARKET-PARAMS)
        scope (:scope MARKET-PARAMS)
        team-1 (sl/sum-values ["and" {"sl/scope?" scope  "sl/game-part?" game-part "sl/team?" "home"}])
        team-2 (sl/sum-values ["and" {"sl/scope?" scope  "sl/game-part?" game-part "sl/team?" "away"}])]
  (> team-1 team-2)))

;;; Match Winner with OT

(sl/defmarket match-winner
  :is-auto-cancel? true
  :MARKET-PARAMS  {:game-part match-with-overtime :scope "point"}
  :selections [home away])

;;; Total of 3 points 

(sl/defselection under-value
  :settle-fn
  (let [total (:total MARKET-PARAMS)
        game-part (:game-part MARKET-PARAMS)
        scope (:scope MARKET-PARAMS)
        value (:value MARKET-PARAMS)
        goals  (sl/count-values ["and" {"sl/scope?" scope  "sl/game-part?" game-part "sl/value?" value}])
        ]
    (< goals total)))

(sl/defselection over-value
  :settle-fn
  (let [total (:total MARKET-PARAMS)
        game-part (:game-part MARKET-PARAMS)
        value (:value MARKET-PARAMS)
        scope (:scope MARKET-PARAMS)
        goals  (sl/count-values ["and" {"sl/scope?" scope  "sl/game-part?" game-part "sl/value?" value}])
        ]
    (> goals total)))

;; Market

(sl/defmarket total-2-3-points-points-Q1
  :is-auto-cancel? true
  :MARKET-PARAMS  {:total 2 :game-part "Q1" :scope "point" :value 3}
  :selections [over-value under-value]
  )
