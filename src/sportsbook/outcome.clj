(ns sportsbook.outcome)

(def test-log [
               {:scope "goal"
                :team "team-1"
                :time "0:26"
                :game-part "half-1"},
               {:scope "goal"
                :team "team-2"
                :time "0:60"
                :game-part "half-2"}])


(defn amount [pred log]
  (->> log
       (filter pred)
       count))

(defn goal? [log-entity]
  (= "goal" (:scope log-entry)))

(defn team-1? [log-entity]
  (= "team-1" (:team log-entry)))

(defn team-2? [log-entity]
  (= "team-2" (:team log-entry)))

(def full-time ["half-1" "half-2"])

(defn full-time? [log-entry]
  (or (= "half1" (:game-part log-entry)) 
      (= "half2" (:game-part log-entry))))

(defn finished? [game-part logs]
  (= :finished (filter  #(= :finished (:status logs)))))

(defoutcome home
  {:vars  
   [:t1 (and  (goal? :scope ) 
              (team-1? :team)
              (full-time? :game-part))
    :t2 (and  (goal? :scope) 
              (team2? :team)
              (full-time? :game-part))
    :t1-goals (amount :t1 log)
    :t2-goals (amount :t2 log)]}

  :to-settle? (finished? full-time)
  :settle (> :t1-goals :t2-goals)
  )

(defoutcome over-1.5
  :filter (and  (goal? :scope ) 
                (full-time? :game-part)) 
  :goals (amount :filter log)

  :to-settle? (or (finished? full-time) :settle)
  :settle (> over-goals 1.5)
  )

(defoutcome under-1.5
  :filter (and  (goal? :scope ) 
                (full-time? :game-part)) 
  :goals (amount :filter log)

  :to-settle? (finished? full-time) 
  :settle (> under-goals 1.5)
  )





