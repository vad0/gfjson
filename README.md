# gfjson

Every API should be as simple as possible. But not simpler. (c) Otto von Bismarck

This is a garbage free JSON parser. It is based on real logic tools. API may not look familiar and friendly, but it is
really fast. It is similar to using JsonParser from Jackson, but unlike Jackson we don't allocate or even copy strings.
Also, we have a couple of methods to parse arrays and structs and to skip values we don't need. Parsing of increment
from Binance with two quotes takes 800ns on my laptop. Snapshot with 2000 quotes is parsed in 250us.

An example code for encoding nd decoding json can be found in jmh benchmarks and tests.

What do the benchmarks say?

```
Benchmark                             Mode  Cnt      Score      Error  Units
de.ReadBench.parseIncrement           avgt    5  15159.743 ±  415.935  ns/op
de.ReadBench.parseIncrementJackson    avgt    5  26337.642 ± 1239.309  ns/op
de.ReadBench.parseIncrementTree       avgt    5  30565.580 ± 1493.097  ns/op
ser.WriteBigIncrement.gfJsonAllocate  avgt    5   8161.840 ±  467.682  ns/op
ser.WriteBigIncrement.gfJsonClear     avgt    5   8189.900 ±   88.775  ns/op
ser.WriteBigIncrement.jackson         avgt    5  31073.208 ± 2441.616  ns/op
ser.WriteDummy.gfJsonAllocate         avgt    5    132.015 ±   20.519  ns/op
ser.WriteDummy.gfJsonClear            avgt    5    128.984 ±    1.310  ns/op
ser.WriteDummy.jackson                avgt    5    571.487 ±   17.395  ns/op
```

We see that parsing is approximately 2 times faster than 'readTree' approach of jackson and about 40% faster than
token-by-token parsing by jackson parser. Writing jsons to wire is about 4 timex faster than via jackson.