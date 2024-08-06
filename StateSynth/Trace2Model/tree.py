import re
import numpy as np

class Node:

	def __init__(self, cond_expr):
		self.left = None
		self.right = None
		self.cond = cond_expr

	def insert(self, smt_expr, expr, cond, arg1, arg2):
		if not smt_expr:
			return

		expr_ind = expr.index(smt_expr)

		if '(ite' in arg1[expr_ind]:
			cond1_ind = expr.index(arg1[expr_ind])
		else:
			cond1_ind = -1

		if '(ite' in arg2[expr_ind]:
			cond2_ind = expr.index(arg2[expr_ind])
		else:
			cond2_ind = -1

		if cond1_ind == -1:
			event = re.findall('[0-9]+', arg1[expr_ind])[0]
			self.left = Node(event)
			self.left.insert(None, expr, cond, arg1, arg2)
		else:
			self.left = Node(cond[cond1_ind])
			self.left.insert(expr[cond1_ind], expr, cond, arg1, arg2)

		if cond2_ind == -1:
			event = re.findall('[0-9]+', arg2[expr_ind])[0]
			self.right = Node(event)
			self.right.insert(None, expr, cond, arg1, arg2)
		else:
			self.right = Node(cond[cond2_ind])
			self.right.insert(expr[cond2_ind], expr, cond, arg1, arg2)

	def PrintTree(self, temp, expressions):
		if self.left:
			temp.append(self.cond)
			self.left.PrintTree(temp, expressions)
		
		if self.right:
			temp.append('!'+self.cond)
			self.right.PrintTree(temp, expressions)

		if not self.right and not self.left:
			temp_t = [self.cond]
			[temp_t.append(x) for x in temp]
			expressions.append(temp_t)
			if temp:
				c = temp.pop()
			else:
				c = None

			while( '!' in c and temp):
				c = temp.pop()

