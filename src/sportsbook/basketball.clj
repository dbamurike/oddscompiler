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


(sl/defmarket match-winner
  :is-auto-cancel? true
  :MARKET-PARAMS  {:game-part match-with-overtime :scope "point"}
  :selections [home away])


(def test-log [{:scope "point" :game-part "Q1" :team "home" :value 2}
               {:scope "point" :game-part "Q1" :team "away" :value 2}
               {:scope "point" :game-part "Q1" :team "away" :value 2}
               {:scope "point" :game-part "Q1" :team "away" :value 2}
               {:scope "point" :game-part "Q1" :team "home" :value 3}
               {:scope "point" :game-part "Q1" :team "away" :value 2}
               ])


(sl/settle-selection home test-log {:game-part match-with-overtime :scope "point"})

(sl/settle match-winner test-log)

