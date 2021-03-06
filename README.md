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

Usage: `routeplanner [OPERATION] [OPTION]...`


### Operations

OPERATION | Explanation
-|-
`-h, --help` | Display this help page

The program is able to calculate shortest paths and nearest neighbors. You can start one of these calculations or a web server which handles requests over HTTP.

OPERATION | Explanation
-|-
`-nbi, --node-by-id <nodeID>` | Find node information by ID
`-oto, --one-to-one` | Calculate distances between points
`-ota, --one-to-all <srcID>` | Calculate all distances from start point `<srcID>`
`-otm, --one-to-many <srcID>` | Calculate distances from start point `<srcID>`
`-nni, --next-node-iterative` | Calculate nearest neighbor iteratively
`-nnf, --next-node-fast` | Calculate nearest neighbor with k-d tree
`-srv, --server` | Start HTTP web server


### I/O stream options

The program operates on 2 input (default: `stdin`) and 2 output (default: `stdout`) streams.

OPTION | Explanation
-|-
`-i, --input-file <file>`<br>&nbsp;&nbsp;`[default: stdin]` | Description of the graph structure<br>Input format:<br>`[total number of nodes]`<br>`[total number of edges]`<br>`FOR EACH NODE:`<br>&nbsp;&nbsp;`[nodeID] [nodeID2] [latitude] [longitude] [elevation]`<br>`FOR EACH EDGE:`<br>&nbsp;&nbsp;`[srcID] [trgID] [cost] [type] [maxspeed]`
`-r, --request-file <file>`<br>&nbsp;&nbsp;`[default: stdin]` | List of distances to calculate or coordinates for nearest neighbor search<br>Input format:<br>**--one-to-one**<br>`FOR EACH ROUTE:`<br>&nbsp;&nbsp;`[srcID] [trgID]`<br>**--one-to-many**<br>`FOR EACH ROUTE:`<br>&nbsp;&nbsp;`[trgID]`<br>**--next-node-iterative, --next-node-fast**<br>`FOR EACH POINT:`<br>&nbsp;&nbsp;`[latitude] [longitude]`
`-o, --output-file <file>`<br>&nbsp;&nbsp;`[default: stdout]` | Output of the calculated distances or the next neighbors<br>**See [Output format options](#output-format-options)**
`-l, --log-file <file>`<br>&nbsp;&nbsp;`[default: stdout]` | Log output of the program


### <a name="output-format-options"></a>Output format options

The output of a calculation looks like this:<br>**--node-by-id**<br>`[success] [node]`<br>**--one-to-one, --one-to-all, --one-to-many**<br>`FOR EACH ROUTE:`<br>&nbsp;&nbsp;`[cost] [node]`<br>**--next-node-iterative, --next-node-fast**<br>`FOR EACH NEAREST NEIGHBOR:`<br>&nbsp;&nbsp;`[distance] [node]`

By default `[node]` is empty. You could change this with the followig options. If any of these parameters is set, `[node]` has the format `[node] := [id] [latitude] [longitude] [distance] [node]...`

OPTION | Explanation
-|-
`-pl, --print-location` | Print `[latitude] [longitude]` of each `[node]`
`-pd, --print-distance` | **--one-to-one, --one-to-all, --one-to-many**<br>Print `[distance]` to target for each `[node]`
`-pp, --print-path` | **--one-to-one, --one-to-all, --one-to-many**<br>Print every `[node]` in the calculated path<br>**Note:** The order of the nodes is reversed: `[trg] [node]... [src]`


### Server options

The web server receives/sends requests over HTTP on a specified port. Also all files in the HTML directory are served (with redirection from `/` to `/index.html`).

OPTION | Explanation
-|-
`-p, --port <port>`<br>&nbsp;&nbsp;`[default: 80]` | Port of the HTTP server
`-d, --html-directory <dir>`<br>&nbsp;&nbsp;`[default: {resources}/html]` | Directory which will be accessible through the HTTP server


### Log options

The log verbosity defaults to `--warning` (`--quiet` if `--output-file` is same as `--log-file`).

OPTION | Explanation
-|-
`-w, --warning` | Log warnings and errors
`-q, --quiet` | Log only errors
`-v, --verbose` | Log additional information about the calculation process


### Other options

By default the program terminates, if there is an error in the input file(s).

OPTION | Explanation
-|-
`-t, --tolerant` | Ignore input errors and try again


### Usage examples

#### Interactive use:

For interactive use of the program, the options `-v` and `-t` are very useful.

&nbsp;&nbsp;**Only user input:**

&nbsp;&nbsp;```routeplanner -oto -v -t```

&nbsp;&nbsp;**Graph structure from file, request from user:**

&nbsp;&nbsp;```routeplanner -nnf -v -t -i graph.fmi```


#### Working with pipes:

Because the default input/output streams are `stdin`/`stdout`, the program could be used with UNIX pipes.

&nbsp;&nbsp;**Read graph and requests from different files and write distances to a third file with pipes:**

&nbsp;&nbsp;```(cat graph.fmi; cat requests.que) | routeplanner -oto > distances.ist```

&nbsp;&nbsp;**Read graph and requests from different files and print only the calculation time:**

&nbsp;&nbsp;```(cat graph.fmi; cat requests.que) | routeplanner -oto -v | grep seconds```

