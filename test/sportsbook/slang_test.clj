(ns sportsbook.slang-test
  (:require [clojure.test :refer :all]
            [sportsbook.slang :as sl]))

(def event-log [{:scope "point" :game-part "Q1" :team "home" :value 2}
               {:scope "point" :game-part "Q1" :team "away" :value 2}
               {:scope "point" :game-part "Q1" :team "away" :value 2}
               {:scope "point" :game-part "Q1" :team "away" :value 2}
               {:scope "point" :game-part "Q1" :team "home" :value 3}
               {:scope "point" :game-part "Q1" :team "away" :value 2}
               ])

(deftest aggregaes-test
  (testing "sum of values in log"
    (is (= 5 (sl/sum-values ["and" {"sl/game-part?" "Q1" "sl/team?" "home"}]))))
  (testing "count of values in log"
    (is (= 1 (sl/count-values ["and" {"sl/game-part?" "Q1" "sl/team?" "home" "sl/value?" 3}])))))

