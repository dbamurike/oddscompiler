(ns sportsbook.basketball-test
  (:require [clojure.test :refer :all]
            [sportsbook.slang :as sl]
            [sportsbook.basketball :refer :all]))

(def test-log [{:scope "point" :game-part "Q1" :team "home" :value 2}
               {:scope "point" :game-part "Q1" :team "away" :value 2}
               {:scope "point" :game-part "Q1" :team "away" :value 2}
               {:scope "point" :game-part "Q1" :team "away" :value 2}
               {:scope "point" :game-part "Q1" :team "home" :value 3}
               {:scope "point" :game-part "Q1" :team "away" :value 2}
               ])

(deftest match-winner-test 
  (testing "Match winner away"
    (is (= :win  (:status  (sl/settle-selection away test-log {:game-part match-with-overtime :scope "point"})))))
  (testing "Match winner home"
    (is (= :lose  (:status (sl/settle-selection home test-log {:game-part match-with-overtime :scope "point"})))))
  (testing "Match winner market"
    (is (= {:MARKET-PARAMS {:game-part #{"OT" "Q3" "Q2" "Q1" "Q4"}, :scope "point"}, 
            :selections '({:name :home, :status :lose} 
                         {:name :away, :status :win}), 
            :name :match-winner, :is-auto-cancel? true}
           (sl/settle match-winner test-log))))

)
