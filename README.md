# Log2Model
Framework for generating models from log files.

## Installation 
Install prism (can be downloaded here: [PRISM Website](http://www.prismmodelchecker.org/download.php)). Copy prism.jar (located in the `prism/lib` folder) into the `lib` folder:
`

Download [grappa](http://www2.research.att.com/~john/Grappa/grappa.tgz) (for prettyprinting), and copy the jar into the `lib` folder.
Install [JUppaal](https://github.com/ksluckow/JUppaal) (for translation to TA), and copy the jar into the `lib` folder.

To build `Log2model`:
```bash
$ gradle build
```
You can also build a distribution that generates start scripts etc:
```bash
$ gradle installDist
```
The main entry point of `Log2model` can then be invoked: 
```bash
$ ./build/install/log2model/bin/log2model
```

## Additional Tools
You can also invoke the charting tool in a similar fashioner. Just execute:
```bash
$ ./log2chart
```

The `./tools` directory contains additional tools that aid in understanding system behaviour. 
One is the `animator.py` tool that makes a real-time playback of the positions over time of the vehicle (drawn as lines on the grid). Currently this is a prototype likely to be extended.

Execute `python animator.py data.csv` to make the playback.

Here `data.csv`is a file with comma-separated x and y values.
`STCoordOutputter` can generate the `data.csv` for `animator.py`

