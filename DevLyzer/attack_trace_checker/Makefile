all:
	# @rm -rf AC_cleaned.smv
	# @cat module.smv main.smv property.smv assign_nf_config.smv protocol.smv\
	# 	assign_consumer.smv assign_nfd.smv assign_producer.smv assign_pac.smv\
	# 	assign_at.smv assign_atr.smv assign_sr.smv\
	# 	 >> AC.smv
	NuSMV -source script.txt > result.txt
	$(info Result has been outputed to result.txt)
clean:
	@rm -rf result.txt