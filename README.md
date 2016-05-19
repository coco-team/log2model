# Log2Model
`Log2Model` is a framework for reasoning on behaviors in log files.

## Installation 

### PRISM
Install prism (can be downloaded here: [PRISM Website](http://www.prismmodelchecker.org/download.php)). Unpack the tar and install `prism.jar`:

```bash
$ cd /path/to/prism
$ cd lib
$ mvn install:install-file -Dfile=prism.jar -DgroupId=prism -DartifactId=prismlib -Dversion=4.3 -Dpackaging=jar
```

Replace the version name `4.3` in `-Dversion` with the one you downloaded. `Log2Model` currently depends on `4.3+`.


### JUppaal
Install [JUppaal](https://github.com/ksluckow/juppaal) (for translation to TA):
```bash
$ git clone https://github.com/ksluckow/juppaal
$ cd juppaal
$ gradle install
```

You should now be ready to install `Log2Model`.

### Log2Model
If you don't have Gradle installed, use the supplied Gradle wrapper scripts `gradlew` (Linux/Mac) and `gradlew.bat` (Windows) in the following.

To build `Log2model`:
```bash
$ gradle build
```
You can also build a distribution that generates start scripts etc:
```bash
$ gradle installDist
```
The main entry point of `Log2model` can then be invoked by the wrapper script: 
```bash
$ ./runner.sh
```

## Examples
The following shows example usages based on a SafeTugs log.

The main tool of `Log2Model` is generating probabilistic models from the behaviors extracted from log files. To generate a `PRISM` model, execute:
```bash
$ ./runner.sh -input examples/st_example.log -type st -tool model -m prism -o ./ -v -dim 2x2
```
This will generate the `PRISM` file `model.prism`. You can optionally leave out the `-v` switch to not produce a visualization of the DTMC. The prettyprint will be output in the same destionation as the argument of the `-o` switch. The `dim` option (SafeTugs specific) specifies the dimensions of the grid projected on top of the airfield.

In addition, `Log2Model` has a number of additional tools:
To perform state mining and visualize the results:
```bash
$ ./runner.sh -input examples/st_example.log -type st -tool eventclass -field speed -flightname FL1
```

The event classes can be viewed in the log file `logs/app.log`. The can also be output to stdout---just change the `log4j2.xml` configuration file in `src/main/resources/`. There are many optional arguments that can be passed to the `eventchart` tool, e.g., number of event classes, tuning control limits etc. A full example:
```bash
$ ./runner.sh -input examples/st_example.log -type st -tool eventclass -classes 6 -alarm 3 -mad 2 -maf 3 -field speed -flightname FL1
```
Here `-classes` is the number of event classes the will be found using k-means. `-alarm` is the number of standard deviations from the expected value in order to raise a new event---essentially it controls the sensitivity with which events are found. `-mad` is the number of previous data points used for calculating a moving average for smoothing the raw data. `-maf` is similarly for smoothing, but applies to the extracted feature used for event detection.

There is also an experimental tool for generating "timed traces" over the abstract states, execute:
```bash
$ ./runner.sh -input examples/st_example.log -type st -tool traces -o ./timed_traces.txt -dim 2x2
```

## Additional Tools
You can also invoke the charting tool in a similar fashion. Just execute:
```bash
$ ./log2chart
```

The `./tools` directory contains additional tools that aid in understanding system behaviour. 
One is the `animator.py` tool that makes a real-time playback of the positions over time of the vehicle (drawn as lines on the grid). Currently this is a prototype likely to be extended.

Execute `python animator.py data.csv` to make the playback.

Here `data.csv`is a file with comma-separated x and y values.
`STCoordOutputter` can generate the `data.csv` for `animator.py`

## Implementation
`Log2model` can be considered a framework for processing log files and has many extension points which will be explained here. In addition, it comes with a number of log processors for e.g. generating models, visualization, data mining etc.
It is likely that much of what is explained here will change in the future.
Currently `Log2model` has support for the SafeTugs and AutoResolver log formats. The latter does not originally log to files so an extension has been made to AutoResolver which does this based on the trajectory data.

The important interfaces and extension points of `Log2model` are:
* `LogParser` implementors return `LogEntry` by parsing the input string.
* `LogEntry` represents the contents (e.g. fields) of a log entry (with a timestamp).
* `LogReader` is used for reading logs. The default `SequentialLogReader` simply reads a log line-by-line and invokes the `parse` method of the provided `LogParser` instance on each of them
* `LogHandler` implementations can be considered separate tools that plugs into the infrastructure. The topmost loghandler is `Main` and instances of `LogHandler` plug in by registering themselves.

Generating models for PRISM and UPPAAL is still under development. Previously there was a translation that worked, but required a user-supplied state definition. The infrastructure is currently being updated with a component that can find likely events in the time series data. These can later be regarded as states.
The model generated for a log is essentially a finite state machine with labels on transitions. The labels describe the frequency with which a behavior has transitioned from the source to the destination state. These frequencies can be used for computing probabilities---essentially the model describes a DTMC. The DTMC is described in terms of an IR that can further be translated to PRISM and (experimentally) to UPPAAL. The model can also be visualized by a translation to DOT. If the model is translated to the reactive modules formalism of PRISM, PCTL properties can be checked. There is a prototype functionality for transparently invoking the model checkers, i.e. the input to `log2model` is simply the log and a properties file. `Log2model` can then relay back the results by acting as a proxy to the model checker.

`Log2model` has functionality for mining likely events in the time series. It does this by using a combination of feature extraction, prediction model generation, and classification: The event detection method is based on computing a prediction model of the rate of change of the log field in question. Then it computes upper and lower control limits based on a making a Gaussian fit on a moving average of the rate of change feature. If the actual (observed) value exceeds the range describe by the limits, it signals a likely event has happened.
When all events have been found, the average feature is calculated for each time range to which the event applies. The average features are then used with k-means clustering to find partitions of events that are ''similar''. Each partition constitutes an abstract state that can then be used in the above mentioned model generation. 
