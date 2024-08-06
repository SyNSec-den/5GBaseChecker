(set-logic LIA)
(synth-fun next ((inp.temp Int) (rangeLow Int) (rangeHigh Int) (desiredTemperature Int) (allowedError Int) (out.coolingTimer Int) (coolingTimeout Int) ) Int
	((Start Int)  (Var Int) (StartBool Bool))
	((Start Int (
				 0
				 1
				 (ite StartBool Start Start)))

	(Var Int (
			 inp.temp
			 rangeLow
			 rangeHigh
			 desiredTemperature
			 allowedError
			 out.coolingTimer
			 coolingTimeout
				(abs Var)						
			 	(+ Var Var)						
			 	(- Var Var)						
			 	(* Var Var)))

	 (StartBool Bool (
					 (> Var Var)						
					 (>= Var Var)						
					 (< Var Var)						
					 (<= Var Var)						
					 (= Var Var)						
					 (and StartBool StartBool)			
					 (or  StartBool StartBool)				
					 (not StartBool)))))

(constraint (= (next 92 0 100 50 10 0 3 ) 0))
(constraint (= (next 1073741870 0 100 50 10 1 3 ) 0))
(constraint (= (next 1 0 100 50 10 2 3 ) 0))
(constraint (= (next 40 0 100 50 10 3 3 ) 0))
(constraint (= (next 40 0 100 50 10 4 3 ) 1))
(constraint (= (next 1 0 100 50 10 0 3 ) 0))
(constraint (= (next 60 0 100 50 10 2 3 ) 0))
(constraint (= (next 16777276 0 100 50 10 2 3 ) 0))
(constraint (= (next 2097192 0 100 50 10 3 3 ) 0))
(constraint (= (next (- 8) 0 100 50 10 0 3 ) 0))
(constraint (= (next 16777276 0 100 50 10 0 3 ) 0))
(constraint (= (next (- 2147483648) 0 100 50 10 1 3 ) 0))
(constraint (= (next 60 0 100 50 10 0 3 ) 0))
(constraint (= (next 56 0 100 50 10 1 3 ) 0))
(constraint (= (next 46 0 100 50 10 2 3 ) 0))
(constraint (= (next 56 0 100 50 10 3 3 ) 0))
(constraint (= (next 42 0 100 50 10 4 3 ) 0))
(constraint (= (next 0 0 100 50 10 5 3 ) 1))
(constraint (= (next (- 40) 0 100 50 10 0 3 ) 0))
(constraint (= (next 1073741852 0 100 50 10 0 3 ) 0))
(constraint (= (next 15 0 100 50 10 1 3 ) 0))
(constraint (= (next 61 0 100 50 10 2 3 ) 0))
(constraint (= (next 134217782 0 100 50 10 3 3 ) 0))
(constraint (= (next 62 0 100 50 10 4 3 ) 0))
(constraint (= (next (- 2147483648) 0 100 50 10 5 3 ) 0))
(constraint (= (next 1 0 100 50 10 1 3 ) 0))
(constraint (= (next (- 8) 0 100 50 10 2 3 ) 0))
(constraint (= (next 37 0 100 50 10 2 3 ) 0))
(constraint (= (next (- 2147483548) 0 100 50 10 3 3 ) 0))
(constraint (= (next (- 8) 0 100 50 10 1 3 ) 0))

(check-synth)
