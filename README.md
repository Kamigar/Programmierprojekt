# routeplanner
## An efficient Dijkstra shortest path implementation

Usage: `routeplanner [OPTION]...`

### Command line options

OPTION | Explanation
-|-
`-h, --help` | Display this help page

The program operates on 2 input (default: `stdin`) and 2 output (default: `stdout`) streams.

OPTION | Explanation
-|-
`-i, --input-file <file><br>  [default: stdin]` | Description of the graph structure<br>Input format: `[total number of nodes]<br>[total number of edges]<br>FOR EACH NODE:<br>  [nodeID] [nodeID2] [latitude] [longitude] [elevation]<br>FOR EACH EDGE:<br>  [srcID] [trgID] [cost] [type] [maxspeed]`
`-r, --request-file <file><br>  [default: stdin]` | List of distances to calculate<br>Input format: `FOR EACH ROUTE:<br>  [srcID] [trgID]`
`-o, --output-file <file><br>  [default: stdout]` | Output of the calculated distances<br>Output format: `FOR EACH ROUTE:<br>  [cost]`
`-l, --log-file <file><br>  [default: stdout]` | Log output of the program

Also there is the option to start a single one-to-all calculation.

OPTION | Explanation
-|-
`-ota, --one-to-all <srcID>` | Calculate all distances from start point `<srcID>`

The log verbosity defaults to `--warning` (`--quiet` if `--output-file` is set to `stdout`).

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

**Only user input:**
```routeplanner -v -t```

**Graph structure from file, distance request from user:**
```routeplanner -v -t -i graph.fmi```


#### Working with pipes:

Because the default input/output streams are `stdin`/`stdout`, the program could be used with UNIX pipes.

**Read graph and requests from different files and write distances to a third file with pipes:**
```(cat graph.fmi; cat requests.que) | routeplanner > distances.ist```

**Read graph and requests from different files and print only the calculation time:**
```(cat graph.fmi; cat requests.que) | routeplanner -v | grep seconds```

