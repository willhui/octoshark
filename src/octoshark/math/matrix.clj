(ns octoshark.math.matrix
  (:use octoshark.math.vec)
  (:use clojure.test))

(defn- matrix-row
  "Returns row i of the given matrix."
  [m i]
  (let [start (* 4 i)]
    (subvec m start (+ start 4))))

(defn- matrix-col
  "Returns column i of the given matrix."
  [m i]
  (for [row (range 4)] (m (+ (* 4 row) i))))

(defn matrix-mul
  "Computes the ordinary matrix product of two 4x4 matrices."
  [a b]
  (vec (for [i (range 4) j (range 4)]
	 (dot (matrix-row a i) (matrix-col b j)))))

(defn matrix-apply
  "Applies a matrix transform to a given 4-vector."
  [m x]
  (vec (for [i (range 4)]
	 (dot (matrix-row m i) x))))

(def identity-matrix
     [1 0 0 0
      0 1 0 0
      0 0 1 0
      0 0 0 1])

(defn translation-matrix
  "Defines a 4x4 translation matrix."
  [x y z]
  [1 0 0 x
   0 1 0 y
   0 0 1 z
   0 0 0 1])

(defn scaling-matrix
  "Defines a 4x4 scaling matrix."
  [x y z]
  [x 0 0 0
   0 y 0 0
   0 0 z 0
   0 0 0 1])

(defn rotation-matrix-x
  "Defines a 4x4 rotation matrix about the x-axis."
  [angle]
  (let [c (Math/cos angle) s (Math/sin angle)]
    [1 0 0 0
     0 c (- s) 0
     0 s c 0
     0 0 0 1]))

(defn rotation-matrix-y
  "Defines a 4x4 rotation matrix about the y-axis."
  [angle]
  (let [c (Math/cos angle) s (Math/sin angle)]
    [c 0 (- s) 0
     0 1 0 0
     (- s) 0 c 0
     0 0 0 1]))

(defn rotation-matrix-z
  "Defines a 4x4 rotation matrix about the z-axis."
  [angle]
  (let [c (Math/cos angle) s (Math/sin angle)]
    [c (- s) 0 0
     s c 0 0
     0 0 1 0
     0 0 0 1]))

(defn rotation-matrix
  "Defines a 4x4 rotation matrix about the x, y, and z axes (applied in that order)."
  [angle-x angle-y angle-z]
  (matrix-mul (rotation-matrix-z angle-z)
	      (matrix-mul (rotation-matrix-y angle-y)
			  (rotation-matrix-x angle-x))))

(deftest test-matrix-mul-returns-vec
  (is (vector? (matrix-mul identity-matrix identity-matrix))))

(deftest test-matrix-mul-identity
  (let [u [2 3 7 0
	   3 6 -3 -4
	   -8 9 3 3
	   4 -2 1 6]]
    (is (= (matrix-mul u identity-matrix)
	   u))))

(deftest test-matrix-mul
  (let [u [2 3 7 0
	   3 6 -3 -4
	   -8 9 3 3
	   4 -2 1 6]
	v [6 3 -8 2
	   9 -1 11 -5
	   4 0 -3 1
	   5 7 8 6]]
    (is (= (matrix-mul u v)
	   [67 3 -4 -4
	    40 -25 19 -51
	    60 -12 178 -40
	    40 56 -9 55]))))