(ns sportsbook.football
  (:require [sportsbook.slang :as sl]))


;;;; Markets & selectoins

(sl/defselection away
  :settle-fn 
  (let [team-1 (count (filter #(and
                                (sl/game-part? "full-time" %)
                                (sl/scope? "goal" %)
                                (sl/team? "home" %)) event-log))
        team-2 (count (filter #(and
                                (sl/game-part? "full-time" %)
                                (sl/scope? "goal" %)
                                (sl/team? "away" %)) event-log))]
    (< team-1 team-2)))

(sl/defselection home
  :settle-fn
  (let [team-1 (count (filter #(and
                                (sl/game-part? "full-time" %)
                                (sl/scope? "goal" %)
                                (sl/team? "home" %)) event-log))
        team-2 (count (filter #(and
                                (sl/game-part? "full-time" %)
                                (sl/scope? "goal" %)
                                (sl/team? "away" %)) event-log))]
    (> team-1 team-2)))

(sl/defselection draw
  :settle-fn
  (let [team-1 (count (filter #(and
                                (sl/game-part? "full-time" %)
                                (sl/scope? "goal" %)
                                (sl/team? "home" %)) event-log))
        team-2 (count (filter #(and
                                (sl/game-part? "full-time" %)
                                (sl/scope? "goal" %)
                                (sl/team? "away" %)) event-log))]
    (= team-1 team-2)))


(sl/defmarket match-winner
  :is-auto-cancel? true
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

;;; Tests

(def test-log [{:scope "goal" :game-part "full-time" :team "home"}
               {:scope "goal" :game-part "full-time" :team "away"}
               {:scope "corner" :game-part "full-time" :team "away"}])

;;(settle-selection home test-log)

(settle match-winner test-log)

(settle total-2-goals test-log)
