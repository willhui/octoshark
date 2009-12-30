(ns octoshark.math.vec
  (:use clojure.test))

(defn vec-len-sq
  "Returns the square of the geometric vector length."
  [v]
  (apply + (map #(* % %) v)))

(defn vec-len
  "Returns the geometric length of a vector. If you only need the squared
  length, use len-sq instead as it is faster to compute."
  [v]
  (Math/sqrt (vec-len-sq v)))

(defn normalize
  "Returns a unit vector pointing in the same direction as the input vector."
  [v]
  (let [len (vec-len v)]
    (vec (map #(/ % len) v))))

(defn dot
  "Computes the dot product of two vectors."
  [u v]
  (apply + (map * u v)))

(defn cross
  "Computes the cross product of two vectors."
  [[bx by bz] [cx cy cz]]
  [(- (* by cz) (* bz cy))
   (- (* bz cx) (* bx cz))
   (- (* bx cy) (* by cx))])

(deftest test-vec-len-sq
  (is (= (vec-len-sq [3 4]) 25)))

(deftest test-vec-len
  (is (= (vec-len [2 5]) (Math/sqrt 29))))

(deftest test-normalize-returns-vec
  (is (vector? (normalize [1 2 3]))))

(deftest test-normalize-output-len
  (is (= (vec-len (normalize [1 2 3])) 1)))

(deftest test-normalize-output-dir
  (is (= (normalize [0 6 0]) [0 1 0])))

(deftest test-normalize-output-unchanged
  (is (= (normalize [1 0]) [1 0])))

(deftest test-dot
  (is (= (dot [1 2 3] [3 2 1]) 10)))

(deftest test-cross
  (is (= (cross [3 -3 1] [4 9 2]) [-15 -2 39])))
