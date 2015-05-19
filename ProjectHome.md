`*``*``*`**IMPORTANT - PELOPS HAS MOVED TO [http://github.com/s7](http://github.com/s7)**`*``*``*`

`*``*``*`**EFFECTIVE AUGUST 2010**`*``*``*`

### Version 0.804 ###

## A Java client library for the Cassandra database ##

Pelops has been created to make working with [Cassandra](http://cassandra.apache.org/) a beautiful thing (hence
the nickname "Cassandra's beautiful son"). Using Pelops developers can quickly access the full power of Cassandra while writing clean, self-documenting code that makes the underlying semantics clear to reviewers. Without compromising
power, Pelops has been designed to improve productivity and code quality while greatly reducing the learning curve for new users. In fact, these objectives go hand in hand.

**Up and running in 5 minutes**

Understanding Pelops only takes 5 minutes. Please see the following introductory article
[http://ria101.wordpress.com/2010/06/11/pelops-the-beautiful-cassandra-database-client-for-java/](http://ria101.wordpress.com/2010/06/11/pelops-the-beautiful-cassandra-database-client-for-java/)

**Status**

Pelops serves as the basis for a complex commercial Cassandra project. Development is ongoing but a key objective is
to avoid breaking changes.

**Related projects**

If you are interested in performing locking over some of your Cassandra data, please also see
Cages @ http://cages.googlecode.com

**Change log**

_v0.804 23/6/10_

Previously the Selector methods getColumnsFromRows, getSubColumnsFromRows, getSuperColumnsFromRows that took key ranges returned a TreeMap. However, where an RP cluster is used, keys can be returned in (apparently) random order. It is important that this order is preserved when iterating over keys on a cluster using RP, since the last key is used as the start value for subsequent range requests. Therefore, instead of returning a TreeMap, we are now returning a LinkedHashMap. The key iterator of this map faithfully reflects the order in which the row data was returned by Cassandra, irrespective of whether OPP or RP is involved. If you are currently receiving the result of
these methods into a Map, then this change will not affect you.

_v0.803 23/6/10_

An API change was made to KeyDeletor as a consequence of a discussion
[here](http://ria101.wordpress.com/2010/06/11/pelops-the-beautiful-cassandra-database-client-for-java/#comment-162). Changes to two method names have been made in this class so that where they are used the changes must be acknowledged.