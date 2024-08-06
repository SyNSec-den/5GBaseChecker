(set-logic LIA)

(synth-fun next ((ip Int) (op Int) (upper_t Int) (lower_t Int)) Int

	((Start Int) (StartBool Bool))

	((Start Int (
		ip
		op
		upper_t
		lower_t
		0
		(+ Start Start)						
		(- Start Start)						
		(* Start Start)
		(ite StartBool Start Start)))

	 (StartBool Bool (
		(> Start Start)						
		(>= Start Start)						
		(< Start Start)						
		(<= Start Start)						
		(= Start Start)						
		(and StartBool StartBool)			
		(or  StartBool StartBool)				
		(not StartBool)))

))

(constraint (= (next (- 1) 5 5 (- 5) ) 4))
(constraint (= (next (- 1) 4 5 (- 5) ) 3))
(constraint (= (next (- 1) 3 5 (- 5) ) 2))
(constraint (= (next (- 1) 2 5 (- 5) ) 1))

(check-synth)
