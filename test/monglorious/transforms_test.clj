(ns monglorious.transforms-test
  (:require [monglorious.core :refer :all]
            [monger.core :as mg]
            [monger.collection :as mc])
  (:use midje.sweet))

;; Test run-command-transform()

(defn check-ok
  [expected response] (= expected (get response "ok")))

(defn is-ok?
  "Checks that a MongoDB response has ok: 1.0"
  [response] (partial check-ok 1.0))

(defn is-not-ok?
  "Checks that a MongoDB response has ok: 0.0"
  [response]
  [response] (partial check-ok 0.0))

;; Create some sample data before tests, delete after
(against-background
  [(before :contents
           (let [conn (mg/connect)
                 db (mg/get-db conn "testdb")]
             (mc/insert-batch db "documents" [{:name "Alan" :age 27 :score 17772}
                                              {:name "Joe" :age 32 :score 8277}
                                              {:name "Macy" :age 29 :score 8837777}])))
   (after :contents
          (let [conn (mg/connect)
                db (mg/get-db conn "testdb")]
            (monger.collection/drop db "documents")))]

  ;; Facts
  (fact "Monglorious executes serverStatus"
        (execute {} "testdb" "db.runCommand('serverStatus')") => map?)

  (fact "Monglorious executes dbStats"
        (execute {} "testdb" "db.runCommand('dbStats')") => map?)

  (fact "Monglorious executes collStats"
        (execute {} "testdb" "db.runCommand({collStats: 'unknownCollection'})") => is-not-ok?
        (execute {} "testdb" "db.runCommand({collStats: 'documents'})") => is-ok?
        (execute {} "testdb" "db.runCommand({'collStats': 'documents'})") => is-ok?
        (execute {} "testdb" "db.runCommand({\"collStats\": \"documents\"})") => is-ok?))

