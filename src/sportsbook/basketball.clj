(ns sportsbook.basketball
  (:require [sportsbook.slang :as sl]))

(def match-with-overtime #{"Q1" "Q2" "Q3" "Q4" "OT"})


(sl/defselection away
  :settle-fn
  (let [game-part (:game-part MARKET-PARAMS)
        scope (:scope MARKET-PARAMS)
        team-1 (reduce + (map :value (filter #(and
                                                (sl/game-part? game-part %)
                                                (sl/scope? scope %)
                                                (sl/team? "home" %)) event-log)))
        team-2 (reduce + (map :value (filter #(and
                                                (sl/game-part? game-part %)
                                                (sl/scope? scope %)
                                                (sl/team? "away" %)) event-log)))]
    (< team-1 team-2)))


(sl/defselection home
  :settle-fn
  (let [game-part (:game-part MARKET-PARAMS)
        scope (:scope MARKET-PARAMS)
        team-1 (reduce + (map :value (filter #(and
                                                (sl/game-part? game-part %)
                                                (sl/scope? scope %)
                                                (sl/team? "home" %)) event-log)))
        team-2 (reduce + (map :value (filter #(and
                                                (sl/game-part? game-part %)
                                                (sl/scope? scope %)
                                                (sl/team? "away" %)) event-log)))]
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

