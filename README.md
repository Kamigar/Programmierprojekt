# routeplanner
## An efficient Dijkstra shortest path implementation

## Build

```
./gradlew build
```

The archive with all neccessary files is created under `build/distributions/routeplanner.tar`

## Installation

Extract the archive and run the script
```
tar xfv routeplanner.tar
routeplanner/bin/routeplanner [...]
```
If you don't want to specify the full path to run the program, link it to a location in your PATH variable
```
sudo tar xfv routeplanner.tar -C /usr/local/lib/
sudo ln -sv /usr/local/lib/routeplanner/bin/routeplanner /usr/local/bin/
routeplanner [...]
```

## Usage

Usage: `routeplanner [OPTION]...`

### Command line options

OPTION | Explanation
-|-
`-h, --help` | Display this help page

The program operates on 2 input (default: `stdin`) and 2 output (default: `stdout`) streams.

OPTION | Explanation
-|-
`-i, --input-file <file>`<br>&nbsp;&nbsp;`[default: stdin]` | Description of the graph structure<br>Input format:<br>`[total number of nodes]`<br>`[total number of edges]`<br>`FOR EACH NODE:`<br>&nbsp;&nbsp;`[nodeID] [nodeID2] [latitude] [longitude] [elevation]`<br>`FOR EACH EDGE:`<br>&nbsp;&nbsp;`[srcID] [trgID] [cost] [type] [maxspeed]`
`-r, --request-file <file>`<br>&nbsp;&nbsp;`[default: stdin]` | List of distances to calculate or coordinates for nearest neighbor search<br>Input format:<br>**default**<br>`FOR EACH ROUTE:`<br>&nbsp;&nbsp;`[srcID] [trgID]`<br>**--one-to-many**<br>`FOR EACH ROUTE:`<br>&nbsp;&nbsp;`[trgID]`<br>**--next-node-iterative, --next-node-fast**<br>`FOR EACH POINT:`<br>&nbsp;&nbsp;`[latitude] [longitude]`
`-o, --output-file <file>`<br>&nbsp;&nbsp;`[default: stdout]` | Output of the calculated distances or the next neighbors<br>Output format:<br>**default**<br>`FOR EACH ROUTE:`<br>&nbsp;&nbsp;`[cost]`<br>**--next-node-interative, --next-node-fast**<br>`FOR EACH NEAREST NEIGHBOR:`<br>&nbsp;&nbsp;`[distance] [nodeID]`
`-l, --log-file <file>`<br>&nbsp;&nbsp;`[default: stdout]` | Log output of the program

Also there is the option to start a single one-to-all or one-to-many calculation as well as a nearest neighbor search (iteratively or with k-d tree)

OPTION | Explanation
-|-
`-ota, --one-to-all <srcID>` | Calculate all distances from start point `<srcID>`
`-otm, --one-to-many <srcID>` | Calculate distances from start point `<srcID>`
`-nni, --next-node-iterative` | Calculate nearest neighbor iteratively
`-nnf, --next-node-fast` | Calculate nearest neighbor with k-d tree

The log verbosity defaults to `--warning` (`--quiet` if `--output-file` is same as `--log-file`).

OPTION | Explanation
-|-
`-w, --warning` | Log warnings and errors
`-q, --quiet` | Log only errors
`-v, --verbose` | Log additional information about the calculation process

By default the program terminates, if there is an error in the input file(s).

OPTION | Explanation
-|-
`-t, --tolerant` | Ignore input errors and try again

### Usage examples

#### Interactive use:

For interactive use of the program, the options `-v` and `-t` are very useful.

&nbsp;&nbsp;**Only user input:**

&nbsp;&nbsp;```routeplanner -v -t```

&nbsp;&nbsp;**Graph structure from file, distance request from user:**

&nbsp;&nbsp;```routeplanner -v -t -i graph.fmi```


#### Working with pipes:

Because the default input/output streams are `stdin`/`stdout`, the program could be used with UNIX pipes.

&nbsp;&nbsp;**Read graph and requests from different files and write distances to a third file with pipes:**

&nbsp;&nbsp;```(cat graph.fmi; cat requests.que) | routeplanner > distances.ist```

&nbsp;&nbsp;**Read graph and requests from different files and print only the calculation time:**

&nbsp;&nbsp;```(cat graph.fmi; cat requests.que) | routeplanner -v | grep seconds```

