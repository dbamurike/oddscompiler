(ns sportsbook.football-test
  (:require [clojure.test :refer :all]
            [sportsbook.slang :as sl]
            [sportsbook.football :refer :all]))

(def test-log [{:scope "goal" :game-part "H1" :team "home"}
               {:scope "goal" :game-part "H2" :team "away"}
               {:scope "corner" :game-part "full-time" :team "away"}])

(deftest match-winner-test 
  (testing "Match winner away"
    (is (= :lose  (:status  (sl/settle-selection away test-log {:game-part full-time :scope "point"})))))
  (testing "Match winner home"
    (is (= :lose  (:status (sl/settle-selection home test-log {:game-part full-time  :scope "point"})))))
  (testing "Match winner draw"
    (is (= :win  (:status (sl/settle-selection draw test-log {:game-part full-time :scope "point"})))))
  (testing "Match winner market"
    (is (= {:MARKET-PARAMS {:game-part #{"H1" "H2"}, :scope "goal"}, 
            :selections '({:name :home, :status :lose} 
                          {:name :draw, :status :win}
                          {:name :away, :status :lose}), 
            :name :match-winner, 
            :is-auto-cancel? true}
           (sl/settle match-winner test-log))))

)
