# shortest-path

## Usage

You run the program in sbt shell like this ```shortest-path/run <args>```

This program has two subcommands `shortest-path` and `dot`. Following are the
usage for each subcommand. Note that this require JVM 11+


```text
Usage: app shortest-path --data-uri <string> --start <string> --end <string>

Find the shortest path between two intersections

Options and flags:
    --help
        Display this help text.
    --data-uri <string>
        file:// or https:// 
    --start <string>
        Starting intersection. For example A:1
    --end <string>
        Finishing intersection. For example B:4
```

```text
Usage: app dot --data-uri <string>

Output traffic data to DOT format

Options and flags:
    --help
        Display this help text.
    --data-uri <string>
        file:// or https:// 
```

## Limitations

* This does not handle negative transit times 
* This does not handle if "expressways" are defined in the input JSON
* This is calculating the average transit time between intersections

## Future Improvements

* Use a graph library (such as [JGraphT](https://jgrapht.org/) or [Neo4j](https://neo4j.com/)) that's optimized to
  handle path queries
* Use a proper logger instead of `println()`
* Handle all errors in an `EitherT` context
* Add more property based tests