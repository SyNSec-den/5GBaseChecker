# Trace2Model
A framework to learn concise system models from system execution traces. <br/>
Based on work presented in 'Learning Concise Models from Long Execution Traces'

* N. Y. Jeppu, T. Melham, D. Kroening and J. O’Leary, "Learning Concise Models from Long Execution Traces," 2020 57th ACM/IEEE Design Automation Conference (DAC), San Francisco, CA, USA, 2020, pp. 1-6, doi: 10.1109/DAC18072.2020.9218613.[[PDF]](https://arxiv.org/pdf/2001.05230.pdf)


## Usage
The modules available in this framework are divided into two categories:
1. Automata learning modules - generate automata from given trace input <br/>
  a. Incremental model learning: `learn_model.py` <br/>
  
~~~
usage: learn_model.py [-h] -i INPUT_FILENAME [-w SLIDING_WINDOW] [-n NUM_STATES] [--dfa [DFA]]
                      [-t TARGET_PATH] [-o TRACE_ORDER] [--incr [INCREMENTAL_SAT]]
                      [--trace-stats [TRACE_STATS]]

optional arguments:
  -h, --help            show this help message and exit
  -w SLIDING_WINDOW, --window SLIDING_WINDOW
                        Sliding window size. Default = 3
  -n NUM_STATES, --num_states NUM_STATES
                        Number of states. Default = 2
  --dfa [DFA]           Generate a DFA. Generates NFA by default.
  -t TARGET_PATH, --target TARGET_PATH
                        Target model path. Default =
                        /Users/natppu/Documents/Research/Trace2model_new/Tool/models
  -o TRACE_ORDER, --order TRACE_ORDER
                        Trace order. Available options stb, bts, same, random. Default = same
  --incr [INCREMENTAL_SAT]
                        Incremental model learning
  --trace-stats [TRACE_STATS]
                        Print trace statistics

required arguments:
  -i INPUT_FILENAME, --input_file INPUT_FILENAME
                        Input trace file for model learning
~~~

2. Transition expression synthesis modules - generate a sequence of transition predicates from raw trace data, to be used as input to automata learning modules in 1. <br/>
  a. Synthesize expressions that will serve as transition conditions for next event: `syn_next_event.py` <br/>
  
~~~
usage: syn_next_event.py [-h] -i INPUT_FILENAME
                         [-dv EVENT_NAME DEPENDENT_VARIABLE_LIST [EVENT_NAME DEPENDENT_VARIABLE_LIST ...]]
                         [-c GRAMMAR_CONST [GRAMMAR_CONST ...]]

optional arguments:
  -h, --help            show this help message and exit
  -dv EVENT_NAME DEPENDENT_VARIABLE_LIST [EVENT_NAME DEPENDENT_VARIABLE_LIST ...], --dvar_list EVENT_NAME DEPENDENT_VARIABLE_LIST [EVENT_NAME DEPENDENT_VARIABLE_LIST ...]
                        Variables that affect update variable behaviour. Use '-dv help' for possible
                        options. Use -dv [all] <var_list> to set variables for all events
  -c GRAMMAR_CONST [GRAMMAR_CONST ...], --const GRAMMAR_CONST [GRAMMAR_CONST ...]
                        Constants to be added to grammar for SyGus CVC4. By default uses average and
                        std dev values of the constants in trace. Use "nil" to not use any
                        constants.

required arguments:
  -i INPUT_FILENAME, --input_file INPUT_FILENAME
                        Input trace file for data update predicate generation
~~~

  b. Synthesize expressions that will serve as transition predicates for data update across transitions: `syn_event_update.py` <br/>
  
~~~
usage: syn_event_update.py [-h] -i INPUT_FILENAME -v UPDATE_VAR
                           [-dv EVENT_NAME DEPENDENT_VARIABLE_LIST [EVENT_NAME DEPENDENT_VARIABLE_LIST ...]]
                           [-c GRAMMAR_CONST [GRAMMAR_CONST ...]]

optional arguments:
  -h, --help            show this help message and exit
  -dv EVENT_NAME DEPENDENT_VARIABLE_LIST [EVENT_NAME DEPENDENT_VARIABLE_LIST ...], --dvar_list EVENT_NAME DEPENDENT_VARIABLE_LIST [EVENT_NAME DEPENDENT_VARIABLE_LIST ...]
                        Variables that affect update variable behaviour. Use '-dv help' for possible
                        options. Use -dv [all] <var_list> to set variables for all events
  -c GRAMMAR_CONST [GRAMMAR_CONST ...], --const GRAMMAR_CONST [GRAMMAR_CONST ...]
                        Constants to be added to grammar for SyGus CVC4

required arguments:
  -i INPUT_FILENAME, --input_file INPUT_FILENAME
                        Input trace file for data update predicate generation
  -v UPDATE_VAR, --var UPDATE_VAR
                        Variable for data update predicate synthesis. Use '-v help' for possible
                        options
~~~

For the synthesis modules 2a and 2b, a new trace file `<input_filename>_events.txt` is created with a sequence of transition predicates. Use this as input to the model learning modules 1a.
You can use `run.py` to run a set of benchmarks already present in the tool. See the `benchmarks` folder.

## Tool Dependencies
You'll need the following tools installed (installation instructions for Fedora 29, Ubuntu 18.04 and MacOS 10.15 are provided below):
1. [`CVC4 v1.8`](https://github.com/CVC4/CVC4/releases/tag/1.8)
2. [`CBMC v5.11 (cbmc-5.11-1062-g25ba4e6a6)`](https://github.com/diffblue/cbmc)

## Benchmarks
There are a few trace files already provided to play around with the tool in the `benchmarks` folder
1. `SoC` : benchmarks from the SoC domain. Traces are generated using the QEMU virtual platform.
2. `AWS` : benchmarks from AWS IoT. Traces are obtained from C implementations of AWS IoT Events Detector Models. 
3. `TelAviv` : log data from a variety of domains, including tcp and ssh protocols and JAVA APIs.
4. `Predicate_Synth` : benchmarks for predicate synthesis.

## Setup Instructions

### Fedora (tested for Fedora 29)

1. Install Basic Requirements
~~~
  dnf -y groupinstall "Development Tools"
  dnf -y install kernel-devel kernel-headers
  dnf -y install gcc gcc-c++ flex bison perl-libwww-perl patch git wget
~~~

2. Install Tool Dependencies
~~~
  dnf -y install python3
~~~

Building cvc4
~~~
  wget https://github.com/CVC4/CVC4/releases/download/1.8/cvc4-1.8-x86_64-linux-opt
  chmod +x cvc4-1.8-x86_64-linux-opt
  ln -s ./cvc4-1.8-x86_64-linux-opt cvc4
  export PATH=$PATH:$(pwd)
~~~

Building CBMC
~~~
  git clone https://github.com/diffblue/cbmc.git
  cd cbmc
  git reset --hard 25ba4e6a600b033df7dbaf3d19437afd8b9bdd1c
  make -C src minisat2-download
  make -C src
  export PATH=$PATH:$(pwd)/src/cbmc
~~~

3. Python Modules
~~~
  dnf -y install graphviz graphviz-devel pkg-config python3-devel redhat-rpm-config
  pip3 install numpy pygraphviz transitions termcolor
~~~

4. Clone the repository `Trace2Model`<br/>
~~~
  git clone https://github.com/natasha-jeppu/Trace2Model.git Trace2Model
~~~

5. Verify cvc4 installation
~~~
  cd Trace2Model
  cvc4 aux_files/gen_event.sl
  cvc4 aux_files/gen_event_update_enum.sl
~~~

6. Run example benchmarks<br/>
~~~
  cd Trace2Model
  python3 run.py -gen_o incr -mt dfa -syn guard
~~~


### Ubuntu 18.04

1. Install Basic Requirements
~~~
  apt-get -y install build-essential
  apt-get -y install g++ gcc flex bison make git libwww-perl patch wget
~~~

2. Install Tool Dependencies
~~~
  apt-get -y install python3
~~~

Building cvc4
~~~
  wget https://github.com/CVC4/CVC4/releases/download/1.8/cvc4-1.8-x86_64-linux-opt
  chmod +x cvc4-1.8-x86_64-linux-opt
  ln -s ./cvc4-1.8-x86_64-linux-opt cvc4
  export PATH=$PATH:$(pwd)
~~~

Building CBMC
~~~
  git clone https://github.com/diffblue/cbmc.git
  cd cbmc
  git reset --hard 25ba4e6a600b033df7dbaf3d19437afd8b9bdd1c
  make -C src minisat2-download
  make -C src
  export PATH=$PATH:$(pwd)/src/cbmc
~~~

3. Python Modules
~~~
  apt-get -y install graphviz libgraphviz-dev pkg-config python3-pip python3-setuptools
  pip3 install numpy pygraphviz transitions termcolor
~~~

4. Clone the repository `Trace2Model`<br/>
~~~
  git clone https://github.com/natasha-jeppu/Trace2Model.git Trace2Model
~~~

5. Verify cvc4 installation
~~~
  cd Trace2Model
  cvc4 aux_files/gen_event.sl
  cvc4 aux_files/gen_event_update_enum.sl
~~~

6. Run example benchmarks<br/>
~~~
  cd Trace2Model
  python3 run.py -gen_o incr -mt dfa -syn guard
~~~




### MacOS (tested on MacOS 10.15)
You will need Homebrew for installation. You can install it from https://brew.sh. <br/>
1. Python Modules
~~~
  brew install graphviz
  pip3 install graphviz
  pip3 install pygraphviz
  pip3 install numpy transitions termcolor
~~~

2. Install Tool Dependencies
Building cvc4
~~~
  wget https://github.com/CVC4/CVC4/releases/download/1.8/cvc4-1.8-macos-opt
  chmod +x cvc4-1.8-macos-opt
  ln -s ./cvc4-1.8-macos-opt cvc4
  export PATH=$PATH:$(pwd)
~~~

  Building CBMC<br/>
  Install Xcode 10.x for CBMC build
~~~
  git clone https://github.com/diffblue/cbmc.git
  cd cbmc
  git reset --hard 25ba4e6a600b033df7dbaf3d19437afd8b9bdd1c
  make -C src minisat2-download
  make -C src
  export PATH=$PATH:$(pwd)/src/cbmc
~~~


3. Clone the repository `Trace2Model`<br/>
~~~
  git clone https://github.com/natasha-jeppu/Trace2Model.git Trace2Model
~~~

4. Verify cvc4 installation
~~~
  cd Trace2Model
  cvc4 aux_files/gen_event.sl
  cvc4 aux_files/gen_event_update_enum.sl
~~~

5. Run example benchmarks<br/>
~~~
  cd Trace2Model
  python3 run.py -gen_o incr -mt dfa -syn guard
~~~

