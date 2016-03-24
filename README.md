# Log2Model
Framework for generating models from log files.

## Installation 
Install prism (can be downloaded here: [PRISM Website](http://www.prismmodelchecker.org/download.php)). Copy prism.jar (located in the `prism/lib` folder) into the `lib` folder:
`

Download [grappa](http://www2.research.att.com/~john/Grappa/grappa.tgz) (for prettyprinting), and copy the jar into the `lib` folder.
Install [JUppaal](https://github.com/ksluckow/JUppaal) (for translation to TA), and copy the jar into the `lib` folder.

Then, in the root directory of Model-inferencer, install it:
```
#!bash
$ gradle build
```

You can also get a fat jar with all dependencies:
```
#!bash
$ gradle fatjar
```

You have to build the fatjar in order to use the supplied wrapper scripts (`log2model` and `log2chart` and ).

## Usage 

`log2model` can be interfaced from the command line. Execute
```
#!bash
$ ./log2model
```
for usage. 


## Additional Tools
You can also invoke the charting tool in a similar fashioner. Just execute:
```
#!bash
$ ./log2chart
```

The `./tools` directory contains additional tools that aid in understanding system behaviour. 
One is the `animator.py` tool that makes a real-time playback of the positions over time of the vehicle (drawn as lines on the grid). Currently this is a prototype likely to be extended.

Execute `python animator.py data.csv` to make the playback.

Here `data.csv`is a file with comma-separated x and y values.
`STCoordOutputter` can generate the `data.csv` for `animator.py`

## TODO ##
* make installation script
* refactorings
* Add notion of weighted (or probabilistc) transition, and nondeterministic transitions. Update visitor accordingly.
* Fix how model variables are added and treatment of state variables. This should be the same...
* Visitor relies on a visited variable being set in already visited state. This is ugly.
* Fix naming of many of the classes
* Fix ArrDepGridState -- shouldn't be composed of two gridstates
