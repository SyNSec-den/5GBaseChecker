all:
	gcc \
	-std=c99 \
	-pedantic \
	-I/usr/include \
	-I/usr/lib/jvm/java-8-openjdk/include/ \
	-I/usr/lib/jvm/java-8-openjdk/include/linux \
	-fPIC \
	-O2 \
	-shared \
	net_automatalib_commons_util_lib_NativeGreeter.c \
	-o libautomata_greeter.so
