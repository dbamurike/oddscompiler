(ns sportsbook.football-test
  (:require [clojure.test :refer :all]
            [sportsbook.slang :as sl]
            [sportsbook.football :refer :all]))

(def test-log [{:scope "goal" :game-part "H1" :team "home"}
               {:scope "goal" :game-part "H2" :team "away"}
               {:scope "corner" :game-part "H1" :team "away"}])

(deftest match-winner-test 
  (testing "Match winner market"
    (is (= {:MARKET-PARAMS {:game-part #{"H1" "H2"}, :scope "goal"}, 
            :selections '({:name :home, :status :lose} 
                          {:name :draw, :status :win}
                          {:name :away, :status :lose}), 
            :name :match-winner, 
            :is-auto-cancel? true}
           (sl/settle match-winner test-log)))))

(deftest match-total-cancel
  (testing "Under lose"
    (is (= :lose (:status (sl/settle-selection under test-log {:total 2, :game-part full-time, :scope "goal"})))))
  (testing "Match total 2 with auto cancel"
    (is (= {:MARKET-PARAMS {:total 2, :game-part full-time, :scope "goal"}, 
            :selections '({:name :over, :status :cancel}
                          {:name :under, :status :cancel}), 
            :name :total-2-goals, 
            :is-auto-cancel? true}
           (sl/settle total-2-goals test-log)))))
