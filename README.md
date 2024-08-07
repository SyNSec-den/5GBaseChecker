# 5GBaseChecker

This is the public release of the code of 5GBaseChecker, a security analysis framework for the control plane protocols of 5G baseband.  

Based on this tool, we have the following USENIX Security '24 paper and BlackHat '24 talk: 

USENIX Security 2024 paper: "Logic Gone Astray: A Security Analysis Framework for the Control Plane Protocols of 5G Basebands" (USENIX'24). Link: https://www.usenix.org/conference/usenixsecurity24/presentation/tu

BlackHat 2024 Talk: https://www.blackhat.com/us-24/briefings/schedule/index.html#cracking-the-g-fortress-peering-into-gs-vulnerability-abyss-40620

**Table of Contents**

- [Introduction](#introduction)
- [Requirements](#requirements)
- [5GBaseChecker Overview](#5GBaseChecker-overview)
- [StateSynth](#StateSynth)
	- [Modified Cellular Stack](#modified-cellular-stack)
		- [Configuration Requirements](#configuration-requirements)
		- [Sim Card Requirements](#sim-card-requirements)
	- [Running Passive Learner](#running-passive-learner)
	- [Running State Learner](#running-state-learner)
- [DevScan](#devscan)
- [DevLyzer](#devlyzer)
	- [Install NuSMV](#install_nusmv)
	- [DevLyzer Component Overview](#devlyzer-component-overview)
	- [Run DevLyzer](#run_devlyzer)
- [Acknowledgement](#acknowledgement)
- [License](#license)

# Introduction
5GBaseChecker is an efficient, scalable, and dynamic security analysis framework based on differential testing for analyzing 5G basebands’ control plane protocol interactions. It captures basebands’ protocol behaviors as finite state machines (FSMs) through black-box automata learning, identifies input sequences for which the extracted FSMs provide deviating outputs, and leverages these deviations to identify security properties from specifications to triage if the deviations found in 5G basebands violate any properties.

# Requirements

- Ubuntu 20.04 (tested OS)  
- USRP B210 (tested SDR)
- sysmoISIM-SJA2 SIM Card
- adb
- graphviz
- jdk 11
- maven
- python 2
- python 3
- pydot
- MongoDB 4.4
- NuSMV 2.6.0 


# 5GBaseChecker Overview  

5GBaseChecker has three primary components, namely, StateSynth, DevScan, and DevLyzer. The StateSynth module employs a hybrid and collaborative automata learning approach to extract finite state machines (FSMs) from 5G baseband implementations. DevScan then compares these FSMs to identify deviations between the FSMs constructed by StateSynth. DevLyzer further triages these deviations to determine security properties and identify potential vulnerabilities. Figure 1 illustrates the workflow of 5GBaseChecker.


| ![overview](https://github.com/helloTkk/5gbasechecker_preview/assets/43031500/4d20179b-b330-4943-bfaa-6a0c6cf049ff) | 
|:--:| 
| *Figure 1: Workflow of 5GBaseChecker* |


# StateSynth

The StateSynth module consists of State learner, and a modified cellular stack (OAI gNodeB, srsRAN gNodeB and Open5GS as Core Network). It can be utilized to construct FSM for 5G UE devices. We provide instructions on how to build and run below. 



## Modified Cellular Stack  

The modified cellular stack will convert the queries from state learner to concrete packets and send it to the UE.

You can find detailed instructions to install Open5GS here: [building-open5gs-from-sources](https://open5gs.org/open5gs/docs/guide/02-building-open5gs-from-sources/). Please note that we are using MongoDB 4.4. 

- Build Open5GS
```bash
cd "StateSynth/modified_cellular_stack/5GBaseChecker_Core"
meson build --prefix=`pwd`/install
ninja -C build
```

You can find detailed instructions to install OAI here: [NR_SA_Tutorial_OAI](https://gitlab.eurecom.fr/oai/openairinterface5g/-/blob/develop/doc/NR_SA_Tutorial_OAI_nrUE.md#32-build-oai-gnb-and-oai-nrue).

- Building OAI gNodeB
```bash
cd "StateSynth/modified_cellular_stack/5GBaseChecker_OAI_gnb/cmake_targets"
sudo ./build_oai  -w USRP -C --gNB --ninja 
```

You can find detailed instructions to install srsRAN here: [srsRAN document](https://docs.srsran.com/projects/4g/en/latest/).

- Building srsRAN gNodeB
```bash
cd "StateSynth/modified_cellular_stack/5GBaseChecker_srs_gnb"
mkdir build && cd build
cmake ..
make srsenb -j4
```


### Configuration Requirements  
A set of modified configurations are given at `StateSynth/modified_cellular_stack/conf`.

### Sim Card Requirements  
It's possible that the commercial UE doesn't search any 5G gNodeB and initiates the connection due to SIM card. We followed this [link](https://gist.github.com/mrlnc/01d6300f1904f154d969ff205136b753) to program our SIM card and it works.

## Running Passive learner 
<!-- ([Trace2Model](https://github.com/natasha-jeppu/Trace2Model)) -->

First, install the dependencies for Trace2Model. You can find the detail instructions [here](https://github.com/natasha-jeppu/Trace2Model). 

To run Passive learner and extract potential counter-examples(PCEs), execute the following commands: 
```
cd 5GBaseChecker/StateSynth/Trace2Model
python3 learn_model.py -i /5GBaseChecker/StateSynth/Trace2Model/benchmarks/TelAviv/example.txt --incr --dfa -w 3
python3 ./DotProcess.py 
cp 5GBaseChecker/StateSynth/Trace2Model/FSM2.dot 5GBaseChecker/StateSynth/fsm_comparator/FSM
python2.7 ./iterative_checker.py -lts1 FSM/FSM1.dot -lts2 FSM/FSM2.dot -o FSM1_vs_FSM2
python3 ./CEextract.py
```



## Running State Learner 

Please update scripts under the directory `StateSynth/5GBaseChecker_Statelearner` according to your machine configuration before you run the State Learner. 

To run the state learner, you can execute the following commands: 
```bash
java -jar StateSynth/5GBaseChecker_Statelearner/out/artifacts/5GBaseChecker_Statelearner_jar/5GBaseChecker_Statelearner.jar fgue.properties
```

To change input symbols or other learning parameters, e.g., device name, max depth, etc., please change `StateSynth/5GBaseChecker_Statelearner/fgue.properties` accordingly.  

You can put potential counter-examples (PCEs) extracted the learning phase of other phones to `StateSynth/5GBaseChecker_Statelearner/CEStore/input`, this could help accelerate the FSM learning procedure. 
<!-- We have put some PCEs in `StateSynth/5GBaseChecker_Statelearner/CEStore/input` which are extracted by us. -->

# DevScan 

DevScan takes a set of FSMs constructed by StateSynth as inputs and provides the deviating behavior inducing message sequences. We have included two sample FSMs for demonstrating the use of the component. It can be run with the following commands:

```bash
cd "DevScan/fsm_checking/fsm_equivalence_checker_5GBaseChecker"
python3 Autorun.py
```
After this command, the result will be stored in the `DevScan/fsm_checking/fsm_equivalence_checker_5GBaseChecker/Result` folder. Then execute the following command to summarize the result.
```bash
python3 AutoAnalysis.py
```

After executing the above command, you will get an output file `deviant-queries.json`. This will be the input for DevLyzer.

# DevLyzer 

## Install NuSMV

You can download NuSMV from [here](https://nusmv.fbk.eu/downloads.html).

To add NuSMV to your PATH environment variable, you need to edit your `~/.bashrc` file. 


1. Open the `~/.bashrc` file in a text editor.
   `nano ~/.bashrc`.
2. Add the path to NuSMV to your PATH variable:
   Find the directory where NuSMV is installed. Let's assume it is installed in `/usr/local/NuSMV/bin`. Add the following line at the end of your `~/.bashrc` file:
   `export PATH=$PATH:/usr/local/NuSMV/bin`.
3. Save and close the file.
4. Source the `~/.bashrc` file to apply the changes. `source ~/.bashrc`
5. Verify the changes. Check if NuSMV is accessible by typing: `NuSMV --version`.
   This should display the version of NuSMV, confirming that it is correctly added to your PATH.


## DevLyzer Component Overview

DevLyzer has an `attack_trace_checker` folder. 
All the input and output alphabets are placed in the `all_input.txt` and `all_output.txt` files respectively. The 
LTL properties are placed in the `ltl_properties.txt` file.
The main python script for DevLyzer is `main.py`. 
The `deviant_queries.json` contains all the deviations among devices output from DevScan.


## Run DevLyzer


Run the main.py file. 

```bash
cd DevLyzer
python3 ./main.py
```

The console will show (total number of deviations analyzed) / (total number of deviations provided). It will also display the input sequence of unresolved deviations (if any). Our provided deviations list and program takes ~20 minutes to run on our machine. 



# Acknowledgement

We thank [Open5GS](https://open5gs.org/), [OpenAirInterface](https://openairinterface.org/), [srsRAN](https://www.srsran.com/), [Trace2Model](https://github.com/natasha-jeppu/Trace2Model) and [StateLearner](https://github.com/jderuiter/statelearner) developers for making their tools publicly available. 5GBaseChecker modifies these tools to implement the FSM Inference Module.  


# License
This work is licensed under the GNU Affero General Public License ([GNU AGPL v3.0](https://www.gnu.org/licenses/agpl-3.0.html)). Please refer to the [license file](LICENSE) for details.


