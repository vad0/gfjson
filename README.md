# gfjson

Every API should be as simple as possible. But not simpler.

This is a garbage free JSON parser. It is not even a proper parser, it is a de. It is based on real logic tools.
API may not look familiar and friendly, but it is really fast. It is similar to using JsonParser from Jackson, but
unlike Jackson we don't allocate or even copy strings. Also, we have a couple of methods to parse arrays and structs
and to skip values we don't need. Parsing of increment from Binance with two quotes takes 800ns on my laptop. Snapshot
with 2000 quotes is parsed in 250us.

An example of usage can be found
here: https://github.com/vad0/gfjson/blob/master/src/test/java/de/ParseSnapshotTest.java
